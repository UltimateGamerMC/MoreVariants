package com.github.nyuppo.client.render.entity.feature;

import com.github.nyuppo.mixin.QuadrupedEntityModelPartAccessor;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.SheepEntityModel;
import net.minecraft.client.render.entity.state.SheepEntityRenderState;

import java.util.Collections;
import java.util.Map;

public class SheepHornsModel extends EntityModel<SheepEntityRenderState> {
    private final FeatureRendererContext<SheepEntityRenderState, SheepEntityModel> context;

    public SheepHornsModel(FeatureRendererContext<SheepEntityRenderState, SheepEntityModel> context, ModelPart hornsPart) {
        super(new ModelPart(Collections.emptyList(), Map.of("horns", hornsPart)));
        this.context = context;
    }

    @Override
    public void setAngles(SheepEntityRenderState state) {
        ModelPart head = ((QuadrupedEntityModelPartAccessor) context.getModel()).getHead();
        this.root.getChild("horns").setTransform(head.getTransform());
    }
}
