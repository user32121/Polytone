package juniper.polytone.task;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import juniper.polytone.pathfinding.GridView;
import juniper.polytone.pathfinding.PathFind;
import juniper.polytone.pathfinding.PathFind.Tile;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;

public class NavigateTask implements Task {
    private BlockPos target;
    private boolean fuzzy;
    private GridView grid;
    private PathFind path;

    public NavigateTask(BlockPos target, boolean fuzzy) {
        this.target = target;
        this.fuzzy = fuzzy;
    }

    @Override
    public void prepare(MinecraftClient client) {
        grid = new GridView(client.world, client.player.getChunkPos(), new ChunkPos(target));
        path = new PathFind(client.player.getBlockPos(), target, grid, fuzzy);
        path.start();
    }

    @Override
    public boolean tick(MinecraftClient client) {
        grid.processMainThread(client);

        Text t = path.feedback.poll();
        if (t != null) {
            client.player.sendMessage(t);
        }

        //wait for pathfinding to finish
        if (path.isAlive()) {
            return false;
        }

        //follow path
        if (path.path == null || path.path.size() == 0) {
            return true;
        }
        Pair<Vec3i, Tile> step = path.path.getFirst();
        boolean done = step.getRight().travelUsing.tick(client, step.getLeft());
        if (done) {
            path.path.removeFirst();
        }

        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("target", target.toShortString()).toString();
    }

    public static class NavigateTaskFactory implements TaskFactory<NavigateTask> {
        public static final RequiredArgumentBuilder<FabricClientCommandSource, ?> POS_ARG = ClientCommandManager.argument("position", BlockPosArgumentType.blockPos());
        public static final RequiredArgumentBuilder<FabricClientCommandSource, ?> FUZZY_ARG = ClientCommandManager.argument("fuzzy", BoolArgumentType.bool());

        @Override
        public String getTaskName() {
            return "navigate";
        }

        @Override
        public Text getDescription() {
            return Text.literal("Pathfind to a location, or to the closest possible spot if fuzzy is enabled");
        }

        @Override
        public List<RequiredArgumentBuilder<FabricClientCommandSource, ?>> getArgs() {
            return List.of(POS_ARG, FUZZY_ARG);
        }

        @Override
        public NavigateTask makeTask(CommandContext<FabricClientCommandSource> ctx) {
            FabricClientCommandSource fccs = ctx.getSource();
            ServerCommandSource scs = new ServerCommandSource(CommandOutput.DUMMY, fccs.getPosition(), fccs.getRotation(), null, 0, "client_command_source_wrapper",
                    Text.literal("Client Command Source Wrapper"), null, fccs.getEntity());
            BlockPos pos = ctx.getArgument(POS_ARG.getName(), PosArgument.class).toAbsoluteBlockPos(scs);
            boolean fuzzy = BoolArgumentType.getBool(ctx, FUZZY_ARG.getName());
            return new NavigateTask(pos, fuzzy);
        }
    }
}
