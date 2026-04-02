package com.github.nyuppo.mixin;

import net.minecraft.client.model.animal.feline.AdultFelineModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AdultFelineModel.class)
public class CatFluffMixin {
	@Inject(method = "createBodyMesh", at = @At("RETURN"))
	private static void onCreateBodyMesh(CubeDeformation g, CallbackInfoReturnable<MeshDefinition> cir) {
		MeshDefinition mesh = cir.getReturnValue();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild(
			"head",
			CubeListBuilder.create()
				.addBox("main", -2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 5.0F, g)
				.addBox("nose", -1.5F, -0.001F, -4.0F, 3, 2, 2, g, 0, 24)
				.addBox("ear1", -2.0F, -3.0F, 0.0F, 1, 1, 2, g, 0, 10)
				.addBox("ear2", 1.0F, -3.0F, 0.0F, 1, 1, 2, g, 6, 10)
				.addBox("scruff1", -4.5F, -2.0F, 1.0F, 9, 4, 0, g, 40, 28)
				.addBox("scruff2", -4.5F, -2.0F, -1.0F, 9, 4, 0, g, 40, 28),
			PartPose.offset(0.0F, 15.0F, -9.0F)
		);
		root.addOrReplaceChild(
			"body",
			CubeListBuilder.create()
				.texOffs(20, 0).addBox(-2.0F, 3.0F, -8.0F, 4.0F, 16.0F, 6.0F, g)
				.texOffs(32, 12).addBox(-4.0F, 6.0F, -9.0F, 8.0F, 0.0F, 8.0F, g)
				.texOffs(32, 20).addBox(-4.0F, 8.0F, -9.0F, 8.0F, 0.0F, 8.0F, g)
				.texOffs(32, 12).addBox(-4.0F, 10.0F, -9.0F, 8.0F, 0.0F, 8.0F, g)
				.texOffs(32, 20).addBox(-4.0F, 12.0F, -9.0F, 8.0F, 0.0F, 8.0F, g)
				.texOffs(32, 12).addBox(-4.0F, 14.0F, -9.0F, 8.0F, 0.0F, 8.0F, g)
				.texOffs(32, 20).addBox(-4.0F, 16.0F, -9.0F, 8.0F, 0.0F, 8.0F, g),
			PartPose.offsetAndRotation(0.0F, 12.0F, -10.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
		);
	}
}
