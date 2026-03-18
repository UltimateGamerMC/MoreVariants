package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.MoreMobVariantsClient;
import com.github.nyuppo.client.render.MMVLivingRenderState;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.util.MMVTrackedData;
import com.github.nyuppo.variant.MobVariant;
import net.minecraft.client.render.entity.ChickenEntityRenderer;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.ChickenVariants;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.render.entity.state.ChickenEntityRenderState;

@Mixin(ChickenEntityRenderer.class)
public class ChickenRendererMixin {
    private static final Identifier DEFAULT = Identifier.ofVanilla("textures/entity/chicken/temperate_chicken.png");

    @Inject(method = "updateRenderState", at = @At("RETURN"))
    private void onUpdateRenderState(ChickenEntity entity, ChickenEntityRenderState state, float tickDelta, CallbackInfo ci) {
        String variantId = MoreMobVariantsClient.VARIANT_CACHE.get(entity.getId());
        if (variantId == null || variantId.isEmpty()) {
            variantId = entity.getDataTracker().get(MMVTrackedData.CHICKEN_VARIANT_ID);
        }
        if (variantId == null || variantId.isEmpty()) {
            NbtCompound nbt = MoreMobVariants.getCustomDataNbt(entity);
            variantId = nbt.getString(MoreMobVariants.NBT_KEY, "");
        }
        if (variantId != null && !variantId.isEmpty()) {
            MoreMobVariantsClient.VARIANT_CACHE.put(entity.getId(), variantId);
        } else {
            MoreMobVariantsClient.requestVariantIfNeeded(entity);
        }
        MMVLivingRenderState ext = (MMVLivingRenderState) state;
        ext.setMmvVariantId(variantId);
        ext.setMmvTextureOverride(null);
        if (variantId != null && !variantId.isEmpty()) {
            state.variant = entity.getRegistryManager().getOrThrow(RegistryKeys.CHICKEN_VARIANT).getOrThrow(ChickenVariants.TEMPERATE).value();
        }
        if (entity.hasCustomName()) {
            MobVariant v = Variants.getVariantFromNametag(EntityType.CHICKEN, entity.getName().getString());
            if (v != null) {
                Identifier id = v.getIdentifier();
                ext.setMmvTextureOverride(Identifier.of(id.getNamespace(), "textures/entity/chicken/" + id.getPath() + ".png"));
            }
        }
    }

    @Inject(method = "getTexture(Lnet/minecraft/client/render/entity/state/ChickenEntityRenderState;)Lnet/minecraft/util/Identifier;", at = @At("HEAD"), cancellable = true)
    private void onGetTexture(ChickenEntityRenderState state, CallbackInfoReturnable<Identifier> ci) {
        MMVLivingRenderState ext = (MMVLivingRenderState) state;
        if (ext.getMmvTextureOverride() != null) {
            ci.setReturnValue(ext.getMmvTextureOverride());
            return;
        }
        String variant = ext.getMmvVariantId();
        if (variant != null && !variant.isEmpty()) {
            if (variant.equals(MoreMobVariants.id("default").toString())) {
                ci.setReturnValue(DEFAULT);
            } else {
                String[] split = Variants.splitVariant(variant);
                if (Variants.getVariantNullable(EntityType.CHICKEN, Identifier.of(split[0], split[1])) != null) {
                    ci.setReturnValue(Identifier.of(split[0], "textures/entity/chicken/" + split[1] + ".png"));
                }
            }
        }
    }

    @Inject(method = "getTexture(Lnet/minecraft/client/render/entity/state/ChickenEntityRenderState;)Lnet/minecraft/util/Identifier;", at = @At("RETURN"))
    private void onGetTextureReturn(ChickenEntityRenderState state, CallbackInfoReturnable<Identifier> ci) {
        if (ci.getReturnValue() != null && MissingSprite.getMissingSpriteId().equals(ci.getReturnValue())) {
            ci.setReturnValue(DEFAULT);
        }
    }
}
