package juniper.polytone.command;

import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class TaskInfo {
    public static int spinInfo(CommandContext<FabricClientCommandSource> ctx) {
        ctx.getSource().sendFeedback(Text.literal("Spin a certain amount of degrees"));
        return 1;
    }

    public static int waitInfo(CommandContext<FabricClientCommandSource> ctx) {
        ctx.getSource().sendFeedback(Text.literal("Wait a number of ticks"));
        return 1;
    }
}
