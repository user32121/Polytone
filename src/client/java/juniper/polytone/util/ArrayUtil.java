package juniper.polytone.util;

import java.util.function.Predicate;

import net.minecraft.util.math.Vec3i;

public class ArrayUtil {
    public static <T> T get(T[][][] a, Vec3i i) {
        return a[i.getX()][i.getY()][i.getZ()];
    }

    public static <T> void set(T[][][] a, Vec3i i, T v) {
        a[i.getX()][i.getY()][i.getZ()] = v;
    }

    public static <T> boolean inBounds(T[][][] grid, Vec3i i) {
        return i.getX() >= 0 && i.getY() >= 0 && i.getZ() >= 0 && i.getX() < grid.length && i.getY() < grid[i.getX()].length && i.getZ() < grid[i.getX()][i.getY()].length;
    }

    public static <T> boolean hasValue(T[][][] grid, Vec3i i, T v) {
        if (!inBounds(grid, i)) {
            return false;
        }
        return get(grid, i).equals(v);
    }

    public static <T> boolean hasValue(T[][][] grid, Vec3i i, Predicate<T> f) {
        if (!inBounds(grid, i)) {
            return false;
        }
        return f.test(get(grid, i));
    }
}
