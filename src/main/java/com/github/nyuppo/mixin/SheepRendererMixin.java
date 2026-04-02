package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVSheepRenderState;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.variant.MobVariant;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.sheep.Sheep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SheepRenderer.class)
public class SheepRendererMixin {
	private static final Identifier DEFAULT = Identifier.withDefaultNamespace("textures/entity/sheep/sheep.png");

	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void onExtractRenderState(Sheep entity, SheepRenderState state, float partialTicks, CallbackInfo ci) {
		CompoundTag nbt = MoreMobVariants.getCustomDataNbt(entity);
		MMVSheepRenderState ext = (MMVSheepRenderState) state;
		ext.setMmvVariantId(nbt.getStringOr(MoreMobVariants.NBT_KEY, ""));
		ext.setMmvHornColour(nbt.getStringOr(MoreMobVariants.SHEEP_HORN_COLOUR_NBT_KEY, ""));
		ext.setMmvTextureOverride(null);
		if (entity.hasCustomName()) {
			MobVariant v = Variants.getVariantFromNametag(EntityType.SHEEP, entity.getName().getString());
			if (v != null) {
				Identifier id = v.getIdentifier();
				ext.setMmvVariantId(id.toString());
				ext.setMmvTextureOverride(Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/entity/sheep/" + id.getPath() + ".png"));
			}
		}
	}

	@Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
	private void onGetTextureLocation(SheepRenderState state, CallbackInfoReturnable<Identifier> ci) {
		MMVSheepRenderState ext = (MMVSheepRenderState) state;
		if (ext.getMmvTextureOverride() != null) {
			ci.setReturnValue(ext.getMmvTextureOverride());
			return;
		}
		String variant = ext.getMmvVariantId();
		if (variant != null && !variant.isEmpty()) {
			if (variant.equals(MoreMobVariants.id("default").toString())) {
				ci.setReturnValue(DEFAULT);
			} else {
				String[] split = Variants.splitVariant(variant);
				ci.setReturnValue(Identifier.fromNamespaceAndPath(split[0], "textures/entity/sheep/" + split[1] + ".png"));
			}
		}
	}
}
