package com.github.nyuppo.client.render.entity.feature;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVSheepRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.SheepEntityModel;
import net.minecraft.client.render.entity.state.SheepEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SheepHornsFeatureRenderer extends FeatureRenderer<SheepEntityRenderState, SheepEntityModel> {
    private static final Identifier HORNS_BROWN = Identifier.of(MoreMobVariants.MOD_ID, "textures/entity/sheep/horns/horns_brown.png");
    private static final Identifier HORNS_GRAY = Identifier.of(MoreMobVariants.MOD_ID, "textures/entity/sheep/horns/horns_gray.png");
    private static final Identifier HORNS_BLACK = Identifier.of(MoreMobVariants.MOD_ID, "textures/entity/sheep/horns/horns_black.png");
    private static final Identifier HORNS_BEIGE = Identifier.of(MoreMobVariants.MOD_ID, "textures/entity/sheep/horns/horns_beige.png");

    private final SheepHornsModel hornsModel;

    public SheepHornsFeatureRenderer(FeatureRendererContext<SheepEntityRenderState, SheepEntityModel> context) {
        super(context);
        ModelPart hornsPart = getTexturedModelData().createModel();
        this.hornsModel = new SheepHornsModel(context, hornsPart);
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, SheepEntityRenderState state, float limbAngle, float limbDistance) {
        String hornColour = ((MMVSheepRenderState) state).getMmvHornColour();
        if (hornColour == null || hornColour.isEmpty()) return;
        hornsModel.setAngles(state);
        Identifier texture = switch (hornColour.toLowerCase()) {
            case "brown" -> HORNS_BROWN;
            case "gray" -> HORNS_GRAY;
            case "black" -> HORNS_BLACK;
            case "beige" -> HORNS_BEIGE;
            default -> null;
        };
        if (texture == null) return;
        if (state.baby) {
            matrices.push();
            matrices.translate(0.0, 0.5, 0.25);
            queue.getBatchingQueue(0).submitModel(hornsModel, state, matrices, RenderLayers.entityCutoutNoCull(texture), light, LivingEntityRenderer.getOverlay(state, 0.0f), 0xFFFFFFFF, null, state.outlineColor, null);
            matrices.pop();
        } else {
            queue.getBatchingQueue(0).submitModel(hornsModel, state, matrices, RenderLayers.entityCutoutNoCull(texture), light, LivingEntityRenderer.getOverlay(state, 0.0f), 0xFFFFFFFF, null, state.outlineColor, null);
        }
    }

    private static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild(EntityModelPartNames.HEAD,
                ModelPartBuilder.create()
                        .uv(0, 0).cuboid(3.0f, -3.5f, -3.0f, 4.0f, 7.0f, 6.0f)
                        .uv(0, 13).cuboid(3.0f, 0.5f, -6.0f, 4.0f, 3.0f, 3.0f)
                        .uv(0, 19).cuboid(-7.0f, -3.5f, -3.0f, 4.0f, 7.0f, 6.0f)
                        .uv(14, 13).cuboid(-7.0f, 0.5f, -6.0f, 4.0f, 3.0f, 3.0f),
                ModelTransform.origin(0.0f, -1.5f, -2.1f));
        return TexturedModelData.of(modelData, 32, 32);
    }
}
