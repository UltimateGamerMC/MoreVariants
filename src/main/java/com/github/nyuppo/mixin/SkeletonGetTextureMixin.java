package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVLivingRenderState;
import com.github.nyuppo.config.Variants;
import net.minecraft.client.render.entity.SkeletonEntityRenderer;
import net.minecraft.client.render.entity.state.SkeletonEntityRenderState;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkeletonEntityRenderer.class)
public class SkeletonGetTextureMixin {
    private static final Identifier DEFAULT = Identifier.ofVanilla("textures/entity/skeleton/skeleton.png");

    @Inject(method = "getTexture(Lnet/minecraft/client/render/entity/state/SkeletonEntityRenderState;)Lnet/minecraft/util/Identifier;", at = @At("HEAD"), cancellable = true)
    private void onGetTexture(SkeletonEntityRenderState state, CallbackInfoReturnable<Identifier> ci) {
        MMVLivingRenderState ext = (MMVLivingRenderState) state;
        if (ext.getMmvTextureOverride() != null) {
            ci.setReturnValue(ext.getMmvTextureOverride());
            return;
        }
        String variant = ext.getMmvVariantId();
        if (variant != null && !variant.isEmpty()) {
            if (variant.equals(MoreMobVariants.id("default").toString())) {
                ci.setReturnValue(DEFAULT);
            } else {
                String[] split = Variants.splitVariant(variant);
                ci.setReturnValue(Identifier.of(split[0], "textures/entity/skeleton/" + split[1] + ".png"));
            }
        }
    }
}
