package juniper.polytone.task;

import java.util.List;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public interface Task {
    /**
     * Called before the first tick of a task
     */
    public void prepare(MinecraftClient client);

    /**
     * Called every tick during a task
     * @return true if the task finished
     */
    public boolean tick(MinecraftClient client);

    public static interface TaskFactory<T extends Task> {
        public String getTaskName();

        public Text getDescription();

        public List<RequiredArgumentBuilder<FabricClientCommandSource, ?>> getArgs();

        public T makeTask(CommandContext<FabricClientCommandSource> ctx);
    }
}
