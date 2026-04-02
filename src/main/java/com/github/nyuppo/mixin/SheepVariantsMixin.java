package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.config.SheepHornSettings;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.networking.ServerRespondVariantPayload;
import com.github.nyuppo.variant.MobVariant;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Sheep.class)
public abstract class SheepVariantsMixin extends MobEntityVariantsMixin {
    private MobVariant variant = Variants.getDefaultVariant(EntityType.SHEEP);
    private String hornColour = "";

    @Override
    protected void onWriteCustomData(ValueOutput view, CallbackInfo ci) {
        view.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
        view.putString(MoreMobVariants.SHEEP_HORN_COLOUR_NBT_KEY, hornColour);
    }

    @Override
    protected void onReadCustomData(ValueInput view, CallbackInfo ci) {
        String variantStr = view.getStringOr(MoreMobVariants.NBT_KEY, "");
        if (!variantStr.isEmpty()) {
            if (variantStr.contains(":")) {
                variant = Variants.getVariant(EntityType.SHEEP, Identifier.parse(variantStr));
            } else {
                variant = Variants.getVariant(EntityType.SHEEP, MoreMobVariants.id(variantStr));
            }
        } else {
            Entity e = (Entity)(Object)this;
            Level w = e.level();
            Float moonSize = w instanceof ServerLevel ? ((ServerLevel)w).getMoonBrightness(e.blockPosition()) : null;
            variant = Variants.getRandomVariant(EntityType.SHEEP, ((Sheep)(Object)this).getRandom().nextLong(), w.getBiome(((Sheep)(Object)this).blockPosition()), null, moonSize);
        }
        hornColour = view.getStringOr(MoreMobVariants.SHEEP_HORN_COLOUR_NBT_KEY, "");
        MinecraftServer server = ((Entity)(Object)this).level().getServer();
        if (server != null) {
            server.getPlayerList().getPlayers().forEach((player) -> {
                ServerPlayNetworking.send(player, new ServerRespondVariantPayload(((Entity)(Object)this).getId(), variant.getIdentifier().toString(), false, 0, hornColour));
            });
        }
    }

    @Override
    protected void onFinalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData entityData, CallbackInfoReturnable<SpawnGroupData> ci) {
        variant = Variants.getRandomVariant(EntityType.SHEEP, ((Sheep)(Object)this).getRandom().nextLong(), world.getBiome(((Sheep)(Object)this).blockPosition()), null, world.getLevel().getMoonBrightness(((Sheep)(Object)this).blockPosition()));
        SheepHornSettings.SheepHornColour colour = SheepHornSettings.getRandomSheepHornColour(((Sheep)(Object)this).getRandom(), world.getBiome(((Sheep)(Object)this).blockPosition()));
        if (colour != null) {
            hornColour = colour.getId();
        }
        var data = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
        data.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
        data.putString(MoreMobVariants.SHEEP_HORN_COLOUR_NBT_KEY, hornColour);
        MoreMobVariants.setCustomDataNbt((Entity)(Object)this, data);
    }

    @Inject(method = "getBreedOffspring", at = @At("RETURN"))
    private void onGetBreedOffspring(ServerLevel world, AgeableMob entity, CallbackInfoReturnable<Sheep> ci) {
        Sheep child = ci.getReturnValue();
        MobVariant variant = Variants.getChildVariant(EntityType.SHEEP, world, ((Sheep)(Object)this), entity);
        var nbtParent1 = MoreMobVariants.getCustomDataNbt((Sheep)(Object)this);
        var nbtParent2 = MoreMobVariants.getCustomDataNbt(entity);
        String colour = "";
        if (!nbtParent1.getStringOr(MoreMobVariants.SHEEP_HORN_COLOUR_NBT_KEY, "").isEmpty()
                && !nbtParent2.getStringOr(MoreMobVariants.SHEEP_HORN_COLOUR_NBT_KEY, "").isEmpty()
                && entity.getRandom().nextDouble() <= SheepHornSettings.getInheritChance()) {
            colour = entity.getRandom().nextBoolean() ? nbtParent1.getStringOr(MoreMobVariants.SHEEP_HORN_COLOUR_NBT_KEY, "") : nbtParent2.getStringOr(MoreMobVariants.SHEEP_HORN_COLOUR_NBT_KEY, "");
        } else {
            SheepHornSettings.SheepHornColour col = SheepHornSettings.getRandomSheepHornColour(entity.getRandom(), world.getBiome(((Sheep)(Object)this).blockPosition()));
            if (col != null) {
                colour = col.getId();
            }
        }
        var childData = MoreMobVariants.getCustomDataNbt(child);
        childData.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
        childData.putString(MoreMobVariants.SHEEP_HORN_COLOUR_NBT_KEY, colour);
        MoreMobVariants.setCustomDataNbt(child, childData);
    }
}
