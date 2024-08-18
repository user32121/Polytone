package juniper.polytone.task;

import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class WaitTask implements Task {
    public static final Text DESCRIPTION = Text.literal("Wait a number of ticks");
    public static final RequiredArgumentBuilder<FabricClientCommandSource, Integer> TICKS_ARG = ClientCommandManager.argument("ticks", IntegerArgumentType.integer());
    public static final List<RequiredArgumentBuilder<FabricClientCommandSource, ?>> ARGS = List.of(TICKS_ARG);

    public static Task makeTask(CommandContext<FabricClientCommandSource> ctx) {
        int delay = IntegerArgumentType.getInteger(ctx, TICKS_ARG.getName());
        return new WaitTask(delay);
    }

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
}
