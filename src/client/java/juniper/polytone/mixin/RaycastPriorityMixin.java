package juniper.polytone.mixin;

import java.util.Optional;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import juniper.polytone.Polytone;
import juniper.polytone.command.RaycastTarget;
import juniper.polytone.init.PolytoneCommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(ProjectileUtil.class)
public class RaycastPriorityMixin {
    @Inject(method = "raycast", at = @At("HEAD"), cancellable = true)
    private static void raycast(Entity source, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate, double maxDistance, CallbackInfoReturnable<EntityHitResult> info) {
        //should only trigger when player is doing cursor raycast
        //hopefully the player is never a projectile
        if (!(source instanceof PlayerEntity)) {
            return;
        }
        World world = source.getWorld();
        double curDist = maxDistance;
        Entity curEntity = null;
        Vec3d curPos = null;
        int curPriority = 0;
        Polytone.LOGGER.info("spacer----------------------------------------------------");
        for (Entity entity : world.getOtherEntities(source, box, predicate)) {
            Box box2 = entity.getBoundingBox().expand(entity.getTargetingMargin());
            //actual raycast check
            Optional<Vec3d> optional = box2.raycast(min, max);
            //check priorities
            int priority = 0;
            if (entity instanceof SheepEntity se) {
                priority += PolytoneCommand.raycastPriority.getOrDefault(RaycastTarget.CAN_SHEAR, false) && se.isShearable() ? 1 : 0;
            }
            //hitting from inside entity
            if (box2.contains(min)) {
                if (!(curDist >= 0.0))
                    continue;
                curEntity = entity;
                curPos = optional.orElse(min);
                curDist = 0.0;
                curPriority = priority;
                continue;
            }
            //fail raycast
            if (!optional.isPresent()) {
                continue;
            }
            //check if hit is closer or has higher priority
            Vec3d pos = optional.get();
            double dist = min.squaredDistanceTo(pos);
            Polytone.LOGGER.info(String.format("%s: %s, %s", entity, priority, dist));
            if (!((dist < curDist && priority == curPriority) || priority > curPriority) && curDist != 0.0) {
                Polytone.LOGGER.info("fail");
                continue;
            }
            Polytone.LOGGER.info("success");
            //hitting entity riding same thing/hitting vehicle?
            if (entity.getRootVehicle() == source.getRootVehicle()) {
                if (curDist != 0.0)
                    continue;
                curEntity = entity;
                curPos = pos;
                curPriority = priority;
                continue;
            }
            curEntity = entity;
            curPos = pos;
            curDist = dist;
            curPriority = priority;
        }
        if (curEntity == null) {
            info.setReturnValue(null);
        }
        info.setReturnValue(new EntityHitResult(curEntity, curPos));
    }
}
