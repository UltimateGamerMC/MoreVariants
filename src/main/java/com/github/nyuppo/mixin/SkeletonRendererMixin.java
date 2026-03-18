package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVLivingRenderState;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.variant.MobVariant;
import net.minecraft.client.render.entity.AbstractSkeletonEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.render.entity.state.SkeletonEntityRenderState;

@Mixin(AbstractSkeletonEntityRenderer.class)
public class SkeletonRendererMixin {
    @Inject(method = "updateRenderState", at = @At("RETURN"))
    private void onUpdateRenderState(AbstractSkeletonEntity entity, SkeletonEntityRenderState state, float tickDelta, CallbackInfo ci) {
        NbtCompound nbt = MoreMobVariants.getCustomDataNbt(entity);
        MMVLivingRenderState ext = (MMVLivingRenderState) state;
        ext.setMmvVariantId(nbt.getString(MoreMobVariants.NBT_KEY, ""));
        ext.setMmvTextureOverride(null);
        if (entity.hasCustomName()) {
            MobVariant v = Variants.getVariantFromNametag(EntityType.SKELETON, entity.getName().getString());
            if (v != null) {
                Identifier id = v.getIdentifier();
                ext.setMmvTextureOverride(Identifier.of(id.getNamespace(), "textures/entity/skeleton/" + id.getPath() + ".png"));
            }
        }
    }
}
