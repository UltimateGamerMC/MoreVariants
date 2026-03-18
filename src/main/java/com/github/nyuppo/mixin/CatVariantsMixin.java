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
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.CatVariants;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.passive.CatVariant;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CatEntity.class)
public abstract class CatVariantsMixin extends MobEntityVariantsMixin {
    private MobVariant variant = Variants.getDefaultVariant(EntityType.CAT);

    @Invoker("setVariant")
    abstract void invokeSetVariant(RegistryEntry<CatVariant> variant);

    @Override
    protected void onWriteCustomData(WriteView view, CallbackInfo ci) {
        view.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
    }

    @Override
    protected void onReadCustomData(ReadView view, CallbackInfo ci) {
        String variantStr = view.getString(MoreMobVariants.NBT_KEY, "");
        if (!variantStr.isEmpty()) {
            if (variantStr.contains(":")) {
                variant = Variants.getVariant(EntityType.CAT, Identifier.of(variantStr));
            } else {
                variant = Variants.getVariant(EntityType.CAT, MoreMobVariants.id(variantStr));
            }
        } else {
            Entity e = (Entity)(Object)this;
            World w = e.getEntityWorld();
            Float moonSize = w instanceof ServerWorld ? ((ServerWorld)w).getMoonSize(e.getBlockPos()) : null;
            variant = Variants.getRandomVariant(EntityType.CAT, ((CatEntity)(Object)this).getRandom().nextLong(), w.getBiome(((CatEntity)(Object)this).getBlockPos()), null, moonSize);
        }
        invokeSetVariant(((CatEntity)(Object)this).getRegistryManager().getOrThrow(RegistryKeys.CAT_VARIANT).getOrThrow(CatVariants.TABBY));
        MinecraftServer server = ((Entity)(Object)this).getEntityWorld().getServer();
        if (server != null) {
            server.getPlayerManager().getPlayerList().forEach((player) -> {
                ServerPlayNetworking.send(player, new ServerRespondBasicVariantPayload(((Entity)(Object)this).getId(), variant.getIdentifier().toString()));
            });
        }
    }

    @Override
    protected void onInitialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, CallbackInfoReturnable<EntityData> ci) {
        variant = Variants.getRandomVariant(EntityType.CAT, ((CatEntity)(Object)this).getRandom().nextLong(), world.getBiome(((CatEntity)(Object)this).getBlockPos()), null, world.toServerWorld().getMoonSize(((CatEntity)(Object)this).getBlockPos()));
        if (world.toServerWorld().getStructureAccessor().getStructureContaining(((CatEntity) (Object) this).getBlockPos(), StructureTags.CATS_SPAWN_AS_BLACK).hasChildren()) {
            MobVariant allBlack = Variants.getVariantNullable(EntityType.CAT, Identifier.ofVanilla("all_black"));
            if (allBlack != null) {
                variant = allBlack;
                ((CatEntity) (Object) this).setPersistent();
            }
        }
        var data = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
        data.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
        MoreMobVariants.setCustomDataNbt((Entity)(Object)this, data);
        invokeSetVariant(world.toServerWorld().getRegistryManager().getOrThrow(RegistryKeys.CAT_VARIANT).getOrThrow(CatVariants.TABBY));
    }

    @Inject(
            method = "createChild(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/PassiveEntity;)Lnet/minecraft/entity/passive/CatEntity;",
            at = @At("RETURN")
    )
    private void onCreateChild(ServerWorld world, PassiveEntity entity, CallbackInfoReturnable<CatEntity> ci) {
        CatEntity child = ci.getReturnValue();
        MobVariant variant = Variants.getChildVariant(EntityType.CAT, world, ((CatEntity)(Object)this), entity);
        ((CatVariantsMixin)(Object)child).invokeSetVariant(world.getRegistryManager().getOrThrow(RegistryKeys.CAT_VARIANT).getOrThrow(CatVariants.TABBY));
        var childData = MoreMobVariants.getCustomDataNbt(child);
        childData.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
        MoreMobVariants.setCustomDataNbt(child, childData);
    }
}