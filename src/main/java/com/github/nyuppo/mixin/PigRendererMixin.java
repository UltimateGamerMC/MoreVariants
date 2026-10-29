package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVEntityModelLayers;
import com.github.nyuppo.client.render.MMVPigRenderState;
import com.github.nyuppo.client.render.MMVTextures;
import com.github.nyuppo.config.Variants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.AdultAndBabyModelPair;
import net.minecraft.client.model.animal.pig.PigModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.minecraft.world.entity.animal.pig.PigVariants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(PigRenderer.class)
public class PigRendererMixin {
	@Shadow
	private Map<PigVariant.ModelType, AdultAndBabyModelPair<PigModel>> models;

	@Unique
	private static final ThreadLocal<PigRenderState> moremobvariants$renderState = new ThreadLocal<>();

	@Unique
	private AdultAndBabyModelPair<PigModel> moremobvariants$legacyPair;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context, CallbackInfo ci) {
		// Babies keep vanilla's baby model (variant skins ship a matching _baby texture), so only adults swap model.
		PigModel legacy = new PigModel(context.bakeLayer(MMVEntityModelLayers.PIG_LEGACY));
		moremobvariants$legacyPair = new AdultAndBabyModelPair<>(legacy, legacy);
	}

	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void onExtractRenderState(Pig entity, PigRenderState state, float partialTicks, CallbackInfo ci) {
		CompoundTag nbt = MoreMobVariants.getCustomDataNbt(entity);
		MMVPigRenderState ext = (MMVPigRenderState) state;
		String mmvId = nbt.getStringOr(MoreMobVariants.NBT_KEY, "");
		ext.setMmvVariantId(mmvId);
		ext.setMmvMuddy(nbt.getBooleanOr(MoreMobVariants.MUDDY_NBT_KEY, false));
		if (!mmvId.isEmpty()) {
			state.variant = entity.registryAccess().lookupOrThrow(Registries.PIG_VARIANT).getOrThrow(PigVariants.TEMPERATE).value();
		}
	}

	@Inject(method = "submit", at = @At("HEAD"))
	private void moremobvariants$onSubmitHead(PigRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
		moremobvariants$renderState.set(state);
	}

	@Inject(method = "submit", at = @At("RETURN"))
	private void moremobvariants$onSubmitReturn(PigRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
		moremobvariants$renderState.remove();
	}

	@Redirect(method = "submit", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object moremobvariants$redirectGetModelPair(Map<?, ?> map, Object key) {
		PigRenderState state = moremobvariants$renderState.get();
		if (state instanceof MMVPigRenderState ext && !state.isBaby && map == this.models && moremobvariants$legacyPair != null) {
			String mmvId = ext.getMmvVariantId();
			if (mmvId != null && !mmvId.isEmpty() && !mmvId.equals(MoreMobVariants.id("default").toString())) {
				return moremobvariants$legacyPair;
			}
		}
		return map.get(key);
	}

	@Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
	private void onGetTextureLocation(PigRenderState state, CallbackInfoReturnable<Identifier> ci) {
		String variant = ((MMVPigRenderState) state).getMmvVariantId();
		// "default" pigs are drawn as vanilla temperate pigs, which vanilla already textures for both ages
		if (variant != null && !variant.isEmpty() && !variant.equals(MoreMobVariants.id("default").toString())) {
			String[] split = Variants.splitVariant(variant);
			Identifier texture = MMVTextures.forAge(Identifier.fromNamespaceAndPath(split[0], "textures/entity/pig/" + split[1] + ".png"), state.isBaby);
			if (texture != null) ci.setReturnValue(texture);
		}
	}
}
