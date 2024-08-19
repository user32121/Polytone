package juniper.polytone.task;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import juniper.polytone.task.NavigateTask.Tile.TILE_TYPE;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;

public class NavigateTask implements Task {
    private static final int HEURISTIC_COST = 10;

    private BlockPos target;

    public static class Tile {
        enum TILE_TYPE {
            UNKNOWN, EMPTY, FLOOR, OBSTACLE,
        }

        TILE_TYPE type = TILE_TYPE.UNKNOWN;
        int cost = Integer.MAX_VALUE;
        Vec3i travel_from = null;

        public Tile(TILE_TYPE type) {
            this.type = type;
        }
    }

    public NavigateTask(BlockPos target) {
        this.target = target;
    }

    @Override
    public void prepare(MinecraftClient client) {
        //get search bounds
        //TODO bound search by render distance (and lazily load chunks)
        BlockPos cur = client.player.getBlockPos();
        BlockPos min = BlockPos.min(cur, target);
        BlockPos max = BlockPos.max(cur, target);
        ChunkPos minChunk = new ChunkPos(min);
        ChunkPos maxChunk = new ChunkPos(max);
        min = new BlockPos(minChunk.getStartX(), client.world.getBottomY(), minChunk.getStartZ());
        max = new BlockPos(maxChunk.getEndX(), client.world.getTopY() - 1, maxChunk.getEndZ());
        //convert world to simplified view
        Tile[][][] grid = new Tile[max.getX() - min.getX() + 1][max.getY() - min.getY() + 1][max.getZ() - min.getZ() + 1];
        for (BlockPos pos : BlockPos.iterate(min, max)) {
            Vec3i gridPos = pos.subtract(min);
            TILE_TYPE t = TILE_TYPE.OBSTACLE;
            if (client.world.isAir(pos)) {
                t = TILE_TYPE.EMPTY;
            } else if (client.world.isTopSolid(pos, client.player)) {
                t = TILE_TYPE.FLOOR;
            }
            grid[gridPos.getX()][gridPos.getY()][gridPos.getZ()] = new Tile(t);
        }
        //run pathfinding
        Queue<Vec3i> toProcess = new PriorityQueue<>((v1, v2) -> {
            int cost1 = grid[v1.getX()][v1.getY()][v1.getZ()].cost;
            int cost2 = grid[v2.getX()][v2.getY()][v2.getZ()].cost;
            int heuristic1 = HEURISTIC_COST * v1.getManhattanDistance(target);
            int heuristic2 = HEURISTIC_COST * v2.getManhattanDistance(target);
            return (cost1 + heuristic1) - (cost2 + heuristic2);
        });
        //extract path
    }

    @Override
    public boolean tick(MinecraftClient client) {
        // TODO Auto-generated method stub
        return true;
    }

    public static class NavigateTaskFactory implements TaskFactory<NavigateTask> {
        public static final RequiredArgumentBuilder<FabricClientCommandSource, ?> POS_ARG = ClientCommandManager.argument("position", BlockPosArgumentType.blockPos());

        @Override
        public String getTaskName() {
            return "navigate";
        }

        @Override
        public Text getDescription() {
            return Text.literal("Pathfind to a location");
        }

        @Override
        public List<RequiredArgumentBuilder<FabricClientCommandSource, ?>> getArgs() {
            return List.of(POS_ARG);
        }

        @Override
        public NavigateTask makeTask(CommandContext<FabricClientCommandSource> ctx) {
            FabricClientCommandSource fccs = ctx.getSource();
            ServerCommandSource scs = new ServerCommandSource(CommandOutput.DUMMY, fccs.getPosition(), fccs.getRotation(), null, 0, "client_command_source_wrapper",
                    Text.literal("Client Command Source Wrapper"), null, fccs.getEntity());
            BlockPos pos = ctx.getArgument(POS_ARG.getName(), PosArgument.class).toAbsoluteBlockPos(scs);
            return new NavigateTask(pos);
        }
    }
}
