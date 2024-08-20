package juniper.polytone.task.steps;

import juniper.polytone.task.NavigateTask.Tile;
import juniper.polytone.util.ArrayUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class TempStep implements Step {

    @Override
    public Vec3i getNewPos(Tile[][][] grid, Vec3i min, Vec3i oldPos) {
        Vec3i newPos = oldPos.add(1, 0, 0);
        if (ArrayUtil.inBounds(grid, newPos.subtract(min))) {
            return newPos;
        } else {
            return null;
        }
    }

    @Override
    public int getCost() {
        return 10;
    }

    @Override
    public boolean tick(MinecraftClient client, Vec3i destination) {
        client.player.setPosition(Vec3d.ofBottomCenter(destination));
        return true;
    }
}
