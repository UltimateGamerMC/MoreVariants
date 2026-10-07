package com.github.nyuppo.client.render.entity.feature;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVPigRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.pig.PigModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class PigMudFeatureRenderer extends RenderLayer<PigRenderState, PigModel> {
	private static final Identifier MUD_TEXTURE = Identifier.fromNamespaceAndPath(MoreMobVariants.MOD_ID, "textures/entity/pig/mud/mud_overlay.png");

	public PigMudFeatureRenderer(RenderLayerParent<PigRenderState, PigModel> context) {
		super(context);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, PigRenderState state, float yRot, float xRot) {
		if (!((MMVPigRenderState) state).getMmvMuddy()) return;
		submitNodeCollector.order(0)
			.submitModel(
				getParentModel(),
				state,
				poseStack,
				RenderTypes.entityTranslucent(MUD_TEXTURE),
				lightCoords,
				LivingEntityRenderer.getOverlayCoords(state, 0.0F),
				0xFFFFFFFF,
				null,
				state.outlineColor
			);
	}
}
