package juniper.polytone.init;

import java.util.function.Function;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import juniper.polytone.Polytone;
import juniper.polytone.command.RaycastTarget;
import juniper.polytone.command.RaycastTarget.RaycastTargetArgument;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class PolytoneCommand {
    public static void init() {
        makeCommand("raycast_priority", node -> node.then(ClientCommandManager.argument("target", new RaycastTargetArgument()).executes(context -> {
            RaycastTarget target = context.getArgument("target", RaycastTarget.class);
            boolean b = RaycastTarget.raycastPriority.getOrDefault(target, false);
            context.getSource().sendFeedback(Text.literal(String.format("Raycast priority for %s is %s", target, b)));
            return 1;
        }).then(ClientCommandManager.argument("value", BoolArgumentType.bool()).executes(context -> {
            RaycastTarget target = context.getArgument("target", RaycastTarget.class);
            boolean b = BoolArgumentType.getBool(context, "value");
            RaycastTarget.raycastPriority.put(target, b);
            context.getSource().sendFeedback(Text.literal(String.format("Raycast priority for %s set to %s", target, b)));
            return 1;
        }))));

        // /view canBreed enable/disable
        //probably fits better in Monotone
    }

    private static void makeCommand(String command, Function<LiteralArgumentBuilder<FabricClientCommandSource>, LiteralArgumentBuilder<FabricClientCommandSource>> buildCommand) {
        ClientCommandRegistrationCallback.EVENT
                .register((dispatcher, registry) -> dispatcher.register(buildCommand.apply(ClientCommandManager.literal(Polytone.MODID + ":" + command))));
    }
}
