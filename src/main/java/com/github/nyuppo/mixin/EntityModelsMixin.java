package com.github.nyuppo.mixin;

import com.github.nyuppo.client.render.MMVEntityModelLayers;
import com.github.nyuppo.client.render.entity.model.LegacyCowEntityModel;
import com.github.nyuppo.client.render.entity.model.LegacyPigEntityModel;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.PigEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.client.render.entity.model.EntityModels")
public class EntityModelsMixin {

    @Redirect(method = "getModels", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;"))
    private static ImmutableMap<?, ?> addLegacyModels(ImmutableMap.Builder<EntityModelLayer, TexturedModelData> builder) {
        builder.put(MMVEntityModelLayers.COW_LEGACY, LegacyCowEntityModel.getTexturedModelData());
        builder.put(MMVEntityModelLayers.COW_LEGACY_BABY, LegacyCowEntityModel.getTexturedModelData().transform(CowEntityModel.BABY_TRANSFORMER));
        builder.put(MMVEntityModelLayers.PIG_LEGACY, LegacyPigEntityModel.getTexturedModelData());
        builder.put(MMVEntityModelLayers.PIG_LEGACY_BABY, LegacyPigEntityModel.getTexturedModelData().transform(PigEntityModel.BABY_TRANSFORMER));
        return builder.build();
    }
}
