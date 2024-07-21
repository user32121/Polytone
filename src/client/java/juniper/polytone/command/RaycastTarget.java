package juniper.polytone.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public enum RaycastTarget {
    CAN_SHEAR, CAN_FEED;

    public static class RaycastTargetArgument implements ArgumentType<RaycastTarget> {
        private static final DynamicCommandExceptionType EXCEPTION_TYPE = new DynamicCommandExceptionType(target -> Text.literal("Unknown target: " + target));

        @Override
        public RaycastTarget parse(StringReader reader) throws CommandSyntaxException {
            String s = reader.readUnquotedString();
            try {
                return Enum.valueOf(RaycastTarget.class, s.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw EXCEPTION_TYPE.createWithContext(reader, s);
            }
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            return CommandSource.suggestMatching(Arrays.stream(values()).map(Object::toString).map(String::toLowerCase), builder);
        }
    }

    public static final Map<RaycastTarget, Boolean> raycastPriority = new HashMap<>();
    public static final RequiredArgumentBuilder<FabricClientCommandSource, RaycastTarget> TARGET_ARG = ClientCommandManager.argument("target", new RaycastTargetArgument());
    public static final RequiredArgumentBuilder<FabricClientCommandSource, Boolean> VALUE_ARG = ClientCommandManager.argument("value", BoolArgumentType.bool());

    public static int getTarget(CommandContext<FabricClientCommandSource> context) {
        RaycastTarget target = context.getArgument(TARGET_ARG.getName(), RaycastTarget.class);
        boolean b = RaycastTarget.raycastPriority.getOrDefault(target, false);
        context.getSource().sendFeedback(Text.literal(String.format("Raycast priority for %s is %s", target, b)));
        return 1;
    }

    public static int setTarget(CommandContext<FabricClientCommandSource> context) {
        RaycastTarget target = context.getArgument(TARGET_ARG.getName(), RaycastTarget.class);
        boolean b = BoolArgumentType.getBool(context, VALUE_ARG.getName());
        RaycastTarget.raycastPriority.put(target, b);
        context.getSource().sendFeedback(Text.literal(String.format("Raycast priority for %s set to %s", target, b)));
        return 1;
    }
}
