package com.github.nyuppo.mixin;

import net.minecraft.client.model.animal.pig.PigModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PigModel.class)
public interface PigModelInvoker {
	@Invoker("createBasePigModel")
	static MeshDefinition mmv_createBasePigModel(CubeDeformation deformation) {
		throw new AssertionError();
	}
}
