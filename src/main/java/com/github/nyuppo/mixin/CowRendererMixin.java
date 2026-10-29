package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.MoreMobVariantsClient;
import com.github.nyuppo.client.render.MMVEntityModelLayers;
import com.github.nyuppo.client.render.MMVLivingRenderState;
import com.github.nyuppo.client.render.MMVTextures;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.variant.MobVariant;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.AdultAndBabyModelPair;
import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.state.CowRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.CowVariant;
import net.minecraft.world.entity.animal.cow.CowVariants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(CowRenderer.class)
public class CowRendererMixin {
	private static final Identifier DEFAULT = Identifier.withDefaultNamespace("textures/entity/cow/cow_temperate.png");
	private static final Identifier DEFAULT_BABY = Identifier.withDefaultNamespace("textures/entity/cow/cow_temperate_baby.png");
	private static final Identifier LEGACY_DEFAULT = Identifier.fromNamespaceAndPath("moremobvariants", "textures/entity/cow/legacy_reference.png");

	@Shadow
	private Map<CowVariant.ModelType, AdultAndBabyModelPair<CowModel>> models;

	@Unique
	private static final ThreadLocal<CowRenderState> moremobvariants$renderState = new ThreadLocal<>();

	@Unique
	private AdultAndBabyModelPair<CowModel> moremobvariants$legacyPair;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context, CallbackInfo ci) {
		// Babies keep vanilla's baby model (variant skins ship a matching _baby texture), so only adults swap model.
		CowModel legacy = new CowModel(context.bakeLayer(MMVEntityModelLayers.COW_LEGACY));
		moremobvariants$legacyPair = new AdultAndBabyModelPair<>(legacy, legacy);
	}

	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void onExtractRenderState(Cow entity, CowRenderState state, float partialTicks, CallbackInfo ci) {
		String mmvId = MoreMobVariantsClient.VARIANT_CACHE.get(entity.getId());
		if (mmvId == null || mmvId.isEmpty()) {
			CompoundTag nbt = MoreMobVariants.getCustomDataNbt(entity);
			mmvId = nbt.getStringOr(MoreMobVariants.NBT_KEY, "");
		}
		if (mmvId != null && !mmvId.isEmpty()) {
			MoreMobVariantsClient.VARIANT_CACHE.put(entity.getId(), mmvId);
		} else {
			MoreMobVariantsClient.requestVariantIfNeeded(entity);
		}
		MMVLivingRenderState ext = (MMVLivingRenderState) state;
		ext.setMmvVariantId(mmvId != null ? mmvId : "");
		if (!(mmvId == null || mmvId.isEmpty())) {
			state.variant = entity.registryAccess().lookupOrThrow(Registries.COW_VARIANT).getOrThrow(CowVariants.TEMPERATE).value();
		}
		ext.setMmvTextureOverride(null);
		if (entity.hasCustomName()) {
			MobVariant v = Variants.getVariantFromNametag(EntityTypes.COW, entity.getName().getString());
			if (v != null) {
				Identifier id = v.getIdentifier();
				ext.setMmvTextureOverride(Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/entity/cow/" + id.getPath() + ".png"));
				ext.setMmvVariantId(id.toString());
			}
		}
	}

	@Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
	private void onGetTextureLocationHead(CowRenderState state, CallbackInfoReturnable<Identifier> ci) {
		MMVLivingRenderState ext = (MMVLivingRenderState) state;
		Identifier texture = ext.getMmvTextureOverride();
		String variant = ext.getMmvVariantId();
		// "default" cows are drawn as vanilla temperate cows, which vanilla already textures for both ages
		if (texture == null && variant != null && !variant.isEmpty() && !variant.equals(MoreMobVariants.id("default").toString())) {
			String[] split = Variants.splitVariant(variant);
			if (Variants.getVariantNullable(EntityTypes.COW, Identifier.fromNamespaceAndPath(split[0], split[1])) != null) {
				texture = Identifier.fromNamespaceAndPath(split[0], "textures/entity/cow/" + split[1] + ".png");
			}
		}
		if (texture != null) {
			texture = MMVTextures.forAge(texture, state.isBaby);
			if (texture != null) ci.setReturnValue(texture);
		}
	}

	@Inject(method = "getTextureLocation", at = @At("RETURN"))
	private void onGetTextureLocationReturn(CowRenderState state, CallbackInfoReturnable<Identifier> ci) {
		if (ci.getReturnValue() != null && MissingTextureAtlasSprite.getLocation().equals(ci.getReturnValue())) {
			MMVLivingRenderState ext = (MMVLivingRenderState) state;
			String mmvId = ext.getMmvVariantId();
			boolean useLegacyModel = !state.isBaby && mmvId != null && !mmvId.isEmpty() && !mmvId.equals(MoreMobVariants.id("default").toString());
			ci.setReturnValue(useLegacyModel ? LEGACY_DEFAULT : state.isBaby ? DEFAULT_BABY : DEFAULT);
		}
	}

	@Inject(method = "submit", at = @At("HEAD"))
	private void moremobvariants$onSubmitHead(CowRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
		moremobvariants$renderState.set(state);
	}

	@Inject(method = "submit", at = @At("RETURN"))
	private void moremobvariants$onSubmitReturn(CowRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
		moremobvariants$renderState.remove();
	}

	@Redirect(method = "submit", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object moremobvariants$redirectGetModelPair(Map<?, ?> map, Object key) {
		CowRenderState state = moremobvariants$renderState.get();
		if (state instanceof MMVLivingRenderState ext && !state.isBaby && map == this.models && moremobvariants$legacyPair != null) {
			String mmvId = ext.getMmvVariantId();
			if (mmvId != null && !mmvId.isEmpty() && !mmvId.equals(MoreMobVariants.id("default").toString())) {
				return moremobvariants$legacyPair;
			}
		}
		return map.get(key);
	}
}
