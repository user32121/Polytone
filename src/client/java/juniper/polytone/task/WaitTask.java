package juniper.polytone.task;

import net.minecraft.client.MinecraftClient;

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
}
