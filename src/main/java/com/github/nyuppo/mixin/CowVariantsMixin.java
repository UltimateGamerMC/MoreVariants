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
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.CowVariants;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Cow.class)
public abstract class CowVariantsMixin extends MobEntityVariantsMixin {
    private MobVariant variant = Variants.getDefaultVariant(EntityTypes.COW);

    private static void setCowVariantTemperate(Cow cow) {
        cow.setVariant(cow.registryAccess().lookupOrThrow(Registries.COW_VARIANT).getOrThrow(CowVariants.TEMPERATE));
    }

    @Override
    protected void onWriteCustomData(ValueOutput view, CallbackInfo ci) {
        CompoundTag nbt = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
        if (nbt.contains(MoreMobVariants.NBT_KEY)) {
            view.putString(MoreMobVariants.NBT_KEY, nbt.getStringOr(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString()));
        }
    }

    @Override
    protected void onReadCustomData(ValueInput view, CallbackInfo ci) {
        String variantStr = view.getStringOr(MoreMobVariants.NBT_KEY, "");
        boolean hasModVariant = false;
        if (!variantStr.isEmpty()) {
            hasModVariant = true;
            if (variantStr.contains(":")) {
                variant = Variants.getVariant(EntityTypes.COW, Identifier.parse(variantStr));
            } else {
                variant = Variants.getVariant(EntityTypes.COW, MoreMobVariants.id(variantStr));
            }
            setCowVariantTemperate((Cow)(Object)this);
            MoreMobVariants.storeVariant((Entity)(Object)this, variant.getIdentifier().toString());
        } else {
            Entity e = (Entity)(Object)this;
            Level w = e.level();
            if (MoreMobVariants.isTemperateBiome(w.getBiome(((Cow)(Object)this).blockPosition()))) {
                hasModVariant = true;
                Float moonSize = w instanceof ServerLevel ? ((ServerLevel)w).getMoonBrightness(e.blockPosition()) : null;
                variant = Variants.getRandomVariant(EntityTypes.COW, ((Cow)(Object)this).getRandom().nextLong(), w.getBiome(((Cow)(Object)this).blockPosition()), null, moonSize);
                setCowVariantTemperate((Cow)(Object)this);
                var data = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
                data.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
                MoreMobVariants.setCustomDataNbt((Entity)(Object)this, data);
            }
        }
        if (hasModVariant) {
            MinecraftServer server = ((Entity)(Object)this).level().getServer();
            if (server != null) {
                server.getPlayerList().getPlayers().forEach((player) -> {
                    ServerPlayNetworking.send(player, new ServerRespondBasicVariantPayload(((Entity)(Object)this).getId(), variant.getIdentifier().toString()));
                });
            }
        }
    }

    @Override
    protected void onFinalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData entityData, CallbackInfoReturnable<SpawnGroupData> ci) {
        if (!MoreMobVariants.isTemperateBiome(world.getBiome(((Cow)(Object)this).blockPosition()))) {
            return;
        }
        variant = Variants.getRandomVariant(EntityTypes.COW, ((Cow)(Object)this).getRandom().nextLong(), world.getBiome(((Cow)(Object)this).blockPosition()), null, world.getLevel().getMoonBrightness(((Cow)(Object)this).blockPosition()));
        var data = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
        data.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
        MoreMobVariants.setCustomDataNbt((Entity)(Object)this, data);
        setCowVariantTemperate((Cow)(Object)this);
    }

    @Inject(method = "getBreedOffspring", at = @At("RETURN"))
    private void onGetBreedOffspring(ServerLevel world, AgeableMob entity, CallbackInfoReturnable<Cow> ci) {
        Cow child = ci.getReturnValue();
        if (child == null) return;
        if (!MoreMobVariants.isTemperateBiome(world.getBiome(child.blockPosition()))) return;
        MobVariant childVariant = Variants.getChildVariant(EntityTypes.COW, world, ((Cow)(Object)this), entity);
        setCowVariantTemperate(child);
        var childData = MoreMobVariants.getCustomDataNbt(child);
        childData.putString(MoreMobVariants.NBT_KEY, childVariant.getIdentifier().toString());
        MoreMobVariants.setCustomDataNbt(child, childData);
    }
}
