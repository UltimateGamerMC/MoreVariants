package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVLivingRenderState;
import com.github.nyuppo.config.Variants;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.SpiderEyesFeatureRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(EyesFeatureRenderer.class)
public class CustomEyesFeatureMixin {

    @ModifyArgs(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/command/RenderCommandQueue;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/RenderLayer;IIILnet/minecraft/client/texture/Sprite;ILnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;)V")
    )
    private void mixinEyesTexture(Args args, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, EntityRenderState state, float limbAngle, float limbDistance) {
        if (!((Object) this instanceof SpiderEyesFeatureRenderer)) return;
        if (!(state instanceof LivingEntityRenderState s) || !(s instanceof MMVLivingRenderState ext)) return;
        String variantStr = ext.getMmvVariantId();
        if (variantStr == null || variantStr.isEmpty()) return;
        int colon = variantStr.indexOf(':');
        Identifier variantId = colon >= 0 ? Identifier.of(variantStr.substring(0, colon), variantStr.substring(colon + 1)) : MoreMobVariants.id(variantStr);
        if (!Variants.getVariant(EntityType.SPIDER, variantId).hasCustomEyes()) return;
        Identifier eyesTexture = Identifier.of(variantId.getNamespace(), "textures/entity/spider/eyes/" + variantId.getPath() + ".png");
        args.set(3, RenderLayers.eyes(eyesTexture));
    }
}
