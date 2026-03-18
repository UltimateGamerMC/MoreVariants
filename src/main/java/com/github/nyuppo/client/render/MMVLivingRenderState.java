package com.github.nyuppo.client.render;

import net.minecraft.util.Identifier;

public interface MMVLivingRenderState {
    String getMmvVariantId();
    void setMmvVariantId(String s);
    Identifier getMmvTextureOverride();
    void setMmvTextureOverride(Identifier id);
}
