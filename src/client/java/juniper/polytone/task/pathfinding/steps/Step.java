package juniper.polytone.task.pathfinding.steps;

import juniper.polytone.task.pathfinding.GridView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3i;

public interface Step {
    /**
     * @return the new position if this step was taken, or null if this step cannot be taken
     */
    public Vec3i getNewPos(GridView grid, Vec3i oldPos) throws InterruptedException;

    public int getCost();

    /**
     * @return true if finished
     */
    public boolean tick(MinecraftClient client, Vec3i destination);
}
