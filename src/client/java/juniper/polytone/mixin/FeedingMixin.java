package juniper.polytone.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import juniper.polytone.Polytone;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.passive.AnimalEntity;

@Mixin(AnimalEntity.class)
public class FeedingMixin {
    @Inject(method = "handleStatus", at = @At("HEAD"))
    public void handleStatus(byte status, CallbackInfo info) {
        if (status == EntityStatuses.ADD_BREEDING_PARTICLES) {
            Polytone.LOGGER.info(this.toString());
        }
    }
}
