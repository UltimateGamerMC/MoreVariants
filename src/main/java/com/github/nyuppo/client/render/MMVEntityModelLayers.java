package com.github.nyuppo.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class MMVEntityModelLayers {
    public static final EntityModelLayer COW_LEGACY = new EntityModelLayer(Identifier.of("moremobvariants", "cow_legacy"), "main");
    public static final EntityModelLayer COW_LEGACY_BABY = new EntityModelLayer(Identifier.of("moremobvariants", "cow_legacy_baby"), "main");
    public static final EntityModelLayer PIG_LEGACY = new EntityModelLayer(Identifier.of("moremobvariants", "pig_legacy"), "main");
    public static final EntityModelLayer PIG_LEGACY_BABY = new EntityModelLayer(Identifier.of("moremobvariants", "pig_legacy_baby"), "main");
}
