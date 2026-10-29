package com.github.nyuppo.client.render.entity.model;

import net.minecraft.client.model.animal.cow.CowModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;

@Environment(EnvType.CLIENT)
public final class LegacyCowEntityModel {
	private LegacyCowEntityModel() {
	}

	public static LayerDefinition createLayerDefinition() {
		MeshDefinition mesh = CowModel.createBaseCowModel();
		// The 1.21.5+ head adds a snout cube at uv (1, 33), below the 64x32 legacy skins; the old cow had none.
		mesh.getRoot().addOrReplaceChild(
			"head",
			CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-4.0F, -4.0F, -6.0F, 8.0F, 8.0F, 6.0F)
				.texOffs(22, 0)
				.addBox("right_horn", -5.0F, -5.0F, -5.0F, 1.0F, 3.0F, 1.0F)
				.texOffs(22, 0)
				.addBox("left_horn", 4.0F, -5.0F, -5.0F, 1.0F, 3.0F, 1.0F),
			PartPose.offset(0.0F, 4.0F, -8.0F)
		);
		return LayerDefinition.create(mesh, 64, 32);
	}
}
