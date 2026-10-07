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
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.wolf.WolfVariants;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public abstract class WolfVariantsMixin extends MobEntityVariantsMixin {
    private MobVariant variant = Variants.getDefaultVariant(EntityTypes.WOLF);

    @Invoker("setVariant")
    abstract void invokeSetVariant(Holder<WolfVariant> variant);

    @Override
    protected void onWriteCustomData(ValueOutput view, CallbackInfo ci) {
        view.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
    }

    @Override
    protected void onReadCustomData(ValueInput view, CallbackInfo ci) {
        String variantStr = view.getStringOr(MoreMobVariants.NBT_KEY, "");
        boolean hasModVariant = false;
        if (!variantStr.isEmpty()) {
            hasModVariant = true;
            if (variantStr.contains(":")) {
                variant = Variants.getVariant(EntityTypes.WOLF, Identifier.parse(variantStr));
            } else {
                variant = Variants.getVariant(EntityTypes.WOLF, MoreMobVariants.id(variantStr));
            }
        } else {
            Entity e = (Entity)(Object)this;
            Level w = e.level();
            if (MoreMobVariants.isTemperateBiome(w.getBiome(((Wolf)(Object)this).blockPosition()))) {
                hasModVariant = true;
                Float moonSize = w instanceof ServerLevel ? ((ServerLevel)w).getMoonBrightness(e.blockPosition()) : null;
                variant = Variants.getRandomVariant(EntityTypes.WOLF, ((Wolf)(Object)this).getRandom().nextLong(), w.getBiome(((Wolf)(Object)this).blockPosition()), null, moonSize);
            }
        }
        if (!hasModVariant) {
            return;
        }
        invokeSetVariant(((Wolf)(Object)this).registryAccess().lookupOrThrow(Registries.WOLF_VARIANT).getOrThrow(WolfVariants.DEFAULT));
        MinecraftServer server = ((Entity)(Object)this).level().getServer();
        if (server != null) {
            server.getPlayerList().getPlayers().forEach((player) -> {
                ServerPlayNetworking.send(player, new ServerRespondBasicVariantPayload(((Entity)(Object)this).getId(), variant.getIdentifier().toString()));
            });
        }
    }

    @Override
    protected void onFinalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData entityData, CallbackInfoReturnable<SpawnGroupData> ci) {
        if (!MoreMobVariants.isTemperateBiome(world.getBiome(((Wolf)(Object)this).blockPosition()))) {
            return;
        }
        variant = Variants.getRandomVariant(EntityTypes.WOLF, ((Wolf)(Object)this).getRandom().nextLong(), world.getBiome(((Wolf)(Object)this).blockPosition()), null, world.getLevel().getMoonBrightness(((Wolf)(Object)this).blockPosition()));
        var data = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
        data.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
        MoreMobVariants.setCustomDataNbt((Entity)(Object)this, data);
        invokeSetVariant(world.getLevel().registryAccess().lookupOrThrow(Registries.WOLF_VARIANT).getOrThrow(WolfVariants.DEFAULT));
    }

    @Inject(method = "getBreedOffspring", at = @At("RETURN"))
    private void onGetBreedOffspring(ServerLevel world, AgeableMob entity, CallbackInfoReturnable<Wolf> ci) {
        Wolf child = ci.getReturnValue();
        if (child == null) return;
        if (!MoreMobVariants.isTemperateBiome(world.getBiome(child.blockPosition()))) return;
        MobVariant variant = Variants.getChildVariant(EntityTypes.WOLF, world, ((Wolf)(Object)this), entity);
        ((WolfVariantsMixin)(Object)child).invokeSetVariant(world.registryAccess().lookupOrThrow(Registries.WOLF_VARIANT).getOrThrow(WolfVariants.DEFAULT));
        var childData = MoreMobVariants.getCustomDataNbt(child);
        childData.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
        MoreMobVariants.setCustomDataNbt(child, childData);
    }
}
