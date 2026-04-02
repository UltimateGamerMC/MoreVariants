package com.github.nyuppo.client.render;

import net.minecraft.resources.Identifier;

public interface MMVSheepRenderState {
    String getMmvVariantId();
    void setMmvVariantId(String s);
    String getMmvHornColour();
    void setMmvHornColour(String s);
    Identifier getMmvTextureOverride();
    void setMmvTextureOverride(Identifier id);
}
