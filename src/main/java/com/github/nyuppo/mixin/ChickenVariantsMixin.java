package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.networking.ServerRespondBasicVariantPayload;
import com.github.nyuppo.util.MMVTrackedData;
import com.github.nyuppo.variant.MobVariant;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.chicken.ChickenVariants;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Chicken.class)
public abstract class ChickenVariantsMixin extends MobEntityVariantsMixin {
	private MobVariant variant = Variants.getDefaultVariant(EntityTypes.CHICKEN);

	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	private void onDefineSynchedData(SynchedEntityData.Builder entityData, CallbackInfo ci) {
		entityData.define(MMVTrackedData.CHICKEN_VARIANT_ID, "");
	}

	@Override
	protected void onWriteCustomData(ValueOutput view, CallbackInfo ci) {
		CompoundTag nbt = MoreMobVariants.getCustomDataNbt((Entity) (Object) this);
		if (nbt.contains(MoreMobVariants.NBT_KEY)) {
			view.putString(MoreMobVariants.NBT_KEY, nbt.getStringOr(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString()));
		}
	}

	@Override
	protected void onReadCustomData(ValueInput view, CallbackInfo ci) {
		String variantStr = view.getStringOr(MoreMobVariants.NBT_KEY, "");
		boolean hasModVariant = false;
		if (!variantStr.isEmpty()) {
			hasModVariant = true;
			if (variantStr.contains(":")) {
				variant = Variants.getVariant(EntityTypes.CHICKEN, Identifier.parse(variantStr));
			} else {
				variant = Variants.getVariant(EntityTypes.CHICKEN, MoreMobVariants.id(variantStr));
			}
			((Chicken) (Object) this).getEntityData().set(MMVTrackedData.CHICKEN_VARIANT_ID, variant.getIdentifier().toString());
			((Chicken) (Object) this).setVariant(((Chicken) (Object) this).registryAccess().lookupOrThrow(Registries.CHICKEN_VARIANT).getOrThrow(ChickenVariants.TEMPERATE));
		} else {
			Entity e = (Entity) (Object) this;
			Level w = e.level();
			Holder<Biome> biome = w.getBiome(((Chicken) (Object) this).blockPosition());
			if (MoreMobVariants.isTemperateBiome(biome)) {
				hasModVariant = true;
				Float moonSize = w instanceof ServerLevel sl ? sl.getMoonBrightness(e.blockPosition()) : null;
				variant = Variants.getRandomVariant(EntityTypes.CHICKEN, ((Chicken) (Object) this).getRandom().nextLong(), biome, null, moonSize);
				String id = variant.getIdentifier().toString();
				((Chicken) (Object) this).getEntityData().set(MMVTrackedData.CHICKEN_VARIANT_ID, id);
				((Chicken) (Object) this).setVariant(((Chicken) (Object) this).registryAccess().lookupOrThrow(Registries.CHICKEN_VARIANT).getOrThrow(ChickenVariants.TEMPERATE));
				var data = MoreMobVariants.getCustomDataNbt((Entity) (Object) this);
				data.putString(MoreMobVariants.NBT_KEY, id);
				MoreMobVariants.setCustomDataNbt((Entity) (Object) this, data);
			}
		}
		if (hasModVariant) {
			MinecraftServer server = ((Entity) (Object) this).level().getServer();
			if (server != null) {
				server.getPlayerList().getPlayers().forEach((player) -> {
					ServerPlayNetworking.send(player, new ServerRespondBasicVariantPayload(((Entity) (Object) this).getId(), variant.getIdentifier().toString()));
				});
			}
		}
	}

	@Override
	protected void onFinalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData entityData, CallbackInfoReturnable<SpawnGroupData> ci) {
		if (!MoreMobVariants.isTemperateBiome(world.getBiome(((Chicken) (Object) this).blockPosition()))) return;
		variant = Variants.getRandomVariant(EntityTypes.CHICKEN, ((Chicken) (Object) this).getRandom().nextLong(), world.getBiome(((Chicken) (Object) this).blockPosition()), null, world.getLevel().getMoonBrightness(((Chicken) (Object) this).blockPosition()));
		String id = variant.getIdentifier().toString();
		((Chicken) (Object) this).getEntityData().set(MMVTrackedData.CHICKEN_VARIANT_ID, id);
		var data = MoreMobVariants.getCustomDataNbt((Entity) (Object) this);
		data.putString(MoreMobVariants.NBT_KEY, id);
		MoreMobVariants.setCustomDataNbt((Entity) (Object) this, data);
		((Chicken) (Object) this).setVariant(world.getLevel().registryAccess().lookupOrThrow(Registries.CHICKEN_VARIANT).getOrThrow(ChickenVariants.TEMPERATE));
	}

	@Inject(method = "getBreedOffspring", at = @At("RETURN"))
	private void onGetBreedOffspring(ServerLevel level, AgeableMob partner, CallbackInfoReturnable<Chicken> cir) {
		Chicken child = cir.getReturnValue();
		if (child == null) return;
		if (!MoreMobVariants.isTemperateBiome(level.getBiome(child.blockPosition()))) return;
		MobVariant childVariant = Variants.getChildVariant(EntityTypes.CHICKEN, level, ((Chicken) (Object) this), partner);
		child.setVariant(level.registryAccess().lookupOrThrow(Registries.CHICKEN_VARIANT).getOrThrow(ChickenVariants.TEMPERATE));
		String id = childVariant.getIdentifier().toString();
		child.getEntityData().set(MMVTrackedData.CHICKEN_VARIANT_ID, id);
		var childData = MoreMobVariants.getCustomDataNbt(child);
		childData.putString(MoreMobVariants.NBT_KEY, id);
		MoreMobVariants.setCustomDataNbt(child, childData);
	}
}
