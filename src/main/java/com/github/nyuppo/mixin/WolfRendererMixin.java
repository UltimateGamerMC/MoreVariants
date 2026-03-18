package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVLivingRenderState;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.variant.MobVariant;
import net.minecraft.client.render.entity.WolfEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.render.entity.state.WolfEntityRenderState;

@Mixin(WolfEntityRenderer.class)
public class WolfRendererMixin {
    private static final Identifier DEFAULT_WILD = Identifier.ofVanilla("textures/entity/wolf/wolf.png");
    private static final Identifier DEFAULT_TAMED = Identifier.ofVanilla("textures/entity/wolf/wolf_tame.png");
    private static final Identifier DEFAULT_ANGRY = Identifier.ofVanilla("textures/entity/wolf/wolf_angry.png");

    @Inject(method = "updateRenderState", at = @At("RETURN"))
    private void onUpdateRenderState(WolfEntity entity, WolfEntityRenderState state, float tickDelta, CallbackInfo ci) {
        NbtCompound nbt = MoreMobVariants.getCustomDataNbt(entity);
        MMVLivingRenderState ext = (MMVLivingRenderState) state;
        String variantStr = nbt.getString(MoreMobVariants.NBT_KEY, "");
        ext.setMmvVariantId(variantStr);
        ext.setMmvTextureOverride(null);
        if (entity.hasCustomName()) {
            MobVariant v = Variants.getVariantFromNametag(EntityType.WOLF, entity.getName().getString());
            if (v != null) {
                Identifier id = v.getIdentifier();
                String suffix = entity.isTamed() ? "_tame" : entity.hasAngerTime() ? "_angry" : "_wild";
                ext.setMmvTextureOverride(Identifier.of(id.getNamespace(), "textures/entity/wolf/" + id.getPath() + suffix + ".png"));
            }
        } else if (variantStr != null && !variantStr.isEmpty() && !variantStr.equals(MoreMobVariants.id("default").toString())) {
            String[] split = Variants.splitVariant(variantStr);
            String suffix = entity.isTamed() ? "_tame" : entity.hasAngerTime() ? "_angry" : "_wild";
            ext.setMmvTextureOverride(Identifier.of(split[0], "textures/entity/wolf/" + split[1] + suffix + ".png"));
        }
    }

    @Inject(method = "getTexture(Lnet/minecraft/client/render/entity/state/WolfEntityRenderState;)Lnet/minecraft/util/Identifier;", at = @At("HEAD"), cancellable = true)
    private void onGetTexture(WolfEntityRenderState state, CallbackInfoReturnable<Identifier> ci) {
        Identifier override = ((MMVLivingRenderState) state).getMmvTextureOverride();
        if (override != null) {
            ci.setReturnValue(override);
        }
    }
}
