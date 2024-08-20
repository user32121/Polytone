package juniper.polytone.task.steps;

import juniper.polytone.task.NavigateTask.Tile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3i;

public interface Step {
    /**
     * @return the new position if this step was taken, or null if this step cannot be taken
     */
    public Vec3i getNewPos(Tile[][][] grid, Vec3i min, Vec3i oldPos);

    public int getCost();

    /**
     * @return true if finished
     */
    public boolean tick(MinecraftClient client, Vec3i destination);
}
