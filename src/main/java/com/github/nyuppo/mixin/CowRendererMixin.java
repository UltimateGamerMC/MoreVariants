package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.MoreMobVariantsClient;
import com.github.nyuppo.client.render.MMVLivingRenderState;
import com.github.nyuppo.client.render.MMVEntityModelLayers;
import com.github.nyuppo.client.render.entity.model.LegacyCowEntityModel;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.variant.MobVariant;
import net.minecraft.client.model.BabyModelPair;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.CowEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.client.render.entity.state.CowEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.CowVariant;
import net.minecraft.entity.passive.CowVariants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CowEntityRenderer.class)
public class CowRendererMixin {
    private static final Identifier DEFAULT = Identifier.ofVanilla("textures/entity/cow/temperate_cow.png");
    private static final Identifier LEGACY_DEFAULT = Identifier.of("moremobvariants", "textures/entity/cow/legacy_reference.png");

    @Shadow
    private java.util.Map<CowVariant.Model, BabyModelPair<CowEntityModel>> babyModelPairMap;
    @Unique
    private static final ThreadLocal<CowEntityRenderState> moremobvariants$renderState = new ThreadLocal<>();
    @Unique
    private BabyModelPair<CowEntityModel> moremobvariants$legacyPair;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(EntityRendererFactory.Context context, CallbackInfo ci) {
        moremobvariants$legacyPair = new BabyModelPair<>(
                new LegacyCowEntityModel(context.getPart(MMVEntityModelLayers.COW_LEGACY)),
                new LegacyCowEntityModel(context.getPart(MMVEntityModelLayers.COW_LEGACY_BABY)));
    }

    @Inject(method = "updateRenderState", at = @At("RETURN"))
    private void onUpdateRenderState(CowEntity entity, CowEntityRenderState state, float tickDelta, CallbackInfo ci) {
        String mmvId = MoreMobVariantsClient.VARIANT_CACHE.get(entity.getId());
        if (mmvId == null || mmvId.isEmpty()) {
            NbtCompound nbt = MoreMobVariants.getCustomDataNbt(entity);
            mmvId = nbt.getString(MoreMobVariants.NBT_KEY, "");
        }
        if (mmvId != null && !mmvId.isEmpty()) {
            MoreMobVariantsClient.VARIANT_CACHE.put(entity.getId(), mmvId);
        } else {
            MoreMobVariantsClient.requestVariantIfNeeded(entity);
        }
        MMVLivingRenderState ext = (MMVLivingRenderState) state;
        ext.setMmvVariantId(mmvId != null ? mmvId : "");
        if (!(mmvId == null || mmvId.isEmpty())) {
            state.variant = entity.getRegistryManager().getOrThrow(RegistryKeys.COW_VARIANT).getOrThrow(CowVariants.TEMPERATE).value();
        }
        ext.setMmvTextureOverride(null);
        if (entity.hasCustomName()) {
            MobVariant v = Variants.getVariantFromNametag(EntityType.COW, entity.getName().getString());
            if (v != null) {
                Identifier id = v.getIdentifier();
                ext.setMmvTextureOverride(Identifier.of(id.getNamespace(), "textures/entity/cow/" + id.getPath() + ".png"));
                ext.setMmvVariantId(id.toString());
            }
        }
    }

    @Inject(method = "getTexture(Lnet/minecraft/client/render/entity/state/CowEntityRenderState;)Lnet/minecraft/util/Identifier;", at = @At("HEAD"), cancellable = true)
    private void onGetTexture(CowEntityRenderState state, CallbackInfoReturnable<Identifier> ci) {
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
                if (Variants.getVariantNullable(EntityType.COW, Identifier.of(split[0], split[1])) != null) {
                    ci.setReturnValue(Identifier.of(split[0], "textures/entity/cow/" + split[1] + ".png"));
                }
            }
        }
    }

    @Inject(method = "getTexture(Lnet/minecraft/client/render/entity/state/CowEntityRenderState;)Lnet/minecraft/util/Identifier;", at = @At("RETURN"))
    private void onGetTextureReturn(CowEntityRenderState state, CallbackInfoReturnable<Identifier> ci) {
        if (ci.getReturnValue() != null && MissingSprite.getMissingSpriteId().equals(ci.getReturnValue())) {
            MMVLivingRenderState ext = (MMVLivingRenderState) state;
            String mmvId = ext.getMmvVariantId();
            boolean useLegacyModel = mmvId != null && !mmvId.isEmpty() && !mmvId.equals(MoreMobVariants.id("default").toString());
            ci.setReturnValue(useLegacyModel ? LEGACY_DEFAULT : DEFAULT);
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void moremobvariants$onRenderHead(CowEntityRenderState state, MatrixStack matrixStack, OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
        moremobvariants$renderState.set(state);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void moremobvariants$onRenderReturn(CowEntityRenderState state, MatrixStack matrixStack, OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
        moremobvariants$renderState.remove();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object moremobvariants$redirectGetModelPair(java.util.Map<?, ?> map, Object key) {
        CowEntityRenderState state = moremobvariants$renderState.get();
        if (state instanceof MMVLivingRenderState ext && map == this.babyModelPairMap && moremobvariants$legacyPair != null) {
            String mmvId = ext.getMmvVariantId();
            if (mmvId != null && !mmvId.isEmpty() && !mmvId.equals(MoreMobVariants.id("default").toString())) {
                return moremobvariants$legacyPair;
            }
        }
        return map.get(key);
    }
}
