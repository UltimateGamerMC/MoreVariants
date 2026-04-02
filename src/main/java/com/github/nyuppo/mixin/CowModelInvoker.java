package com.github.nyuppo.mixin;

import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CowModel.class)
public interface CowModelInvoker {
	@Invoker("createBaseCowModel")
	static MeshDefinition mmv_createBaseCowModel() {
		throw new AssertionError();
	}
}
