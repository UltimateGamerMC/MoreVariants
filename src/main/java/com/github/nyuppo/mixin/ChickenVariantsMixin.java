package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.networking.ServerRespondBasicVariantPayload;
import com.github.nyuppo.util.MMVTrackedData;
import com.github.nyuppo.variant.MobVariant;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.ChickenVariants;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChickenEntity.class)
public abstract class ChickenVariantsMixin extends MobEntityVariantsMixin {
    private MobVariant variant = Variants.getDefaultVariant(EntityType.CHICKEN);

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void onInitDataTracker(net.minecraft.entity.data.DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(MMVTrackedData.CHICKEN_VARIANT_ID, "");
    }

    @Override
    protected void onWriteCustomData(WriteView view, CallbackInfo ci) {
        NbtCompound nbt = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
        if (nbt.contains(MoreMobVariants.NBT_KEY)) {
            view.putString(MoreMobVariants.NBT_KEY, nbt.getString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString()));
        }
    }

    @Override
    protected void onReadCustomData(ReadView view, CallbackInfo ci) {
        String variantStr = view.getString(MoreMobVariants.NBT_KEY, "");
        boolean hasModVariant = false;
        if (!variantStr.isEmpty()) {
            hasModVariant = true;
            if (variantStr.contains(":")) {
                variant = Variants.getVariant(EntityType.CHICKEN, Identifier.of(variantStr));
            } else {
                variant = Variants.getVariant(EntityType.CHICKEN, MoreMobVariants.id(variantStr));
            }
            ((ChickenEntity)(Object)this).getDataTracker().set(MMVTrackedData.CHICKEN_VARIANT_ID, variant.getIdentifier().toString());
            ((ChickenEntity)(Object)this).setVariant(((ChickenEntity)(Object)this).getRegistryManager().getOrThrow(RegistryKeys.CHICKEN_VARIANT).getOrThrow(ChickenVariants.TEMPERATE));
        } else {
            Entity e = (Entity)(Object)this;
            World w = e.getEntityWorld();
            if (MoreMobVariants.isTemperateBiome(w.getBiome(((ChickenEntity)(Object)this).getBlockPos()))) {
                hasModVariant = true;
                Float moonSize = w instanceof ServerWorld ? ((ServerWorld)w).getMoonSize(e.getBlockPos()) : null;
                variant = Variants.getRandomVariant(EntityType.CHICKEN, ((ChickenEntity)(Object)this).getRandom().nextLong(), w.getBiome(((ChickenEntity)(Object)this).getBlockPos()), null, moonSize);
                String id = variant.getIdentifier().toString();
                ((ChickenEntity)(Object)this).getDataTracker().set(MMVTrackedData.CHICKEN_VARIANT_ID, id);
                ((ChickenEntity)(Object)this).setVariant(((ChickenEntity)(Object)this).getRegistryManager().getOrThrow(RegistryKeys.CHICKEN_VARIANT).getOrThrow(ChickenVariants.TEMPERATE));
                var data = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
                data.putString(MoreMobVariants.NBT_KEY, id);
                MoreMobVariants.setCustomDataNbt((Entity)(Object)this, data);
            }
        }
        if (hasModVariant) {
            MinecraftServer server = ((Entity)(Object)this).getEntityWorld().getServer();
            if (server != null) {
                server.getPlayerManager().getPlayerList().forEach((player) -> {
                    ServerPlayNetworking.send(player, new ServerRespondBasicVariantPayload(((Entity)(Object)this).getId(), variant.getIdentifier().toString()));
                });
            }
        }
    }

    @Override
    protected void onInitialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, CallbackInfoReturnable<EntityData> ci) {
        if (!MoreMobVariants.isTemperateBiome(world.getBiome(((ChickenEntity)(Object)this).getBlockPos()))) return;
        variant = Variants.getRandomVariant(EntityType.CHICKEN, ((ChickenEntity)(Object)this).getRandom().nextLong(), world.getBiome(((ChickenEntity)(Object)this).getBlockPos()), null, world.toServerWorld().getMoonSize(((ChickenEntity)(Object)this).getBlockPos()));
        String id = variant.getIdentifier().toString();
        ((ChickenEntity)(Object)this).getDataTracker().set(MMVTrackedData.CHICKEN_VARIANT_ID, id);
        var data = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
        data.putString(MoreMobVariants.NBT_KEY, id);
        MoreMobVariants.setCustomDataNbt((Entity)(Object)this, data);
        ((ChickenEntity)(Object)this).setVariant(world.toServerWorld().getRegistryManager().getOrThrow(RegistryKeys.CHICKEN_VARIANT).getOrThrow(ChickenVariants.TEMPERATE));
    }

    @Inject(
            method = "createChild(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/PassiveEntity;)Lnet/minecraft/entity/passive/ChickenEntity;",
            at = @At("RETURN")
    )
    private void onCreateChild(ServerWorld world, PassiveEntity entity, CallbackInfoReturnable<ChickenEntity> ci) {
        ChickenEntity child = ci.getReturnValue();
        if (child == null) return;
        if (!MoreMobVariants.isTemperateBiome(world.getBiome(child.getBlockPos()))) return;
        MobVariant childVariant = Variants.getChildVariant(EntityType.CHICKEN, world, ((ChickenEntity)(Object)this), entity);
        child.setVariant(world.getRegistryManager().getOrThrow(RegistryKeys.CHICKEN_VARIANT).getOrThrow(ChickenVariants.TEMPERATE));
        String id = childVariant.getIdentifier().toString();
        child.getDataTracker().set(MMVTrackedData.CHICKEN_VARIANT_ID, id);
        var childData = MoreMobVariants.getCustomDataNbt(child);
        childData.putString(MoreMobVariants.NBT_KEY, id);
        MoreMobVariants.setCustomDataNbt(child, childData);
    }
}
