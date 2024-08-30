package juniper.polytone.pathfinding;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import juniper.polytone.pathfinding.PathFind.Tile;
import juniper.polytone.pathfinding.PathFind.Tile.TILE_TYPE;
import juniper.polytone.util.ArrayUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.EmptyChunk;

/**
 * Provides a way for non-render threads to access the world in a thread safe manner
 */
public class GridView {
    private final int minY, maxY; //inclusive

    private Map<ChunkPos, Pair<CountDownLatch, Tile[][][]>> chunks = new HashMap<>();
    private BlockingQueue<ChunkPos> toProcess = new LinkedBlockingQueue<>();

    public GridView(World world) {
        this.minY = world.getBottomY();
        this.maxY = world.getTopY() - 1;
    }

    public Tile getTile(Vec3i pos) throws InterruptedException {
        if (pos.getY() < minY || pos.getY() > maxY) {
            return new Tile(TILE_TYPE.OUT_OF_BOUNDS);
        }

        ChunkPos cp = new ChunkPos(new BlockPos(pos));
        Pair<CountDownLatch, Tile[][][]> chunk = chunks.get(cp);

        if (chunk == null) {
            chunk = new Pair<>(new CountDownLatch(1), null);
            chunks.put(cp, chunk);
            toProcess.add(cp);
        }
        chunk.getLeft().await();

        return ArrayUtil.get(chunk.getRight(), pos.subtract(getMinPos(cp)));
    }

    public Vec3i getClosestReachablePos(Vec3i target) {
        Vec3i minPos = null;
        double minDistSqr = Double.POSITIVE_INFINITY;
        for (Entry<ChunkPos, Pair<CountDownLatch, Tile[][][]>> e : chunks.entrySet()) {
            if (e.getValue().getLeft().getCount() != 0) {
                continue;
            }
            Tile[][][] chunk = e.getValue().getRight();
            for (int x = 0; x < chunk.length; ++x) {
                for (int y = 0; y < chunk[x].length; ++y) {
                    for (int z = 0; z < chunk[x][y].length; ++z) {
                        Tile t = chunk[x][y][z];
                        BlockPos pos = getMinPos(e.getKey()).add(x, y, z);
                        double distSqr = target.getSquaredDistance(pos);
                        if (distSqr < minDistSqr && t.cost < Integer.MAX_VALUE) {
                            minDistSqr = distSqr;
                            minPos = pos;
                        }
                    }
                }
            }
        }
        if (minPos == null) {
            throw new RuntimeException("No tiles could be found");
        }
        return minPos;
    }

    /**
     * Runs processing that needs to be done on the main (client) thread
     */
    public void processMainThread(MinecraftClient client) {
        ChunkPos cp = toProcess.poll();
        if (cp == null) {
            return;
        }
        Vec3i size = getMaxPos(cp).subtract(getMinPos(cp)).add(1, 1, 1);
        Tile[][][] grid = new Tile[size.getX()][size.getY()][size.getZ()];
        if (client.world.getChunk(cp.x, cp.z) instanceof EmptyChunk) {
            for (BlockPos pos : BlockPos.iterate(getMinPos(cp), getMaxPos(cp))) {
                ArrayUtil.set(grid, pos.subtract(getMinPos(cp)), new Tile(TILE_TYPE.OUT_OF_BOUNDS));
            }
        } else {
            for (BlockPos pos : BlockPos.iterate(getMinPos(cp), getMaxPos(cp))) {
                TILE_TYPE tt = TILE_TYPE.OBSTACLE;
                if (client.world.getBlockState(pos).getCollisionShape(client.world, pos).isEmpty()) {
                    tt = TILE_TYPE.EMPTY;
                } else if (client.world.isTopSolid(pos, client.player)) {
                    tt = TILE_TYPE.FLOOR;
                }
                ArrayUtil.set(grid, pos.subtract(getMinPos(cp)), new Tile(tt));
            }
        }

        chunks.get(cp).setRight(grid);
        chunks.get(cp).getLeft().countDown();
    }

    /**
     * @return The most negative possible BlockPos in a ChunkPos, inclusive
     */
    public BlockPos getMinPos(ChunkPos cp) {
        return cp.getStartPos().withY(minY);
    }

    /**
     * @return The most positive possible BlockPos in a ChunkPos, inclusive
     */
    public BlockPos getMaxPos(ChunkPos cp) {
        return new BlockPos(cp.getEndX(), maxY, cp.getEndZ());
    }

    public boolean hasTileType(Vec3i pos, TILE_TYPE type) throws InterruptedException {
        return getTile(pos).type.equals(type);
    }

    /**
     * Clear all tile data. Mainly for memory reasons.
     */
    public void clear() {
        toProcess.clear();
        chunks.clear();
    }
}
