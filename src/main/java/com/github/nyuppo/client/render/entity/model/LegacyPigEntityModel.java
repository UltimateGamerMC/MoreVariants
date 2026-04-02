package com.github.nyuppo.client.render.entity.model;

import com.github.nyuppo.mixin.PigModelInvoker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.pig.PigModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

@Environment(EnvType.CLIENT)
public final class LegacyPigEntityModel {
	private LegacyPigEntityModel() {
	}

	public static LayerDefinition createLayerDefinition() {
		return LayerDefinition.create(PigModelInvoker.mmv_createBasePigModel(CubeDeformation.NONE), 64, 32);
	}

	public static LayerDefinition createBabyLayerDefinition() {
		return createLayerDefinition().apply(PigModel.BABY_TRANSFORMER);
	}
}
