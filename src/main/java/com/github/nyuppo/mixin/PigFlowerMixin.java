package com.github.nyuppo.mixin;

import net.minecraft.client.model.animal.pig.PigModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PigModel.class)
public class PigFlowerMixin {
	@Inject(method = "createBasePigModel", at = @At("RETURN"))
	private static void onCreateBasePigModel(CubeDeformation g, CallbackInfoReturnable<MeshDefinition> cir) {
		MeshDefinition mesh = cir.getReturnValue();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild(
			"head",
			CubeListBuilder.create()
				.texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, g)
				.texOffs(16, 16).addBox(-2.0F, 0.0F, -9.0F, 4.0F, 3.0F, 1.0F, g)
				.texOffs(28, 3).addBox(-1.0F, -5.0F, -7.0F, 4.0F, 1.0F, 4.0F, g)
				.texOffs(44, 2).addBox(0.0F, -11.0F, -5.0F, 4.0F, 6.0F, 0.0F, g),
			PartPose.offset(0.0F, 12.0F, -6.0F)
		);
	}
}
