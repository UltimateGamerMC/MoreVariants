package com.github.nyuppo.mixin;

import com.github.nyuppo.client.render.MMVPigRenderState;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PigRenderState.class)
public class PigEntityRenderStateMixin implements MMVPigRenderState {
    @Unique
    private String moremobvariantsVariantId = "";
    @Unique
    private boolean moremobvariantsMuddy = false;

    @Override
    public String getMmvVariantId() {
        return moremobvariantsVariantId;
    }

    @Override
    public void setMmvVariantId(String s) {
        this.moremobvariantsVariantId = s;
    }

    @Override
    public boolean getMmvMuddy() {
        return moremobvariantsMuddy;
    }

    @Override
    public void setMmvMuddy(boolean b) {
        this.moremobvariantsMuddy = b;
    }
}
