package juniper.polytone.render;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import juniper.polytone.pathfinding.PathFind;
import juniper.polytone.pathfinding.PathFind.Tile;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer.Renderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3i;

public class PathFindDebugRenderer implements Renderer {
    private List<PathFind> paths = new ArrayList<>();

    public void addPath(PathFind path) {
        paths.add(path);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        if (!PathFind.getShowPath()) {
            return;
        }
        for (int i = 0; i < paths.size(); ++i) {
            PathFind path = paths.get(i);
            if (path.path == null) {
                continue;
            }
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip(1.0));
            for (Pair<Vec3i, Tile> cur : path.path) {
                Vec3i pos = cur.getLeft();
                vertexConsumer.vertex(matrix4f, pos.getX() + 0.5f - (float) cameraX, pos.getY() + 0.5f - (float) cameraY, pos.getZ() + 0.5f - (float) cameraZ).color(0.0f, 1.0f, 0.0f, 1.0f).next();
            }
            if (path.path.isEmpty()) {
                paths.remove(i);
                --i;
            }
        }
    }

    @Override
    public void clear() {
        paths.clear();
    }
}
