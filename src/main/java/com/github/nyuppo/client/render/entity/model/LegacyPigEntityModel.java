package com.github.nyuppo.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.PigEntityModel;

@Environment(EnvType.CLIENT)
public class LegacyPigEntityModel extends PigEntityModel {
    public LegacyPigEntityModel(ModelPart root) {
        super(root);
    }

    public static TexturedModelData getTexturedModelData() {
        return TexturedModelData.of(getModelData(), 64, 32);
    }

    public static ModelData getModelData() {
        return PigEntityModel.getModelData(Dilation.NONE);
    }
}

