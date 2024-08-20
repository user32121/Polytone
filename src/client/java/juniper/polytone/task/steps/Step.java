package juniper.polytone.task.steps;

import juniper.polytone.task.NavigateTask.Tile;
import net.minecraft.util.math.Vec3i;

public interface Step {
    public Vec3i getNewPos(Tile[][][] grid, Vec3i min, Vec3i oldPos);

    public int getCost();
}
