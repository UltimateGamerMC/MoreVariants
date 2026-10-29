package com.github.nyuppo.client.render;

import com.github.nyuppo.MoreMobVariants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class MMVEntityModelLayers {
	public static final ModelLayerLocation COW_LEGACY = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MoreMobVariants.MOD_ID, "cow_legacy"), "main");
	public static final ModelLayerLocation PIG_LEGACY = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MoreMobVariants.MOD_ID, "pig_legacy"), "main");
	public static final ModelLayerLocation SHEEP_HORNS = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MoreMobVariants.MOD_ID, "sheep_horns"), "main");
}
