package com.github.nyuppo.client.render.entity.feature;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVSheepRenderState;
import com.github.nyuppo.config.Variants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.sheep.SheepModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

@Environment(EnvType.CLIENT)
public class ShearedWoolColorFeatureRenderer extends RenderLayer<SheepRenderState, SheepModel> {

	public ShearedWoolColorFeatureRenderer(RenderLayerParent<SheepRenderState, SheepModel> context) {
		super(context);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, SheepRenderState state, float yRot, float xRot) {
		MMVSheepRenderState ext = (MMVSheepRenderState) state;
		String variantStr = ext.getMmvVariantId();
		if (variantStr == null || variantStr.isEmpty()) return;
		int colon = variantStr.indexOf(':');
		Identifier variantId = colon >= 0 ? Identifier.fromNamespaceAndPath(variantStr.substring(0, colon), variantStr.substring(colon + 1)) : MoreMobVariants.id(variantStr);
		if (!Variants.getVariant(EntityTypes.SHEEP, variantId).hasColorWhenSheared()) return;
		Identifier overlay = MoreMobVariants.id("textures/entity/sheep/sheared_color_overlay/" + variantId.getPath() + ".png");
		EntityModel<SheepRenderState> model = getParentModel();
		submitNodeCollector.order(0)
			.submitModel(
				model,
				state,
				poseStack,
				RenderTypes.entityCutout(overlay),
				lightCoords,
				LivingEntityRenderer.getOverlayCoords(state, 0.0F),
				state.getWoolColor(),
				null,
				state.outlineColor
			);
	}
}
