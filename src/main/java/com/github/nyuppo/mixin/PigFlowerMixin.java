package com.github.nyuppo.mixin;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.PigEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PigEntityModel.class)
public class PigFlowerMixin {
    @Inject(method = "getModelData", at = @At("RETURN"))
    private static void onGetModelData(Dilation dilation, CallbackInfoReturnable<ModelData> ci) {
        ModelData data = ci.getReturnValue();
        if (data != null) {
            data.getRoot().addChild(EntityModelPartNames.HEAD,
                    ModelPartBuilder.create()
                            .uv(0, 0).cuboid(-4.0f, -4.0f, -8.0f, 8.0f, 8.0f, 8.0f, dilation)
                            .uv(16, 16).cuboid(-2.0f, 0.0f, -9.0f, 4.0f, 3.0f, 1.0f, dilation)
                            .uv(28, 3).cuboid(-1.0f, -5.0f, -7.0f, 4.0f, 1.0f, 4.0f, dilation)
                            .uv(44, 2).cuboid(0.0f, -11.0f, -5.0f, 4.0f, 6.0f, 0.0f, dilation),
                    ModelTransform.origin(0.0f, 12.0f, -6.0f));
        }
    }
}
