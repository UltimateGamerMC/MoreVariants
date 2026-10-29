package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.networking.ServerRespondBasicVariantPayload;
import com.github.nyuppo.variant.MobVariant;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Skeleton.class)
public class SkeletonVariantsMixin extends MobEntityVariantsMixin {
    private MobVariant variant = Variants.getDefaultVariant(EntityTypes.SKELETON);

    @Override
    protected void onWriteCustomData(ValueOutput view, CallbackInfo ci) {
        view.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
    }

    @Override
    protected void onReadCustomData(ValueInput view, CallbackInfo ci) {
        String variantStr = view.getStringOr(MoreMobVariants.NBT_KEY, "");
        if (!variantStr.isEmpty()) {
            if (variantStr.contains(":")) {
                variant = Variants.getVariant(EntityTypes.SKELETON, Identifier.parse(variantStr));
            } else {
                variant = Variants.getVariant(EntityTypes.SKELETON, MoreMobVariants.id(variantStr));
            }
        } else {
            Entity e = (Entity)(Object)this;
            Level w = e.level();
            Float moonSize = w instanceof ServerLevel ? ((ServerLevel)w).getMoonBrightness(e.blockPosition()) : null;
            variant = Variants.getRandomVariant(EntityTypes.SKELETON, ((Skeleton)(Object)this).getRandom().nextLong(), w.getBiome(((Skeleton)(Object)this).blockPosition()), null, moonSize);
        }
        MoreMobVariants.storeVariant((Entity)(Object)this, variant.getIdentifier().toString());
        MinecraftServer server = ((Entity)(Object)this).level().getServer();
        if (server != null) {
            server.getPlayerList().getPlayers().forEach((player) -> {
                ServerPlayNetworking.send(player, new ServerRespondBasicVariantPayload(((Entity)(Object)this).getId(), variant.getIdentifier().toString()));
            });
        }
    }

    @Override
    protected void onFinalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData entityData, CallbackInfoReturnable<SpawnGroupData> ci) {
        variant = Variants.getRandomVariant(EntityTypes.SKELETON, ((Skeleton)(Object)this).getRandom().nextLong(), world.getBiome(((Skeleton)(Object)this).blockPosition()), null, world.getLevel().getMoonBrightness(((Skeleton)(Object)this).blockPosition()));
        var data = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
        data.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
        MoreMobVariants.setCustomDataNbt((Entity)(Object)this, data);
    }
}
