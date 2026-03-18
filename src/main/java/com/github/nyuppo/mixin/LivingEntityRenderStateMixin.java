package com.github.nyuppo.mixin;

import com.github.nyuppo.client.render.MMVLivingRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements MMVLivingRenderState {
    @Unique
    private String moremobvariantsVariantId = "";
    @Unique
    private Identifier moremobvariantsTextureOverride = null;

    @Override
    public String getMmvVariantId() {
        return moremobvariantsVariantId;
    }

    @Override
    public void setMmvVariantId(String s) {
        this.moremobvariantsVariantId = s;
    }

    @Override
    public Identifier getMmvTextureOverride() {
        return moremobvariantsTextureOverride;
    }

    @Override
    public void setMmvTextureOverride(Identifier id) {
        this.moremobvariantsTextureOverride = id;
    }
}
