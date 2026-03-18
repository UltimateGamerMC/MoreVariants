package com.github.nyuppo.mixin;

import com.github.nyuppo.client.render.MMVEntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(targets = "net.minecraft.client.render.entity.model.EntityModelLayers")
public class EntityModelLayersMixin {
    @Shadow
    @Mutable
    private static Set<EntityModelLayer> LAYERS;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onClinit(CallbackInfo ci) {
        LAYERS.add(MMVEntityModelLayers.COW_LEGACY);
        LAYERS.add(MMVEntityModelLayers.COW_LEGACY_BABY);
        LAYERS.add(MMVEntityModelLayers.PIG_LEGACY);
        LAYERS.add(MMVEntityModelLayers.PIG_LEGACY_BABY);
    }
}
