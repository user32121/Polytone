package juniper.polytone.task;

import net.minecraft.client.MinecraftClient;

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
}
