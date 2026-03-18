package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVSheepRenderState;
import com.github.nyuppo.config.Variants;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.SheepWoolFeatureRenderer;
import net.minecraft.client.render.entity.state.SheepEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(SheepWoolFeatureRenderer.class)
public class SheepWoolFeatureMixin {

    @ModifyArgs(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/entity/state/SheepEntityRenderState;FF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/SheepWoolFeatureRenderer;render(Lnet/minecraft/client/model/Model;Lnet/minecraft/util/Identifier;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/entity/state/LivingEntityRenderState;II)V")
    )
    private void mixinSheepFurTexture(Args args, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, SheepEntityRenderState state, float f, float g) {
        if (!(state instanceof MMVSheepRenderState ext)) return;
        String variantStr = ext.getMmvVariantId();
        if (variantStr == null || variantStr.isEmpty()) return;
        int colon = variantStr.indexOf(':');
        Identifier variantId = colon >= 0 ? Identifier.of(variantStr.substring(0, colon), variantStr.substring(colon + 1)) : MoreMobVariants.id(variantStr);
        if (!Variants.getVariant(EntityType.SHEEP, variantId).hasCustomWool()) return;
        Identifier woolTexture = Identifier.of(variantId.getNamespace(), "textures/entity/sheep/wool/" + variantId.getPath() + ".png");
        args.set(1, woolTexture);
    }
}
