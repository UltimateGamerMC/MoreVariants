package com.github.nyuppo.mixin;

import com.github.nyuppo.client.render.MMVEntityModelLayers;
import com.github.nyuppo.client.render.entity.feature.SheepHornsFeatureRenderer;
import com.github.nyuppo.client.render.entity.model.LegacyCowEntityModel;
import com.github.nyuppo.client.render.entity.model.LegacyPigEntityModel;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LayerDefinitions.class)
public class LayerDefinitionsMixin {
	@Redirect(
		method = "createRoots",
		at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;")
	)
	private static ImmutableMap<ModelLayerLocation, LayerDefinition> mmv$injectLegacy(ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> builder) {
		builder.put(MMVEntityModelLayers.COW_LEGACY, LegacyCowEntityModel.createLayerDefinition());
		builder.put(MMVEntityModelLayers.PIG_LEGACY, LegacyPigEntityModel.createLayerDefinition());
		builder.put(MMVEntityModelLayers.SHEEP_HORNS, SheepHornsFeatureRenderer.createHornsLayer());
		return builder.build();
	}
}
