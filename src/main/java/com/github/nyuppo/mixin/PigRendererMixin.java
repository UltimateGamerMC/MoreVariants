package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVEntityModelLayers;
import com.github.nyuppo.client.render.MMVPigRenderState;
import com.github.nyuppo.client.render.entity.model.LegacyPigEntityModel;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.variant.MobVariant;
import net.minecraft.client.model.BabyModelPair;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.PigEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.PigEntityModel;
import net.minecraft.client.render.entity.state.PigEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.PigVariant;
import net.minecraft.entity.passive.PigVariants;
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

import java.util.Map;

@Mixin(PigEntityRenderer.class)
public class PigRendererMixin {
    private static final Identifier DEFAULT = Identifier.ofVanilla("textures/entity/pig/temperate_pig.png");

    @Shadow
    private Map<PigVariant.Model, BabyModelPair<PigEntityModel>> modelPairs;

    @Unique
    private static final ThreadLocal<PigEntityRenderState> moremobvariants$renderState = new ThreadLocal<>();

    @Unique
    private BabyModelPair<PigEntityModel> moremobvariants$legacyPair;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(EntityRendererFactory.Context context, CallbackInfo ci) {
        moremobvariants$legacyPair = new BabyModelPair<>(
                new LegacyPigEntityModel(context.getPart(MMVEntityModelLayers.PIG_LEGACY)),
                new LegacyPigEntityModel(context.getPart(MMVEntityModelLayers.PIG_LEGACY_BABY))
        );
    }

    @Inject(method = "updateRenderState", at = @At("RETURN"))
    private void onUpdateRenderState(PigEntity entity, PigEntityRenderState state, float tickDelta, CallbackInfo ci) {
        NbtCompound nbt = MoreMobVariants.getCustomDataNbt(entity);
        MMVPigRenderState ext = (MMVPigRenderState) state;
        String mmvId = nbt.getString(MoreMobVariants.NBT_KEY, "");
        ext.setMmvVariantId(mmvId);
        ext.setMmvMuddy(nbt.getBoolean(MoreMobVariants.MUDDY_NBT_KEY, false));
        if (!mmvId.isEmpty()) {
            state.variant = entity.getRegistryManager().getOrThrow(RegistryKeys.PIG_VARIANT).getOrThrow(PigVariants.TEMPERATE).value();
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void moremobvariants$onRenderHead(PigEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
        moremobvariants$renderState.set(state);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void moremobvariants$onRenderReturn(PigEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
        moremobvariants$renderState.remove();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object moremobvariants$redirectGetModelPair(Map<?, ?> map, Object key) {
        PigEntityRenderState state = moremobvariants$renderState.get();
        if (state instanceof MMVPigRenderState ext && map == this.modelPairs && moremobvariants$legacyPair != null) {
            String mmvId = ext.getMmvVariantId();
            if (mmvId != null && !mmvId.isEmpty() && !mmvId.equals(MoreMobVariants.id("default").toString())) {
                return moremobvariants$legacyPair;
            }
        }
        return map.get(key);
    }

    @Inject(method = "getTexture(Lnet/minecraft/client/render/entity/state/PigEntityRenderState;)Lnet/minecraft/util/Identifier;", at = @At("HEAD"), cancellable = true)
    private void onGetTexture(PigEntityRenderState state, CallbackInfoReturnable<Identifier> ci) {
        String variant = ((MMVPigRenderState) state).getMmvVariantId();
        if (variant != null && !variant.isEmpty()) {
            if (variant.equals(MoreMobVariants.id("default").toString())) {
                ci.setReturnValue(DEFAULT);
            } else {
                String[] split = Variants.splitVariant(variant);
                ci.setReturnValue(Identifier.of(split[0], "textures/entity/pig/" + split[1] + ".png"));
            }
        }
    }
}

