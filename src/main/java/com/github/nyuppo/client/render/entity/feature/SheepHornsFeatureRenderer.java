package com.github.nyuppo.client.render.entity.feature;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVSheepRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.sheep.SheepModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class SheepHornsFeatureRenderer extends RenderLayer<SheepRenderState, SheepModel> {
	private static final Identifier HORNS_BROWN = Identifier.fromNamespaceAndPath(MoreMobVariants.MOD_ID, "textures/entity/sheep/horns/horns_brown.png");
	private static final Identifier HORNS_GRAY = Identifier.fromNamespaceAndPath(MoreMobVariants.MOD_ID, "textures/entity/sheep/horns/horns_gray.png");
	private static final Identifier HORNS_BLACK = Identifier.fromNamespaceAndPath(MoreMobVariants.MOD_ID, "textures/entity/sheep/horns/horns_black.png");
	private static final Identifier HORNS_BEIGE = Identifier.fromNamespaceAndPath(MoreMobVariants.MOD_ID, "textures/entity/sheep/horns/horns_beige.png");

	private final SheepHornsModel hornsModel;

	public SheepHornsFeatureRenderer(RenderLayerParent<SheepRenderState, SheepModel> context, ModelPart hornsPart) {
		super(context);
		this.hornsModel = new SheepHornsModel(context, hornsPart);
	}

	public static LayerDefinition createHornsLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild(
			"horns",
			CubeListBuilder.create()
				.texOffs(0, 0).addBox(3.0F, -3.5F, -3.0F, 4.0F, 7.0F, 6.0F)
				.texOffs(0, 13).addBox(3.0F, 0.5F, -6.0F, 4.0F, 3.0F, 3.0F)
				.texOffs(0, 19).addBox(-7.0F, -3.5F, -3.0F, 4.0F, 7.0F, 6.0F)
				.texOffs(14, 13).addBox(-7.0F, 0.5F, -6.0F, 4.0F, 3.0F, 3.0F),
			PartPose.offset(0.0F, -1.5F, -2.1F)
		);
		return LayerDefinition.create(mesh, 32, 32);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, SheepRenderState state, float yRot, float xRot) {
		String hornColour = ((MMVSheepRenderState) state).getMmvHornColour();
		if (hornColour == null || hornColour.isEmpty()) return;
		hornsModel.setupAnim(state);
		Identifier texture = switch (hornColour.toLowerCase()) {
			case "brown" -> HORNS_BROWN;
			case "gray" -> HORNS_GRAY;
			case "black" -> HORNS_BLACK;
			case "beige" -> HORNS_BEIGE;
			default -> null;
		};
		if (texture == null) return;
		if (state.isBaby) {
			poseStack.pushPose();
			poseStack.translate(0.0, 0.5, 0.25);
			submitNodeCollector.order(0)
				.submitModel(hornsModel, state, poseStack, RenderTypes.entityCutout(texture), lightCoords, LivingEntityRenderer.getOverlayCoords(state, 0.0F), 0xFFFFFFFF, null, state.outlineColor);
			poseStack.popPose();
		} else {
			submitNodeCollector.order(0)
				.submitModel(hornsModel, state, poseStack, RenderTypes.entityCutout(texture), lightCoords, LivingEntityRenderer.getOverlayCoords(state, 0.0F), 0xFFFFFFFF, null, state.outlineColor);
		}
	}
}
