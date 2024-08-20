package juniper.polytone.task.steps;

import juniper.polytone.task.NavigateTask.Tile;
import juniper.polytone.task.NavigateTask.Tile.TILE_TYPE;
import juniper.polytone.util.ArrayUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class TeleportStep implements Step {
    private Vec3i offset;

    public TeleportStep(Vec3i offset) {
        this.offset = offset;
    }

    @Override
    public Vec3i getNewPos(Tile[][][] grid, Vec3i min, Vec3i oldPos) {
        Vec3i newPos = oldPos.add(offset);
        Vec3i gridPos = newPos.subtract(min);
        if (!ArrayUtil.hasValue(grid, gridPos, t -> t.type.equals(TILE_TYPE.EMPTY)) || !ArrayUtil.hasValue(grid, gridPos.up(), t -> t.type.equals(TILE_TYPE.EMPTY))
                || !ArrayUtil.hasValue(grid, gridPos.down(), t -> t.type.equals(TILE_TYPE.FLOOR))) {
            return null;
        }
        return newPos;
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
