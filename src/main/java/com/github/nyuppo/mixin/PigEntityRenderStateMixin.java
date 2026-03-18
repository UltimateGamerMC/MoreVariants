package com.github.nyuppo.mixin;

import com.github.nyuppo.client.render.MMVPigRenderState;
import net.minecraft.client.render.entity.state.PigEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PigEntityRenderState.class)
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
