package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.config.VariantSettings;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.networking.ServerRespondVariantPayload;
import com.github.nyuppo.variant.MobVariant;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.pig.PigVariants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.Level;
import net.minecraft.tags.BiomeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Pig.class)
public abstract class PigVariantsMixin extends MobEntityVariantsMixin {
    private MobVariant variant = Variants.getDefaultVariant(EntityType.PIG);

    @Invoker("setVariant")
    abstract void invokeSetVariant(Holder<PigVariant> variant);
    private boolean isMuddy = false;
    private int muddyTimeLeft = -1;

    @Override
    protected void onWriteCustomData(ValueOutput view, CallbackInfo ci) {
        CompoundTag nbt = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
        if (nbt.contains(MoreMobVariants.NBT_KEY)) {
            view.putString(MoreMobVariants.NBT_KEY, nbt.getStringOr(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString()));
        }
        view.putBoolean(MoreMobVariants.MUDDY_NBT_KEY, isMuddy);
        view.putInt(MoreMobVariants.MUDDY_TIMEOUT_NBT_KEY, muddyTimeLeft);
    }

    @Override
    protected void onReadCustomData(ValueInput view, CallbackInfo ci) {
        String variantStr = view.getStringOr(MoreMobVariants.NBT_KEY, "");
        isMuddy = view.getBooleanOr(MoreMobVariants.MUDDY_NBT_KEY, false);
        muddyTimeLeft = view.getIntOr(MoreMobVariants.MUDDY_TIMEOUT_NBT_KEY, -1);
        boolean hasModVariant = false;
        if (!variantStr.isEmpty()) {
            hasModVariant = true;
            if (variantStr.contains(":")) {
                variant = Variants.getVariant(EntityType.PIG, Identifier.parse(variantStr));
            } else {
                variant = Variants.getVariant(EntityType.PIG, MoreMobVariants.id(variantStr));
            }
            invokeSetVariant(((Pig)(Object)this).registryAccess().lookupOrThrow(Registries.PIG_VARIANT).getOrThrow(PigVariants.TEMPERATE));
        } else {
            Entity e = (Entity)(Object)this;
            if (MoreMobVariants.isTemperateBiome(e.level().getBiome(((Pig)(Object)this).blockPosition()))) {
                hasModVariant = true;
                variant = Variants.getRandomVariant(EntityType.PIG, ((Pig)(Object)this).getRandom().nextLong(), e.level().getBiome(e.blockPosition()), null, e.level() instanceof ServerLevel ? ((ServerLevel)e.level()).getMoonBrightness(e.blockPosition()) : null);
                invokeSetVariant(((Pig)(Object)this).registryAccess().lookupOrThrow(Registries.PIG_VARIANT).getOrThrow(PigVariants.TEMPERATE));
                var data = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
                data.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
                data.putBoolean(MoreMobVariants.MUDDY_NBT_KEY, isMuddy);
                data.putInt(MoreMobVariants.MUDDY_TIMEOUT_NBT_KEY, muddyTimeLeft);
                MoreMobVariants.setCustomDataNbt((Entity)(Object)this, data);
            }
        }
        if (hasModVariant) {
            MinecraftServer server = ((Entity)(Object)this).level().getServer();
            if (server != null) {
                server.getPlayerList().getPlayers().forEach((player) -> {
                    ServerPlayNetworking.send(player, new ServerRespondVariantPayload(((Entity)(Object)this).getId(), variant.getIdentifier().toString(), isMuddy, muddyTimeLeft, ""));
                });
            }
        }
    }

    @Override
    protected void onFinalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData entityData, CallbackInfoReturnable<SpawnGroupData> ci) {
        if (!MoreMobVariants.isTemperateBiome(world.getBiome(((Pig)(Object)this).blockPosition()))) return;
        variant = Variants.getRandomVariant(EntityType.PIG, ((Pig)(Object)this).getRandom().nextLong(), world.getBiome(((Pig)(Object)this).blockPosition()), null, world.getLevel().getMoonBrightness(((Pig)(Object)this).blockPosition()));
        if (world.getBiome(((Pig)(Object)this).blockPosition()).is(BiomeTags.HAS_RUINED_PORTAL_SWAMP) && world.getRandom().nextDouble() < 0.02) {
            isMuddy = true;
        }
        var data = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
        data.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
        data.putBoolean(MoreMobVariants.MUDDY_NBT_KEY, isMuddy);
        data.putInt(MoreMobVariants.MUDDY_TIMEOUT_NBT_KEY, muddyTimeLeft);
        MoreMobVariants.setCustomDataNbt((Entity)(Object)this, data);
        invokeSetVariant(world.getLevel().registryAccess().lookupOrThrow(Registries.PIG_VARIANT).getOrThrow(PigVariants.TEMPERATE));
    }

    @Override
    protected void onTick(CallbackInfo ci) {
        // Handle muddy pigs
        if (VariantSettings.getEnableMuddyPigs()) {
            int muddyPigTimeout = VariantSettings.getMuddyPigTimeout();

            if (muddyTimeLeft == -1) {
                if (((Pig)(Object)this).level().getBlockState(((Pig)(Object)this).blockPosition()).getBlock().builtInRegistryHolder().is(MoreMobVariants.PIG_MUD_BLOCKS) || ((Pig)(Object)this).level().getBlockState(((Pig)(Object)this).blockPosition().below()).getBlock().builtInRegistryHolder().is(MoreMobVariants.PIG_MUD_BLOCKS)) {
                    isMuddy = true;
                    if (muddyPigTimeout > 0 ) {
                        muddyTimeLeft = 20 * muddyPigTimeout;
                    }
                } else if (((Pig)(Object)this).isInWaterOrRain()) {
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

    @Inject(method = "getBreedOffspring", at = @At("RETURN"))
    private void onGetBreedOffspring(ServerLevel world, AgeableMob entity, CallbackInfoReturnable<Pig> ci) {
        Pig child = ci.getReturnValue();
        if (child == null) return;
        if (!MoreMobVariants.isTemperateBiome(world.getBiome(child.blockPosition()))) return;
        MobVariant childVariant = Variants.getChildVariant(EntityType.PIG, world, ((Pig)(Object)this), entity);
        ((PigVariantsMixin)(Object)child).invokeSetVariant(world.registryAccess().lookupOrThrow(Registries.PIG_VARIANT).getOrThrow(PigVariants.TEMPERATE));
        var childData = MoreMobVariants.getCustomDataNbt(child);
        childData.putString(MoreMobVariants.NBT_KEY, childVariant.getIdentifier().toString());
        childData.putBoolean(MoreMobVariants.MUDDY_NBT_KEY, false);
        childData.putInt(MoreMobVariants.MUDDY_TIMEOUT_NBT_KEY, -1);
        MoreMobVariants.setCustomDataNbt(child, childData);
    }
}