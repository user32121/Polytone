package juniper.polytone.pathfinding.steps;

import juniper.polytone.pathfinding.GridView;
import juniper.polytone.pathfinding.PathFind.Tile.TILE_TYPE;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class TeleportStep implements Step {
    private Vec3i offset;

    public TeleportStep(Vec3i offset) {
        this.offset = offset;
    }

    @Override
    public Vec3i getNewPos(GridView grid, Vec3i oldPos) throws InterruptedException {
        Vec3i newPos = oldPos.add(offset);
        if (!grid.hasTileType(newPos, TILE_TYPE.EMPTY) || !grid.hasTileType(newPos.up(), TILE_TYPE.EMPTY) || !grid.hasTileType(newPos.down(), TILE_TYPE.FLOOR)) {
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
