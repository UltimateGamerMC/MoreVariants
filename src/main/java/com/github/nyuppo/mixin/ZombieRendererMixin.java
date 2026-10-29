package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVLivingRenderState;
import com.github.nyuppo.client.render.MMVTextures;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.variant.MobVariant;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractZombieRenderer.class)
public class ZombieRendererMixin {
	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void onExtractRenderState(Zombie entity, ZombieRenderState state, float partialTicks, CallbackInfo ci) {
		MMVLivingRenderState ext = (MMVLivingRenderState) state;
		ext.setMmvTextureOverride(null);
		// husks and drowned share this renderer but have their own look (drowned even an extra layer); the skins are zombie-only
		if (entity.getType() != EntityTypes.ZOMBIE) {
			ext.setMmvVariantId("");
			return;
		}
		CompoundTag nbt = MoreMobVariants.getCustomDataNbt(entity);
		String variant = nbt.getStringOr(MoreMobVariants.NBT_KEY, "");
		ext.setMmvVariantId(variant);

		Identifier id = null;
		if (entity.hasCustomName()) {
			MobVariant v = Variants.getVariantFromNametag(EntityTypes.ZOMBIE, entity.getName().getString());
			if (v != null) id = v.getIdentifier();
		}
		if (id == null && !variant.isEmpty() && !variant.equals(MoreMobVariants.id("default").toString())) {
			String[] split = Variants.splitVariant(variant);
			id = Identifier.fromNamespaceAndPath(split[0], split[1]);
		}
		if (id != null) {
			ext.setMmvTextureOverride(MMVTextures.forAge(Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/entity/zombie/" + id.getPath() + ".png"), state.isBaby));
		}
	}

	@Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
	private void onGetTextureLocation(ZombieRenderState state, CallbackInfoReturnable<Identifier> ci) {
		Identifier texture = ((MMVLivingRenderState) state).getMmvTextureOverride();
		if (texture != null) ci.setReturnValue(texture);
	}
}
