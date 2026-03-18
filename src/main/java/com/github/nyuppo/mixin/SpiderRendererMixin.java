package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVLivingRenderState;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.variant.MobVariant;
import net.minecraft.client.render.entity.SpiderEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;

@Mixin(SpiderEntityRenderer.class)
public class SpiderRendererMixin {
    private static final Identifier DEFAULT = Identifier.ofVanilla("textures/entity/spider/spider.png");

    @Inject(method = "updateRenderState", at = @At("RETURN"))
    private void onUpdateRenderState(SpiderEntity entity, LivingEntityRenderState state, float tickDelta, CallbackInfo ci) {
        NbtCompound nbt = MoreMobVariants.getCustomDataNbt(entity);
        MMVLivingRenderState ext = (MMVLivingRenderState) state;
        ext.setMmvVariantId(nbt.getString(MoreMobVariants.NBT_KEY, ""));
        ext.setMmvTextureOverride(null);
        if (entity.hasCustomName()) {
            MobVariant v = Variants.getVariantFromNametag(EntityType.SPIDER, entity.getName().getString());
            if (v != null) {
                Identifier id = v.getIdentifier();
                ext.setMmvTextureOverride(Identifier.of(id.getNamespace(), "textures/entity/spider/" + id.getPath() + ".png"));
            }
        }
    }

    @Inject(method = "getTexture(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;)Lnet/minecraft/util/Identifier;", at = @At("HEAD"), cancellable = true)
    private void onGetTexture(LivingEntityRenderState state, CallbackInfoReturnable<Identifier> ci) {
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
                ci.setReturnValue(Identifier.of(split[0], "textures/entity/spider/" + split[1] + ".png"));
            }
        }
    }
}
