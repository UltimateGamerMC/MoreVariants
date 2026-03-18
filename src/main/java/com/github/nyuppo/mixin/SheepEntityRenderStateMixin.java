package com.github.nyuppo.mixin;

import com.github.nyuppo.client.render.MMVSheepRenderState;
import net.minecraft.client.render.entity.state.SheepEntityRenderState;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SheepEntityRenderState.class)
public class SheepEntityRenderStateMixin implements MMVSheepRenderState {
    @Unique
    private String moremobvariantsVariantId = "";
    @Unique
    private String moremobvariantsHornColour = "";
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
    public String getMmvHornColour() {
        return moremobvariantsHornColour;
    }

    @Override
    public void setMmvHornColour(String s) {
        this.moremobvariantsHornColour = s;
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
