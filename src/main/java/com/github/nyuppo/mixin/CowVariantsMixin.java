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
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.CowVariants;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.nbt.NbtCompound;
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

@Mixin(CowEntity.class)
public abstract class CowVariantsMixin extends MobEntityVariantsMixin {
    private MobVariant variant = Variants.getDefaultVariant(EntityType.COW);

    private static void setCowVariantTemperate(CowEntity cow) {
        cow.setVariant(cow.getRegistryManager().getOrThrow(RegistryKeys.COW_VARIANT).getOrThrow(CowVariants.TEMPERATE));
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
                variant = Variants.getVariant(EntityType.COW, Identifier.of(variantStr));
            } else {
                variant = Variants.getVariant(EntityType.COW, MoreMobVariants.id(variantStr));
            }
            setCowVariantTemperate((CowEntity)(Object)this);
        } else {
            Entity e = (Entity)(Object)this;
            World w = e.getEntityWorld();
            if (MoreMobVariants.isTemperateBiome(w.getBiome(((CowEntity)(Object)this).getBlockPos()))) {
                hasModVariant = true;
                Float moonSize = w instanceof ServerWorld ? ((ServerWorld)w).getMoonSize(e.getBlockPos()) : null;
                variant = Variants.getRandomVariant(EntityType.COW, ((CowEntity)(Object)this).getRandom().nextLong(), w.getBiome(((CowEntity)(Object)this).getBlockPos()), null, moonSize);
                setCowVariantTemperate((CowEntity)(Object)this);
                var data = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
                data.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
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
        if (!MoreMobVariants.isTemperateBiome(world.getBiome(((CowEntity)(Object)this).getBlockPos()))) {
            return;
        }
        variant = Variants.getRandomVariant(EntityType.COW, ((CowEntity)(Object)this).getRandom().nextLong(), world.getBiome(((CowEntity)(Object)this).getBlockPos()), null, world.toServerWorld().getMoonSize(((CowEntity)(Object)this).getBlockPos()));
        var data = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
        data.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
        MoreMobVariants.setCustomDataNbt((Entity)(Object)this, data);
        setCowVariantTemperate((CowEntity)(Object)this);
    }

    @Inject(
            method = "createChild(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/PassiveEntity;)Lnet/minecraft/entity/passive/CowEntity;",
            at = @At("RETURN")
    )
    private void onCreateChild(ServerWorld world, PassiveEntity entity, CallbackInfoReturnable<CowEntity> ci) {
        CowEntity child = ci.getReturnValue();
        if (child == null) return;
        if (!MoreMobVariants.isTemperateBiome(world.getBiome(child.getBlockPos()))) return;
        MobVariant childVariant = Variants.getChildVariant(EntityType.COW, world, ((CowEntity)(Object)this), entity);
        setCowVariantTemperate(child);
        var childData = MoreMobVariants.getCustomDataNbt(child);
        childData.putString(MoreMobVariants.NBT_KEY, childVariant.getIdentifier().toString());
        MoreMobVariants.setCustomDataNbt(child, childData);
    }
}
