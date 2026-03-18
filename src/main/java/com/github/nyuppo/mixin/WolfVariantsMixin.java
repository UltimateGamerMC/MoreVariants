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
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.passive.WolfVariants;
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
import net.minecraft.entity.passive.WolfVariant;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WolfEntity.class)
public abstract class WolfVariantsMixin extends MobEntityVariantsMixin {
    private MobVariant variant = Variants.getDefaultVariant(EntityType.WOLF);

    @Invoker("setVariant")
    abstract void invokeSetVariant(RegistryEntry<WolfVariant> variant);

    @Override
    protected void onWriteCustomData(WriteView view, CallbackInfo ci) {
        view.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
    }

    @Override
    protected void onReadCustomData(ReadView view, CallbackInfo ci) {
        String variantStr = view.getString(MoreMobVariants.NBT_KEY, "");
        boolean hasModVariant = false;
        if (!variantStr.isEmpty()) {
            hasModVariant = true;
            if (variantStr.contains(":")) {
                variant = Variants.getVariant(EntityType.WOLF, Identifier.of(variantStr));
            } else {
                variant = Variants.getVariant(EntityType.WOLF, MoreMobVariants.id(variantStr));
            }
        } else {
            Entity e = (Entity)(Object)this;
            World w = e.getEntityWorld();
            if (MoreMobVariants.isTemperateBiome(w.getBiome(((WolfEntity)(Object)this).getBlockPos()))) {
                hasModVariant = true;
                Float moonSize = w instanceof ServerWorld ? ((ServerWorld)w).getMoonSize(e.getBlockPos()) : null;
                variant = Variants.getRandomVariant(EntityType.WOLF, ((WolfEntity)(Object)this).getRandom().nextLong(), w.getBiome(((WolfEntity)(Object)this).getBlockPos()), null, moonSize);
            }
        }
        if (!hasModVariant) {
            return;
        }
        invokeSetVariant(((WolfEntity)(Object)this).getRegistryManager().getOrThrow(RegistryKeys.WOLF_VARIANT).getOrThrow(WolfVariants.DEFAULT));
        MinecraftServer server = ((Entity)(Object)this).getEntityWorld().getServer();
        if (server != null) {
            server.getPlayerManager().getPlayerList().forEach((player) -> {
                ServerPlayNetworking.send(player, new ServerRespondBasicVariantPayload(((Entity)(Object)this).getId(), variant.getIdentifier().toString()));
            });
        }
    }

    @Override
    protected void onInitialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, CallbackInfoReturnable<EntityData> ci) {
        if (!MoreMobVariants.isTemperateBiome(world.getBiome(((WolfEntity)(Object)this).getBlockPos()))) {
            return;
        }
        variant = Variants.getRandomVariant(EntityType.WOLF, ((WolfEntity)(Object)this).getRandom().nextLong(), world.getBiome(((WolfEntity)(Object)this).getBlockPos()), null, world.toServerWorld().getMoonSize(((WolfEntity)(Object)this).getBlockPos()));
        var data = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
        data.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
        MoreMobVariants.setCustomDataNbt((Entity)(Object)this, data);
        invokeSetVariant(world.toServerWorld().getRegistryManager().getOrThrow(RegistryKeys.WOLF_VARIANT).getOrThrow(WolfVariants.DEFAULT));
    }

    @Inject(
            method = "createChild(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/PassiveEntity;)Lnet/minecraft/entity/passive/WolfEntity;",
            at = @At("RETURN")
    )
    private void onCreateChild(ServerWorld world, PassiveEntity entity, CallbackInfoReturnable<WolfEntity> ci) {
        WolfEntity child = ci.getReturnValue();
        if (child == null) return;
        if (!MoreMobVariants.isTemperateBiome(world.getBiome(child.getBlockPos()))) return;
        MobVariant variant = Variants.getChildVariant(EntityType.WOLF, world, ((WolfEntity)(Object)this), entity);
        ((WolfVariantsMixin)(Object)child).invokeSetVariant(world.getRegistryManager().getOrThrow(RegistryKeys.WOLF_VARIANT).getOrThrow(WolfVariants.DEFAULT));
        var childData = MoreMobVariants.getCustomDataNbt(child);
        childData.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
        MoreMobVariants.setCustomDataNbt(child, childData);
    }
}
