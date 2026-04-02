package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.client.render.MMVLivingRenderState;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.variant.MobVariant;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractZombieRenderer.class)
public class ZombieRendererMixin {
	private static final Identifier DEFAULT = Identifier.withDefaultNamespace("textures/entity/zombie/zombie.png");

	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void onExtractRenderState(Zombie entity, ZombieRenderState state, float partialTicks, CallbackInfo ci) {
		CompoundTag nbt = MoreMobVariants.getCustomDataNbt(entity);
		MMVLivingRenderState ext = (MMVLivingRenderState) state;
		ext.setMmvVariantId(nbt.getStringOr(MoreMobVariants.NBT_KEY, ""));
		ext.setMmvTextureOverride(null);
		if (entity.hasCustomName()) {
			MobVariant v = Variants.getVariantFromNametag(EntityType.ZOMBIE, entity.getName().getString());
			if (v != null) {
				Identifier id = v.getIdentifier();
				ext.setMmvTextureOverride(Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/entity/zombie/" + id.getPath() + ".png"));
			}
		}
	}

	@Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
	private void onGetTextureLocation(ZombieRenderState state, CallbackInfoReturnable<Identifier> ci) {
		MMVLivingRenderState ext = (MMVLivingRenderState) state;
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
				ci.setReturnValue(Identifier.fromNamespaceAndPath(split[0], "textures/entity/zombie/" + split[1] + ".png"));
			}
		}
	}
}
