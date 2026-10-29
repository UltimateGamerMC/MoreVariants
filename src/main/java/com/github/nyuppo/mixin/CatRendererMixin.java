package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVLivingRenderState;
import com.github.nyuppo.client.render.MMVTextures;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.variant.MobVariant;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.feline.Cat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CatRenderer.class)
public class CatRendererMixin {
	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void onExtractRenderState(Cat entity, CatRenderState state, float partialTicks, CallbackInfo ci) {
		CompoundTag nbt = MoreMobVariants.getCustomDataNbt(entity);
		MMVLivingRenderState ext = (MMVLivingRenderState) state;
		ext.setMmvVariantId(nbt.getStringOr(MoreMobVariants.NBT_KEY, ""));
		ext.setMmvTextureOverride(null);
		if (entity.hasCustomName()) {
			MobVariant v = Variants.getVariantFromNametag(EntityTypes.CAT, entity.getName().getString());
			if (v != null) {
				Identifier id = v.getIdentifier();
				ext.setMmvTextureOverride(moremobvariants$texture(id.getNamespace(), id.getPath()));
			}
		}
	}

	@Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
	private void onGetTextureLocation(CatRenderState state, CallbackInfoReturnable<Identifier> ci) {
		MMVLivingRenderState ext = (MMVLivingRenderState) state;
		Identifier texture = ext.getMmvTextureOverride();
		String variant = ext.getMmvVariantId();
		if (texture == null && variant != null && !variant.isEmpty()) {
			String[] split = Variants.splitVariant(variant);
			texture = variant.equals(MoreMobVariants.id("default").toString()) ? moremobvariants$texture("minecraft", "tabby") : moremobvariants$texture(split[0], split[1]);
		}
		if (texture != null) {
			texture = MMVTextures.forAge(texture, state.isBaby);
			if (texture != null) ci.setReturnValue(texture);
		}
	}

	// vanilla's own cat skins are named cat_<variant>.png since 1.21.5
	@Unique
	private static Identifier moremobvariants$texture(String namespace, String name) {
		return Identifier.fromNamespaceAndPath(namespace, "textures/entity/cat/" + (namespace.equals("minecraft") ? "cat_" : "") + name + ".png");
	}
}
