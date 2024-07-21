package juniper.polytone.command;

import java.util.LinkedList;
import java.util.Queue;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import juniper.polytone.task.SpinTask;
import juniper.polytone.task.Task;
import juniper.polytone.task.WaitTask;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class TaskQueue {
    public static final RequiredArgumentBuilder<FabricClientCommandSource, Float> DEGREES_ARG = ClientCommandManager.argument("degrees", FloatArgumentType.floatArg());
    public static final RequiredArgumentBuilder<FabricClientCommandSource, Integer> TICKS_ARG = ClientCommandManager.argument("ticks", IntegerArgumentType.integer());
    private static final Queue<Task> taskQueue = new LinkedList<>();
    private static Task curTask = null;
    private static boolean runningTasks = false;

    public static int addSpinTask(CommandContext<FabricClientCommandSource> ctx) {
        float rotation = FloatArgumentType.getFloat(ctx, DEGREES_ARG.getName());
        taskQueue.add(new SpinTask(rotation));
        ctx.getSource().sendFeedback(Text.literal(String.format("Added spin task for %s degrees", rotation)));
        return 1;
    }

    public static int addWaitTask(CommandContext<FabricClientCommandSource> ctx) {
        int delay = IntegerArgumentType.getInteger(ctx, TICKS_ARG.getName());
        taskQueue.add(new WaitTask(delay));
        ctx.getSource().sendFeedback(Text.literal(String.format("Added wait task for %s ticks", delay)));
        return 1;
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

    @SuppressWarnings("resource")
    public static void stopTasks() {
        runningTasks = false;
        MinecraftClient.getInstance().inGameHud.getChatHud()
                .addMessage(Text.literal(String.format("Stopped tasks (%s unfinished and %s in queue)", curTask == null ? 0 : 1, taskQueue.size())));
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
