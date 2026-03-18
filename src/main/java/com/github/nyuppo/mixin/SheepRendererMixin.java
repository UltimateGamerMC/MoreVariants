package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVSheepRenderState;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.variant.MobVariant;
import net.minecraft.client.render.entity.SheepEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.render.entity.state.SheepEntityRenderState;

@Mixin(SheepEntityRenderer.class)
public class SheepRendererMixin {
    private static final Identifier DEFAULT = Identifier.ofVanilla("textures/entity/sheep/sheep.png");

    @Inject(method = "updateRenderState", at = @At("RETURN"))
    private void onUpdateRenderState(SheepEntity entity, SheepEntityRenderState state, float tickDelta, CallbackInfo ci) {
        NbtCompound nbt = MoreMobVariants.getCustomDataNbt(entity);
        MMVSheepRenderState ext = (MMVSheepRenderState) state;
        ext.setMmvVariantId(nbt.getString(MoreMobVariants.NBT_KEY, ""));
        ext.setMmvHornColour(nbt.getString(MoreMobVariants.SHEEP_HORN_COLOUR_NBT_KEY, ""));
        ext.setMmvTextureOverride(null);
        if (entity.hasCustomName()) {
            MobVariant v = Variants.getVariantFromNametag(EntityType.SHEEP, entity.getName().getString());
            if (v != null) {
                Identifier id = v.getIdentifier();
                ext.setMmvVariantId(id.toString());
                ext.setMmvTextureOverride(Identifier.of(id.getNamespace(), "textures/entity/sheep/" + id.getPath() + ".png"));
            }
        }
    }

    @Inject(method = "getTexture(Lnet/minecraft/client/render/entity/state/SheepEntityRenderState;)Lnet/minecraft/util/Identifier;", at = @At("HEAD"), cancellable = true)
    private void onGetTexture(SheepEntityRenderState state, CallbackInfoReturnable<Identifier> ci) {
        MMVSheepRenderState ext = (MMVSheepRenderState) state;
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
                ci.setReturnValue(Identifier.of(split[0], "textures/entity/sheep/" + split[1] + ".png"));
            }
        }
    }
}
