package com.github.nyuppo.client.render.entity.model;

import net.minecraft.client.model.animal.cow.CowModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.builders.LayerDefinition;

@Environment(EnvType.CLIENT)
public final class LegacyCowEntityModel {
	private LegacyCowEntityModel() {
	}

	public static LayerDefinition createLayerDefinition() {
		return LayerDefinition.create(CowModel.createBaseCowModel(), 64, 32);
	}

	public static LayerDefinition createBabyLayerDefinition() {
		return createLayerDefinition().apply(CowModel.BABY_TRANSFORMER);
	}
}
