package juniper.polytone.command;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import juniper.polytone.task.NavigateTask.NavigateTaskFactory;
import juniper.polytone.task.SpinTask.SpinTaskFactory;
import juniper.polytone.task.Task;
import juniper.polytone.task.Task.TaskFactory;
import juniper.polytone.task.WaitTask.WaitTaskFactory;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class TaskQueue {
    private static final Queue<Task> taskQueue = new LinkedList<>();
    private static Task curTask = null;
    private static boolean runningTasks = false;

    private static List<TaskFactory<?>> taskFactories = new ArrayList<>();
    static {
        taskFactories.add(new SpinTaskFactory());
        taskFactories.add(new WaitTaskFactory());
        taskFactories.add(new NavigateTaskFactory());
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> makeInfoCommand() {
        LiteralArgumentBuilder<FabricClientCommandSource> node = ClientCommandManager.literal("info");
        for (TaskFactory<?> factory : taskFactories) {
            node = node.then(ClientCommandManager.literal(factory.getTaskName()).executes(ctx -> {
                ctx.getSource().sendFeedback(factory.getDescription());
                return 1;
            }));
        }
        return node;
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> makeAddCommand() {
        LiteralArgumentBuilder<FabricClientCommandSource> node = ClientCommandManager.literal("add");
        for (TaskFactory<?> factory : taskFactories) {
            Command<FabricClientCommandSource> cmd = ctx -> {
                Task task = factory.makeTask(ctx);
                TaskQueue.taskQueue.add(task);
                ctx.getSource().sendFeedback(Text.literal(String.format("Added %s", task)));
                return 1;
            };
            RequiredArgumentBuilder<FabricClientCommandSource, ?> argNode = null;
            for (RequiredArgumentBuilder<FabricClientCommandSource, ?> arg : Lists.reverse(factory.getArgs())) {
                if (argNode == null) {
                    argNode = arg.executes(cmd);
                } else {
                    argNode = arg.then(argNode);
                }
            }
            if (argNode == null) {
                node = node.then(ClientCommandManager.literal(factory.getTaskName()).executes(cmd));
            } else {
                node = node.then(ClientCommandManager.literal(factory.getTaskName()).then(argNode));
            }
        }
        return node;
    }

    public static int startTasks(CommandContext<FabricClientCommandSource> ctx) {
        runningTasks = true;
        ctx.getSource().sendFeedback(Text.literal(String.format("Starting tasks (%s unfinished and %s in queue)", curTask == null ? 0 : 1, taskQueue.size())));
        return 1;
    }

    public static int stopTasks(CommandContext<FabricClientCommandSource> ctx) {
        runningTasks = false;
        ctx.getSource().sendFeedback(Text.literal(String.format("Stopped tasks (%s unfinished and %s in queue)", curTask == null ? 0 : 1, taskQueue.size())));
        return 1;
    }

    public static int clearTasks(CommandContext<FabricClientCommandSource> ctx) {
        skipTask(ctx);
        ctx.getSource().sendFeedback(Text.literal(String.format("Cleared tasks (%s in queue)", taskQueue.size())));
        taskQueue.clear();
        return 1;
    }

    @SuppressWarnings("resource")
    public static void stopTasks() {
        runningTasks = false;
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal(String.format("Stopped tasks (%s unfinished and %s in queue)", curTask == null ? 0 : 1, taskQueue.size())));
    }

    public static int skipTask(CommandContext<FabricClientCommandSource> ctx) {
        if (curTask == null) {
            ctx.getSource().sendFeedback(Text.literal("No current task"));
            return 0;
        }
        ctx.getSource().sendFeedback(Text.literal(String.format("Skipped task %s", curTask)));
        curTask = null;
        return 1;
    }

    public static void tick(MinecraftClient client) {
        if (!runningTasks) {
            return;
        }
        if (curTask == null) {
            curTask = taskQueue.poll();
            if (curTask == null) {
                return;
            }
            client.inGameHud.getChatHud().addMessage(Text.literal(String.format("Processing task %s", curTask)));
            curTask.prepare(client);
        }
        if (curTask != null) {
            boolean finished = curTask.tick(client);
            if (finished) {
                client.inGameHud.getChatHud().addMessage(Text.literal(String.format("Finished task %s", curTask)));
                curTask = null;
            }
        }
    }
}
