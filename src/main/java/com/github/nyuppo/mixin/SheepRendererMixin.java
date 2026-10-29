package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVSheepRenderState;
import com.github.nyuppo.client.render.MMVTextures;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.variant.MobVariant;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.sheep.Sheep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SheepRenderer.class)
public class SheepRendererMixin {
	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void onExtractRenderState(Sheep entity, SheepRenderState state, float partialTicks, CallbackInfo ci) {
		CompoundTag nbt = MoreMobVariants.getCustomDataNbt(entity);
		MMVSheepRenderState ext = (MMVSheepRenderState) state;
		ext.setMmvVariantId(nbt.getStringOr(MoreMobVariants.NBT_KEY, ""));
		ext.setMmvHornColour(nbt.getStringOr(MoreMobVariants.SHEEP_HORN_COLOUR_NBT_KEY, ""));
		ext.setMmvTextureOverride(null);
		if (entity.hasCustomName()) {
			MobVariant v = Variants.getVariantFromNametag(EntityTypes.SHEEP, entity.getName().getString());
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
		Identifier texture = ext.getMmvTextureOverride();
		String variant = ext.getMmvVariantId();
		// "default" sheep are plain vanilla sheep, which vanilla already textures for both ages
		if (texture == null && variant != null && !variant.isEmpty() && !variant.equals(MoreMobVariants.id("default").toString())) {
			String[] split = Variants.splitVariant(variant);
			texture = Identifier.fromNamespaceAndPath(split[0], "textures/entity/sheep/" + split[1] + ".png");
		}
		if (texture != null) {
			texture = MMVTextures.forAge(texture, state.isBaby);
			if (texture != null) ci.setReturnValue(texture);
		}
	}
}
