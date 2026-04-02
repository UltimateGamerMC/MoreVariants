package com.github.nyuppo.client.render.entity.feature;

import com.github.nyuppo.mixin.QuadrupedEntityModelPartAccessor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.sheep.SheepModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.SheepRenderState;

public class SheepHornsModel extends EntityModel<SheepRenderState> {
	private final RenderLayerParent<SheepRenderState, SheepModel> parent;

	public SheepHornsModel(RenderLayerParent<SheepRenderState, SheepModel> parent, ModelPart root) {
		super(root);
		this.parent = parent;
	}

	@Override
	public void setupAnim(SheepRenderState state) {
		SheepModel sheep = parent.getModel();
		ModelPart head = ((QuadrupedEntityModelPartAccessor) (Object) sheep).mmv_getHead();
		root().getChild("horns").loadPose(head.storePose());
	}
}
