package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.config.VariantSettings;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.networking.ServerRespondVariantPayload;
import com.github.nyuppo.variant.MobVariant;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.PigVariant;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.PigVariants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.Nullable;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PigEntity.class)
public abstract class PigVariantsMixin extends MobEntityVariantsMixin {
    private MobVariant variant = Variants.getDefaultVariant(EntityType.PIG);

    @Invoker("setVariant")
    abstract void invokeSetVariant(RegistryEntry<PigVariant> variant);
    private boolean isMuddy = false;
    private int muddyTimeLeft = -1;

    @Override
    protected void onWriteCustomData(WriteView view, CallbackInfo ci) {
        NbtCompound nbt = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
        if (nbt.contains(MoreMobVariants.NBT_KEY)) {
            view.putString(MoreMobVariants.NBT_KEY, nbt.getString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString()));
        }
        view.putBoolean(MoreMobVariants.MUDDY_NBT_KEY, isMuddy);
        view.putInt(MoreMobVariants.MUDDY_TIMEOUT_NBT_KEY, muddyTimeLeft);
    }

    @Override
    protected void onReadCustomData(ReadView view, CallbackInfo ci) {
        String variantStr = view.getString(MoreMobVariants.NBT_KEY, "");
        isMuddy = view.getBoolean(MoreMobVariants.MUDDY_NBT_KEY, false);
        muddyTimeLeft = view.getInt(MoreMobVariants.MUDDY_TIMEOUT_NBT_KEY, -1);
        boolean hasModVariant = false;
        if (!variantStr.isEmpty()) {
            hasModVariant = true;
            if (variantStr.contains(":")) {
                variant = Variants.getVariant(EntityType.PIG, Identifier.of(variantStr));
            } else {
                variant = Variants.getVariant(EntityType.PIG, MoreMobVariants.id(variantStr));
            }
            invokeSetVariant(((PigEntity)(Object)this).getRegistryManager().getOrThrow(RegistryKeys.PIG_VARIANT).getOrThrow(PigVariants.TEMPERATE));
        } else {
            Entity e = (Entity)(Object)this;
            if (MoreMobVariants.isTemperateBiome(e.getEntityWorld().getBiome(((PigEntity)(Object)this).getBlockPos()))) {
                hasModVariant = true;
                variant = Variants.getRandomVariant(EntityType.PIG, ((PigEntity)(Object)this).getRandom().nextLong(), e.getEntityWorld().getBiome(e.getBlockPos()), null, e.getEntityWorld() instanceof ServerWorld ? ((ServerWorld)e.getEntityWorld()).getMoonSize(e.getBlockPos()) : null);
                invokeSetVariant(((PigEntity)(Object)this).getRegistryManager().getOrThrow(RegistryKeys.PIG_VARIANT).getOrThrow(PigVariants.TEMPERATE));
                var data = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
                data.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
                data.putBoolean(MoreMobVariants.MUDDY_NBT_KEY, isMuddy);
                data.putInt(MoreMobVariants.MUDDY_TIMEOUT_NBT_KEY, muddyTimeLeft);
                MoreMobVariants.setCustomDataNbt((Entity)(Object)this, data);
            }
        }
        if (hasModVariant) {
            MinecraftServer server = ((Entity)(Object)this).getEntityWorld().getServer();
            if (server != null) {
                server.getPlayerManager().getPlayerList().forEach((player) -> {
                    ServerPlayNetworking.send(player, new ServerRespondVariantPayload(((Entity)(Object)this).getId(), variant.getIdentifier().toString(), isMuddy, muddyTimeLeft, ""));
                });
            }
        }
    }

    @Override
    protected void onInitialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, CallbackInfoReturnable<EntityData> ci) {
        if (!MoreMobVariants.isTemperateBiome(world.getBiome(((PigEntity)(Object)this).getBlockPos()))) return;
        variant = Variants.getRandomVariant(EntityType.PIG, ((PigEntity)(Object)this).getRandom().nextLong(), world.getBiome(((PigEntity)(Object)this).getBlockPos()), null, world.toServerWorld().getMoonSize(((PigEntity)(Object)this).getBlockPos()));
        if (world.getBiome(((PigEntity)(Object)this).getBlockPos()).isIn(BiomeTags.RUINED_PORTAL_SWAMP_HAS_STRUCTURE) && world.getRandom().nextDouble() < 0.02) {
            isMuddy = true;
        }
        var data = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
        data.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
        data.putBoolean(MoreMobVariants.MUDDY_NBT_KEY, isMuddy);
        data.putInt(MoreMobVariants.MUDDY_TIMEOUT_NBT_KEY, muddyTimeLeft);
        MoreMobVariants.setCustomDataNbt((Entity)(Object)this, data);
        invokeSetVariant(world.toServerWorld().getRegistryManager().getOrThrow(RegistryKeys.PIG_VARIANT).getOrThrow(PigVariants.TEMPERATE));
    }

    @Override
    protected void onTick(CallbackInfo ci) {
        // Handle muddy pigs
        if (VariantSettings.getEnableMuddyPigs()) {
            int muddyPigTimeout = VariantSettings.getMuddyPigTimeout();

            if (muddyTimeLeft == -1) {
                if (((PigEntity)(Object)this).getEntityWorld().getBlockState(((PigEntity)(Object)this).getBlockPos()).isIn(MoreMobVariants.PIG_MUD_BLOCKS) || ((PigEntity)(Object)this).getEntityWorld().getBlockState(((PigEntity)(Object)this).getBlockPos().down()).isIn(MoreMobVariants.PIG_MUD_BLOCKS)) {
                    isMuddy = true;
                    if (muddyPigTimeout > 0 ) {
                        muddyTimeLeft = 20 * muddyPigTimeout;
                    }
                } else if (((PigEntity)(Object)this).isTouchingWaterOrRain()) {
                    isMuddy = false;
                    muddyTimeLeft = -1;
                }
            }

            if (muddyPigTimeout > 0 && muddyTimeLeft > 0) {
                muddyTimeLeft--;
                if (muddyTimeLeft == 0) {
                    isMuddy = false;
                    muddyTimeLeft = -1;
                }
            }
        }
    }

    @Inject(
            method = "createChild(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/PassiveEntity;)Lnet/minecraft/entity/passive/PigEntity;",
            at = @At("RETURN")
    )
    private void onCreateChild(ServerWorld world, PassiveEntity entity, CallbackInfoReturnable<PigEntity> ci) {
        PigEntity child = ci.getReturnValue();
        if (child == null) return;
        if (!MoreMobVariants.isTemperateBiome(world.getBiome(child.getBlockPos()))) return;
        MobVariant childVariant = Variants.getChildVariant(EntityType.PIG, world, ((PigEntity)(Object)this), entity);
        ((PigVariantsMixin)(Object)child).invokeSetVariant(world.getRegistryManager().getOrThrow(RegistryKeys.PIG_VARIANT).getOrThrow(PigVariants.TEMPERATE));
        var childData = MoreMobVariants.getCustomDataNbt(child);
        childData.putString(MoreMobVariants.NBT_KEY, childVariant.getIdentifier().toString());
        childData.putBoolean(MoreMobVariants.MUDDY_NBT_KEY, false);
        childData.putInt(MoreMobVariants.MUDDY_TIMEOUT_NBT_KEY, -1);
        MoreMobVariants.setCustomDataNbt(child, childData);
    }
}