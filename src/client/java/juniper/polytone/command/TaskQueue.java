package juniper.polytone.command;

import java.util.LinkedList;
import java.util.Queue;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import juniper.polytone.task.Task;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class TaskQueue {
    public static final Queue<Task> taskQueue = new LinkedList<>();
    public static final RequiredArgumentBuilder<FabricClientCommandSource, Float> VALUE_ARG = ClientCommandManager.argument("value", FloatArgumentType.floatArg());

    public static int addSpinTask(CommandContext<FabricClientCommandSource> ctx) {
        // float rotation = FloatArgumentType.getFloat(ctx, VALUE_ARG.getName());
        return -1;
    }

    public static int addWaitTask(CommandContext<FabricClientCommandSource> ctx) {
        return -1;
    }
}
