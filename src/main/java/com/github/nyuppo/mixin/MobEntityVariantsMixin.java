package com.github.nyuppo.mixin;

import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobEntityVariantsMixin {
	@Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
	protected void onAddAdditionalSaveData(ValueOutput output, CallbackInfo ci) {
		onWriteCustomData(output, ci);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
	protected void onReadAdditionalSaveData(ValueInput input, CallbackInfo ci) {
		onReadCustomData(input, ci);
	}

	protected void onWriteCustomData(ValueOutput view, CallbackInfo ci) {
	}

	protected void onReadCustomData(ValueInput view, CallbackInfo ci) {
	}

	@Inject(method = "finalizeSpawn", at = @At("RETURN"))
	protected void onFinalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData entityData, CallbackInfoReturnable<SpawnGroupData> ci) {
	}

	@Inject(method = "tick", at = @At("RETURN"))
	protected void onTick(CallbackInfo ci) {
	}
}
