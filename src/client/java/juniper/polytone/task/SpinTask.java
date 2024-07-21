package juniper.polytone.task;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

public class SpinTask implements Task {
    float rotation;

    public SpinTask(float rotation) {
        this.rotation = rotation;
    }

    @Override
    public void prepare(MinecraftClient client) {
    }

    @Override
    public boolean tick(MinecraftClient client) {
        double move = MathHelper.sign(rotation);
        float yaw = client.player.getYaw();
        client.player.changeLookDirection(move * 10, 0);
        float yaw2 = client.player.getYaw();
        rotation -= yaw2 - yaw;
        //finish when rotation changes sign
        return move != MathHelper.sign(rotation);
    }
}
