package com.github.nyuppo.client.render.entity.feature;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVPigRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PigEntityModel;
import net.minecraft.client.render.entity.state.PigEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class PigMudFeatureRenderer extends FeatureRenderer<PigEntityRenderState, PigEntityModel> {
    private static final Identifier MUD_TEXTURE = Identifier.of(MoreMobVariants.MOD_ID, "textures/entity/pig/mud/mud_overlay.png");

    public PigMudFeatureRenderer(FeatureRendererContext<PigEntityRenderState, PigEntityModel> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, PigEntityRenderState state, float limbAngle, float limbDistance) {
        if (!((MMVPigRenderState) state).getMmvMuddy()) return;
        queue.getBatchingQueue(0).submitModel(getContextModel(), state, matrices, RenderLayers.entityTranslucent(MUD_TEXTURE), light, LivingEntityRenderer.getOverlay(state, 0.0f), 0xFFFFFFFF, null, state.outlineColor, null);
    }
}
