package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVSheepRenderState;
import com.github.nyuppo.config.Variants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.SheepWoolLayer;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SheepWoolLayer.class)
public class SheepWoolFeatureMixin {
	@Unique
	private static final ThreadLocal<SheepRenderState> moremobvariants$state = new ThreadLocal<>();

	@Inject(method = "submit", at = @At("HEAD"))
	private void moremobvariants$captureState(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, SheepRenderState state, float yRot, float xRot, CallbackInfo ci) {
		moremobvariants$state.set(state);
	}

	@Inject(method = "submit", at = @At("RETURN"))
	private void moremobvariants$clearState(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, SheepRenderState state, float yRot, float xRot, CallbackInfo ci) {
		moremobvariants$state.remove();
	}

	@ModifyArg(
		method = "submit",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;coloredCutoutModelCopyLayerRender(Lnet/minecraft/client/model/Model;Lnet/minecraft/resources/Identifier;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;II)V"),
		index = 1
	)
	private Identifier mixinSheepFurTexture(Identifier location) {
		SheepRenderState state = moremobvariants$state.get();
		if (!(state instanceof MMVSheepRenderState ext)) return location;
		String variantStr = ext.getMmvVariantId();
		if (variantStr == null || variantStr.isEmpty()) return location;
		int colon = variantStr.indexOf(':');
		Identifier variantId = colon >= 0 ? Identifier.fromNamespaceAndPath(variantStr.substring(0, colon), variantStr.substring(colon + 1)) : MoreMobVariants.id(variantStr);
		if (!Variants.getVariant(EntityType.SHEEP, variantId).hasCustomWool()) return location;
		return Identifier.fromNamespaceAndPath(variantId.getNamespace(), "textures/entity/sheep/wool/" + variantId.getPath() + ".png");
	}
}
