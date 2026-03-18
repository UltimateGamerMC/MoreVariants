package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.variant.MobVariant;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EggEntity.class)
public class ChickenEggMixin {
    @ModifyVariable(
            method = "onCollision(Lnet/minecraft/util/hit/HitResult;)V",
            at = @At("STORE")
    )
    private ChickenEntity mixin(ChickenEntity chickenEntity) {
        if (chickenEntity.getEntityWorld() instanceof ServerWorld world) {
            MobVariant variant = Variants.getRandomVariant(EntityType.CHICKEN, chickenEntity.getRandom().nextLong(), world.getBiome(chickenEntity.getBlockPos()), null, world.getMoonSize(chickenEntity.getBlockPos()));
            var data = MoreMobVariants.getCustomDataNbt(chickenEntity);
            data.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
            MoreMobVariants.setCustomDataNbt(chickenEntity, data);
        }
        return chickenEntity;
    }
}
