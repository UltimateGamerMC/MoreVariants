package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.variant.MobVariant;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ThrownEgg.class)
public class ChickenEggMixin {
    @ModifyVariable(
            method = "onHit(Lnet/minecraft/world/phys/HitResult;)V",
            at = @At("STORE")
    )
    private Chicken mixin(Chicken chickenEntity) {
        if (chickenEntity.level() instanceof ServerLevel world) {
            MobVariant variant = Variants.getRandomVariant(EntityType.CHICKEN, chickenEntity.getRandom().nextLong(), world.getBiome(chickenEntity.blockPosition()), null, world.getMoonBrightness(chickenEntity.blockPosition()));
            var data = MoreMobVariants.getCustomDataNbt(chickenEntity);
            data.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
            MoreMobVariants.setCustomDataNbt(chickenEntity, data);
        }
        return chickenEntity;
    }
}
