package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.networking.ServerRespondBasicVariantPayload;
import com.github.nyuppo.variant.MobVariant;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.CatVariants;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.StructureTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Cat.class)
public abstract class CatVariantsMixin extends MobEntityVariantsMixin {
    private MobVariant variant = Variants.getDefaultVariant(EntityType.CAT);

    @Invoker("setVariant")
    abstract void invokeSetVariant(Holder<CatVariant> variant);

    @Override
    protected void onWriteCustomData(ValueOutput view, CallbackInfo ci) {
        view.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
    }

    @Override
    protected void onReadCustomData(ValueInput view, CallbackInfo ci) {
        String variantStr = view.getStringOr(MoreMobVariants.NBT_KEY, "");
        if (!variantStr.isEmpty()) {
            if (variantStr.contains(":")) {
                variant = Variants.getVariant(EntityType.CAT, Identifier.parse(variantStr));
            } else {
                variant = Variants.getVariant(EntityType.CAT, MoreMobVariants.id(variantStr));
            }
        } else {
            Entity e = (Entity)(Object)this;
            Level w = e.level();
            Float moonSize = w instanceof ServerLevel ? ((ServerLevel)w).getMoonBrightness(e.blockPosition()) : null;
            variant = Variants.getRandomVariant(EntityType.CAT, ((Cat)(Object)this).getRandom().nextLong(), w.getBiome(((Cat)(Object)this).blockPosition()), null, moonSize);
        }
        invokeSetVariant(((Cat)(Object)this).registryAccess().lookupOrThrow(Registries.CAT_VARIANT).getOrThrow(CatVariants.TABBY));
        MinecraftServer server = ((Entity)(Object)this).level().getServer();
        if (server != null) {
            server.getPlayerList().getPlayers().forEach((player) -> {
                ServerPlayNetworking.send(player, new ServerRespondBasicVariantPayload(((Entity)(Object)this).getId(), variant.getIdentifier().toString()));
            });
        }
    }

    @Override
    protected void onFinalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData entityData, CallbackInfoReturnable<SpawnGroupData> ci) {
        variant = Variants.getRandomVariant(EntityType.CAT, ((Cat)(Object)this).getRandom().nextLong(), world.getBiome(((Cat)(Object)this).blockPosition()), null, world.getLevel().getMoonBrightness(((Cat)(Object)this).blockPosition()));
        if (world.getLevel().structureManager().getStructureWithPieceAt(((Cat) (Object) this).blockPosition(), StructureTags.CATS_SPAWN_AS_BLACK).isValid()) {
            MobVariant allBlack = Variants.getVariantNullable(EntityType.CAT, Identifier.withDefaultNamespace("all_black"));
            if (allBlack != null) {
                variant = allBlack;
                ((Cat) (Object) this).setPersistenceRequired();
            }
        }
        var data = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
        data.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
        MoreMobVariants.setCustomDataNbt((Entity)(Object)this, data);
        invokeSetVariant(world.getLevel().registryAccess().lookupOrThrow(Registries.CAT_VARIANT).getOrThrow(CatVariants.TABBY));
    }

    @Inject(method = "getBreedOffspring", at = @At("RETURN"))
    private void onGetBreedOffspring(ServerLevel world, AgeableMob entity, CallbackInfoReturnable<Cat> ci) {
        Cat child = ci.getReturnValue();
        MobVariant variant = Variants.getChildVariant(EntityType.CAT, world, ((Cat)(Object)this), entity);
        ((CatVariantsMixin)(Object)child).invokeSetVariant(world.registryAccess().lookupOrThrow(Registries.CAT_VARIANT).getOrThrow(CatVariants.TABBY));
        var childData = MoreMobVariants.getCustomDataNbt(child);
        childData.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
        MoreMobVariants.setCustomDataNbt(child, childData);
    }
}