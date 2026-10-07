package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVLivingRenderState;
import com.github.nyuppo.config.Variants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.SpiderEyesLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EyesLayer.class)
public class CustomEyesFeatureMixin {

	@Redirect(
		method = "submit",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/EyesLayer;renderType()Lnet/minecraft/client/renderer/rendertype/RenderType;")
	)
	// SpiderEyesLayer no longer overrides submit in 26.3, so hook the shared EyesLayer and only touch spiders.
	private RenderType mixinEyesTexture(EyesLayer<?, ?> instance, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, EntityRenderState state, float yRot, float xRot) {
		if (!(instance instanceof SpiderEyesLayer<?>) || !(state instanceof MMVLivingRenderState ext)) {
			return instance.renderType();
		}
		String variantStr = ext.getMmvVariantId();
		if (variantStr == null || variantStr.isEmpty()) {
			return instance.renderType();
		}
		int colon = variantStr.indexOf(':');
		Identifier variantId = colon >= 0 ? Identifier.fromNamespaceAndPath(variantStr.substring(0, colon), variantStr.substring(colon + 1)) : MoreMobVariants.id(variantStr);
		if (!Variants.getVariant(EntityTypes.SPIDER, variantId).hasCustomEyes()) {
			return instance.renderType();
		}
		Identifier eyesTexture = Identifier.fromNamespaceAndPath(variantId.getNamespace(), "textures/entity/spider/eyes/" + variantId.getPath() + ".png");
		return RenderTypes.eyes(eyesTexture);
	}
}
