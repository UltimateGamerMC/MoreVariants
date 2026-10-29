package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.MoreMobVariantsClient;
import com.github.nyuppo.client.render.MMVLivingRenderState;
import com.github.nyuppo.client.render.MMVTextures;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.util.MMVTrackedData;
import com.github.nyuppo.variant.MobVariant;
import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.chicken.ChickenVariants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChickenRenderer.class)
public class ChickenRendererMixin {
	private static final Identifier DEFAULT = Identifier.withDefaultNamespace("textures/entity/chicken/chicken_temperate.png");
	private static final Identifier DEFAULT_BABY = Identifier.withDefaultNamespace("textures/entity/chicken/chicken_temperate_baby.png");

	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void onExtractRenderState(Chicken entity, ChickenRenderState state, float partialTicks, CallbackInfo ci) {
		String variantId = MoreMobVariantsClient.VARIANT_CACHE.get(entity.getId());
		if (variantId == null || variantId.isEmpty()) {
			variantId = entity.getEntityData().get(MMVTrackedData.CHICKEN_VARIANT_ID);
		}
		if (variantId == null || variantId.isEmpty()) {
			CompoundTag nbt = MoreMobVariants.getCustomDataNbt(entity);
			variantId = nbt.getStringOr(MoreMobVariants.NBT_KEY, "");
		}
		if (variantId != null && !variantId.isEmpty()) {
			MoreMobVariantsClient.VARIANT_CACHE.put(entity.getId(), variantId);
		} else {
			MoreMobVariantsClient.requestVariantIfNeeded(entity);
		}
		MMVLivingRenderState ext = (MMVLivingRenderState) state;
		ext.setMmvVariantId(variantId != null ? variantId : "");
		ext.setMmvTextureOverride(null);
		if (variantId != null && !variantId.isEmpty()) {
			state.variant = entity.registryAccess().lookupOrThrow(Registries.CHICKEN_VARIANT).getOrThrow(ChickenVariants.TEMPERATE).value();
		}
		if (entity.hasCustomName()) {
			MobVariant v = Variants.getVariantFromNametag(EntityTypes.CHICKEN, entity.getName().getString());
			if (v != null) {
				Identifier id = v.getIdentifier();
				ext.setMmvTextureOverride(Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/entity/chicken/" + id.getPath() + ".png"));
			}
		}
	}

	@Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
	private void onGetTextureLocationHead(ChickenRenderState state, CallbackInfoReturnable<Identifier> ci) {
		MMVLivingRenderState ext = (MMVLivingRenderState) state;
		Identifier texture = ext.getMmvTextureOverride();
		String variant = ext.getMmvVariantId();
		// "default" chickens are drawn as vanilla temperate chickens, which vanilla already textures for both ages
		if (texture == null && variant != null && !variant.isEmpty() && !variant.equals(MoreMobVariants.id("default").toString())) {
			String[] split = Variants.splitVariant(variant);
			if (Variants.getVariantNullable(EntityTypes.CHICKEN, Identifier.fromNamespaceAndPath(split[0], split[1])) != null) {
				texture = Identifier.fromNamespaceAndPath(split[0], "textures/entity/chicken/" + split[1] + ".png");
			}
		}
		if (texture != null) {
			texture = MMVTextures.forAge(texture, state.isBaby);
			if (texture != null) ci.setReturnValue(texture);
		}
	}

	@Inject(method = "getTextureLocation", at = @At("RETURN"))
	private void onGetTextureLocationReturn(ChickenRenderState state, CallbackInfoReturnable<Identifier> ci) {
		if (ci.getReturnValue() != null && MissingTextureAtlasSprite.getLocation().equals(ci.getReturnValue())) {
			ci.setReturnValue(state.isBaby ? DEFAULT_BABY : DEFAULT);
		}
	}
}
