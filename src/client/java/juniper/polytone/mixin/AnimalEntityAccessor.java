package juniper.polytone.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.passive.AnimalEntity;

@Mixin(AnimalEntity.class)
public interface AnimalEntityAccessor {
    @Accessor
    public static int getBREEDING_COOLDOWN() {
        throw new AssertionError("Mixin failed");
    }
}
