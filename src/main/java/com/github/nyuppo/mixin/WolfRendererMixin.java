package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVLivingRenderState;
import com.github.nyuppo.client.render.MMVTextures;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.variant.MobVariant;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WolfRenderer.class)
public class WolfRendererMixin {
	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void onExtractRenderState(Wolf entity, WolfRenderState state, float partialTicks, CallbackInfo ci) {
		CompoundTag nbt = MoreMobVariants.getCustomDataNbt(entity);
		MMVLivingRenderState ext = (MMVLivingRenderState) state;
		String variant = nbt.getStringOr(MoreMobVariants.NBT_KEY, "");
		ext.setMmvVariantId(variant);
		ext.setMmvTextureOverride(null);

		Identifier id = null;
		if (entity.hasCustomName()) {
			MobVariant v = Variants.getVariantFromNametag(EntityTypes.WOLF, entity.getName().getString());
			if (v != null) id = v.getIdentifier();
		}
		// "default" wolves carry vanilla's pale variant, which vanilla already textures for every state and age
		if (id == null && !variant.isEmpty() && !variant.equals(MoreMobVariants.id("default").toString())) {
			String[] split = Variants.splitVariant(variant);
			id = Identifier.fromNamespaceAndPath(split[0], split[1]);
		}
		if (id != null) {
			String suffix = entity.isTame() ? "_tame" : entity.isAngry() ? "_angry" : "_wild";
			ext.setMmvTextureOverride(MMVTextures.forAge(Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/entity/wolf/" + id.getPath() + suffix + ".png"), state.isBaby));
		}
	}

	@Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
	private void onGetTextureLocation(WolfRenderState state, CallbackInfoReturnable<Identifier> ci) {
		Identifier texture = ((MMVLivingRenderState) state).getMmvTextureOverride();
		if (texture != null) ci.setReturnValue(texture);
	}
}
