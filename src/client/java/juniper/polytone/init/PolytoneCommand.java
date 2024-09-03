package juniper.polytone.init;

import java.util.function.Function;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import juniper.polytone.Polytone;
import juniper.polytone.command.RaycastTarget;
import juniper.polytone.command.TaskQueue;
import juniper.polytone.pathfinding.PathFind;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class PolytoneCommand {
    public static void init() {
        makeCommand("raycast_priority", node -> node.then(RaycastTarget.TARGET_ARG.executes(RaycastTarget::getTarget).then(RaycastTarget.VALUE_ARG.executes(RaycastTarget::setTarget))));
        makeCommand("tasks",
                node -> node.then(ClientCommandManager.literal("start").executes(TaskQueue::startTasks)).then(ClientCommandManager.literal("stop").executes(TaskQueue::stopTasks))
                        .then(ClientCommandManager.literal("skip").executes(TaskQueue::skipTask)).then(ClientCommandManager.literal("clear").executes(TaskQueue::clearTasks))
                        .then(ClientCommandManager.literal("view").executes(TaskQueue::viewTasks)).then(TaskQueue.makeInfoCommand()).then(TaskQueue.makeAddCommand()));
        makeCommand("pathfinding",
                node -> node.then(ClientCommandManager.literal("notify_interval").executes(PathFind::getNotifyInterval).then(PathFind.INTERVAL_ARG.executes(PathFind::setNotifyInterval)))
                        .then(ClientCommandManager.literal("search_radius").executes(PathFind::getSearchRadius).then(PathFind.RADIUS_ARG.executes(PathFind::setSearchRadius)))
                        .then(ClientCommandManager.literal("search_angle").executes(PathFind::getSearchAngle).then(PathFind.ANGLE_ARG.executes(PathFind::setSearchAngle))));
    }

    private static void makeCommand(String command, Function<LiteralArgumentBuilder<FabricClientCommandSource>, LiteralArgumentBuilder<FabricClientCommandSource>> buildCommand) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) -> dispatcher.register(buildCommand.apply(ClientCommandManager.literal(Polytone.MODID + ":" + command))));
    }
}
