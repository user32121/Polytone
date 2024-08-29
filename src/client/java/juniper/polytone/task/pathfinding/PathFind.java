package juniper.polytone.task.pathfinding;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.collect.Lists;

import juniper.polytone.task.pathfinding.steps.Step;
import juniper.polytone.task.pathfinding.steps.TeleportStep;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3i;

/**
 * Runs pathfinding in a separate threads
 */
public class PathFind extends Thread {
    private static final int HEURISTIC_ESTIMATED_COST = 10;
    private static List<Step> STEPS = new ArrayList<>();
    static {
        for (int y = -1; y <= 1; ++y) {
            STEPS.add(new TeleportStep(new Vec3i(1, y, 0)));
            STEPS.add(new TeleportStep(new Vec3i(-1, y, 0)));
            STEPS.add(new TeleportStep(new Vec3i(0, y, 1)));
            STEPS.add(new TeleportStep(new Vec3i(0, y, -1)));
        }
    }

    public static class Tile {
        public enum TILE_TYPE {
            UNKNOWN, OUT_OF_BOUNDS, EMPTY, FLOOR, OBSTACLE,
        }

        public TILE_TYPE type = TILE_TYPE.UNKNOWN;
        public int cost = Integer.MAX_VALUE;
        public Vec3i travelFrom = null;
        public Step travelUsing = null;

        public Tile(TILE_TYPE type) {
            this.type = type;
        }
    }

    public List<Pair<Vec3i, Tile>> path;
    public BlockingQueue<Text> feedback = new LinkedBlockingQueue<>();

    private Vec3i start;
    private Vec3i target;
    private GridView grid;

    public PathFind(Vec3i start, Vec3i target, GridView grid) {
        this.start = start;
        this.target = target;
        this.grid = grid;
    }

    @Override
    public void run() {
        try {
            long blocksExplored = 0;
            long lastChecked = System.currentTimeMillis();
            Queue<Vec3i> toProcess = new PriorityQueue<>((v1, v2) -> {
                int cost1;
                int cost2;
                try {
                    cost1 = grid.getTile(v1).cost;
                    cost2 = grid.getTile(v2).cost;
                } catch (InterruptedException e) {
                    return 0;
                }
                int heuristic1 = HEURISTIC_ESTIMATED_COST * v1.getManhattanDistance(target);
                int heuristic2 = HEURISTIC_ESTIMATED_COST * v2.getManhattanDistance(target);
                return (cost1 + heuristic1) - (cost2 + heuristic2);
            });
            toProcess.add(start);
            grid.getTile(start).cost = 0;
            grid.getTile(start).travelFrom = start;
            while (toProcess.size() > 0 && grid.getTile(target).travelFrom == null) {
                if (System.currentTimeMillis() - lastChecked >= 5000) {
                    feedback.add(Text.literal(String.format("pathfinding (%s blocks explored) ...", blocksExplored)));
                    lastChecked = System.currentTimeMillis();
                }
                Vec3i pos = toProcess.remove();
                ++blocksExplored;
                for (Step step : STEPS) {
                    Vec3i newPos = step.getNewPos(grid, pos);
                    if (newPos == null) {
                        continue;
                    }
                    int newCost = grid.getTile(pos).cost + step.getCost();
                    if (newCost < grid.getTile(newPos).cost) {
                        Tile t = grid.getTile(newPos);
                        t.cost = newCost;
                        t.travelFrom = pos;
                        t.travelUsing = step;
                        toProcess.add(newPos);
                    }
                }
            }
            //extract path
            Vec3i pos = target;
            path = new LinkedList<>();
            while (!pos.equals(start)) {
                Tile t = grid.getTile(pos);
                path.add(new Pair<>(pos, t));
                pos = t.travelFrom;
                if (pos == null) {
                    path = null;
                    feedback.add(Text.literal(String.format("Unable to reach %s (%s) (%s blocks explored)", target, grid.getTile(target).type, blocksExplored)).formatted(Formatting.RED));
                    return;
                }
            }
            path = Lists.reverse(path);
            feedback.add(Text.literal(String.format("Found a path of length %s (%s blocks explored)", path.size(), blocksExplored)));
        } catch (InterruptedException e) {
            feedback.add(Text.literal(String.format("An exception occurred: %s", e)).formatted(Formatting.RED));
        }
    }
}
