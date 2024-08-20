package juniper.polytone.task;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class WaitTask implements Task {
    private int ticksLeft;

    public WaitTask(int delay) {
        ticksLeft = delay;
    }

    @Override
    public void prepare(MinecraftClient client) {
    }

    @Override
    public boolean tick(MinecraftClient client) {
        --ticksLeft;
        return ticksLeft <= 0;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("ticks", ticksLeft).toString();
    }

    public static class WaitTaskFactory implements TaskFactory<WaitTask> {
        public static final RequiredArgumentBuilder<FabricClientCommandSource, Integer> TICKS_ARG = ClientCommandManager.argument("ticks", IntegerArgumentType.integer());

        @Override
        public String getTaskName() {
            return "wait";
        }

        @Override
        public Text getDescription() {
            return Text.literal("Wait a number of ticks");
        }

        @Override
        public List<RequiredArgumentBuilder<FabricClientCommandSource, ?>> getArgs() {
            return List.of(TICKS_ARG);
        }

        @Override
        public WaitTask makeTask(CommandContext<FabricClientCommandSource> ctx) {
            int delay = IntegerArgumentType.getInteger(ctx, TICKS_ARG.getName());
            return new WaitTask(delay);
        }
    }
}
