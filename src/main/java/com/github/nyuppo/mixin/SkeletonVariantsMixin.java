package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.networking.ServerRespondBasicVariantPayload;
import com.github.nyuppo.variant.MobVariant;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkeletonEntity.class)
public class SkeletonVariantsMixin extends MobEntityVariantsMixin {
    private MobVariant variant = Variants.getDefaultVariant(EntityType.SKELETON);

    @Override
    protected void onWriteCustomData(WriteView view, CallbackInfo ci) {
        view.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
    }

    @Override
    protected void onReadCustomData(ReadView view, CallbackInfo ci) {
        String variantStr = view.getString(MoreMobVariants.NBT_KEY, "");
        if (!variantStr.isEmpty()) {
            if (variantStr.contains(":")) {
                variant = Variants.getVariant(EntityType.SKELETON, Identifier.of(variantStr));
            } else {
                variant = Variants.getVariant(EntityType.SKELETON, MoreMobVariants.id(variantStr));
            }
        } else {
            Entity e = (Entity)(Object)this;
            World w = e.getEntityWorld();
            Float moonSize = w instanceof ServerWorld ? ((ServerWorld)w).getMoonSize(e.getBlockPos()) : null;
            variant = Variants.getRandomVariant(EntityType.SKELETON, ((SkeletonEntity)(Object)this).getRandom().nextLong(), w.getBiome(((SkeletonEntity)(Object)this).getBlockPos()), null, moonSize);
        }
        MinecraftServer server = ((Entity)(Object)this).getEntityWorld().getServer();
        if (server != null) {
            server.getPlayerManager().getPlayerList().forEach((player) -> {
                ServerPlayNetworking.send(player, new ServerRespondBasicVariantPayload(((Entity)(Object)this).getId(), variant.getIdentifier().toString()));
            });
        }
    }

    @Override
    protected void onInitialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, CallbackInfoReturnable<EntityData> ci) {
        variant = Variants.getRandomVariant(EntityType.SKELETON, ((SkeletonEntity)(Object)this).getRandom().nextLong(), world.getBiome(((SkeletonEntity)(Object)this).getBlockPos()), null, world.toServerWorld().getMoonSize(((SkeletonEntity)(Object)this).getBlockPos()));
        var data = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
        data.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
        MoreMobVariants.setCustomDataNbt((Entity)(Object)this, data);
    }
}
