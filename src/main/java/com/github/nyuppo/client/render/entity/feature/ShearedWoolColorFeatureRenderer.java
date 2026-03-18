package com.github.nyuppo.client.render.entity.feature;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVSheepRenderState;
import com.github.nyuppo.config.Variants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.SheepEntityModel;
import net.minecraft.client.render.entity.state.SheepEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
@Environment(EnvType.CLIENT)
public class ShearedWoolColorFeatureRenderer extends FeatureRenderer<SheepEntityRenderState, SheepEntityModel> {

    public ShearedWoolColorFeatureRenderer(FeatureRendererContext<SheepEntityRenderState, SheepEntityModel> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, SheepEntityRenderState state, float limbAngle, float limbDistance) {
        MMVSheepRenderState ext = (MMVSheepRenderState) state;
        String variantStr = ext.getMmvVariantId();
        if (variantStr == null || variantStr.isEmpty()) return;
        int colon = variantStr.indexOf(':');
        Identifier variantId = colon >= 0 ? Identifier.of(variantStr.substring(0, colon), variantStr.substring(colon + 1)) : MoreMobVariants.id(variantStr);
        if (!Variants.getVariant(EntityType.SHEEP, variantId).hasColorWhenSheared()) return;
        Identifier overlay = MoreMobVariants.id("textures/entity/sheep/sheared_color_overlay/" + variantId.getPath() + ".png");
        EntityModel<SheepEntityRenderState> model = getContextModel();
        queue.getBatchingQueue(0).submitModel(model, state, matrices, RenderLayers.entityCutoutNoCull(overlay), light, LivingEntityRenderer.getOverlay(state, 0.0f), state.getRgbColor(), null, state.outlineColor, null);
    }
}
