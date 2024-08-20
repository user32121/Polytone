package juniper.polytone.task;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import com.google.common.collect.Lists;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import juniper.polytone.Polytone;
import juniper.polytone.task.NavigateTask.Tile.TILE_TYPE;
import juniper.polytone.task.steps.Step;
import juniper.polytone.task.steps.TempStep;
import juniper.polytone.util.ArrayUtil;
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
    private static List<Step> STEPS = new ArrayList<>();
    static {
        STEPS.add(new TempStep());
    }

    private BlockPos target;
    private List<Tile> path;

    public static class Tile {
        enum TILE_TYPE {
            UNKNOWN, EMPTY, FLOOR, OBSTACLE,
        }

        TILE_TYPE type = TILE_TYPE.UNKNOWN;
        int cost = Integer.MAX_VALUE;
        Vec3i travelFrom = null;
        Step travelUsing = null;

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
        BlockPos start = client.player.getBlockPos();
        BlockPos min = BlockPos.min(start, target);
        BlockPos max = BlockPos.max(start, target);
        ChunkPos minChunk = new ChunkPos(min);
        ChunkPos maxChunk = new ChunkPos(max);
        min = new BlockPos(minChunk.getStartX(), client.world.getBottomY(), minChunk.getStartZ());
        max = new BlockPos(maxChunk.getEndX(), client.world.getTopY() - 1, maxChunk.getEndZ());
        //convert world to simplified view
        Tile[][][] grid = new Tile[max.getX() - min.getX() + 1][max.getY() - min.getY() + 1][max.getZ() - min.getZ() + 1];
        for (BlockPos pos : BlockPos.iterate(min, max)) {
            TILE_TYPE tt = TILE_TYPE.OBSTACLE;
            if (client.world.isAir(pos)) {
                tt = TILE_TYPE.EMPTY;
            } else if (client.world.isTopSolid(pos, client.player)) {
                tt = TILE_TYPE.FLOOR;
            }
            ArrayUtil.set(grid, pos.subtract(min), new Tile(tt));
        }
        //run pathfinding
        Queue<Vec3i> toProcess = new PriorityQueue<>((v1, v2) -> {
            int cost1 = ArrayUtil.get(grid, v1).cost;
            int cost2 = ArrayUtil.get(grid, v2).cost;
            int heuristic1 = HEURISTIC_COST * v1.getManhattanDistance(target);
            int heuristic2 = HEURISTIC_COST * v2.getManhattanDistance(target);
            return (cost1 + heuristic1) - (cost2 + heuristic2);
        });
        toProcess.add(start);
        ArrayUtil.get(grid, start.subtract(min)).cost = 0;
        ArrayUtil.get(grid, start.subtract(min)).travelFrom = start;
        while (toProcess.size() > 0 && ArrayUtil.get(grid, target.subtract(min)).travelFrom == null) {
            Vec3i pos = toProcess.remove();
            for (Step step : STEPS) {
                Vec3i newPos = step.getNewPos(grid, min, pos);
                if (newPos == null) {
                    continue;
                }
                int newCost = ArrayUtil.get(grid, pos.subtract(min)).cost + step.getCost();
                if (newCost < ArrayUtil.get(grid, newPos.subtract(min)).cost) {
                    Tile t = ArrayUtil.get(grid, newPos.subtract(min));
                    t.cost = newCost;
                    t.travelFrom = pos;
                    t.travelUsing = step;
                    toProcess.add(newPos);
                }
            }
        }
        //extract path
        Vec3i pos = target;
        path = new ArrayList<>();
        while (!pos.equals(start)) {
            Tile t = ArrayUtil.get(grid, pos.subtract(min));
            path.add(t);
            pos = t.travelFrom;
            if (pos == null) {
                client.player.sendMessage(Text.literal(String.format("Unable to reach %s", target)));
                path = null;
                return;
            }
        }
        path = Lists.reverse(path);
    }

    @Override
    public boolean tick(MinecraftClient client) {
        // TODO Auto-generated method stub
        Polytone.LOGGER.info("path: {}", path);
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
