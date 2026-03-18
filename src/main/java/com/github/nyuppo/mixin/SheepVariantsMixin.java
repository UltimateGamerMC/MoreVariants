package com.github.nyuppo.mixin;

import com.github.nyuppo.MoreMobVariants;
import com.github.nyuppo.config.SheepHornSettings;
import com.github.nyuppo.config.Variants;
import com.github.nyuppo.networking.ServerRespondVariantPayload;
import com.github.nyuppo.variant.MobVariant;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SheepEntity.class)
public abstract class SheepVariantsMixin extends MobEntityVariantsMixin {
    private MobVariant variant = Variants.getDefaultVariant(EntityType.SHEEP);
    private String hornColour = "";

    @Override
    protected void onWriteCustomData(WriteView view, CallbackInfo ci) {
        view.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
        view.putString(MoreMobVariants.SHEEP_HORN_COLOUR_NBT_KEY, hornColour);
    }

    @Override
    protected void onReadCustomData(ReadView view, CallbackInfo ci) {
        String variantStr = view.getString(MoreMobVariants.NBT_KEY, "");
        if (!variantStr.isEmpty()) {
            if (variantStr.contains(":")) {
                variant = Variants.getVariant(EntityType.SHEEP, Identifier.of(variantStr));
            } else {
                variant = Variants.getVariant(EntityType.SHEEP, MoreMobVariants.id(variantStr));
            }
        } else {
            Entity e = (Entity)(Object)this;
            World w = e.getEntityWorld();
            Float moonSize = w instanceof ServerWorld ? ((ServerWorld)w).getMoonSize(e.getBlockPos()) : null;
            variant = Variants.getRandomVariant(EntityType.SHEEP, ((SheepEntity)(Object)this).getRandom().nextLong(), w.getBiome(((SheepEntity)(Object)this).getBlockPos()), null, moonSize);
        }
        hornColour = view.getString(MoreMobVariants.SHEEP_HORN_COLOUR_NBT_KEY, "");
        MinecraftServer server = ((Entity)(Object)this).getEntityWorld().getServer();
        if (server != null) {
            server.getPlayerManager().getPlayerList().forEach((player) -> {
                ServerPlayNetworking.send(player, new ServerRespondVariantPayload(((Entity)(Object)this).getId(), variant.getIdentifier().toString(), false, 0, hornColour));
            });
        }
    }

    @Override
    protected void onInitialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, CallbackInfoReturnable<EntityData> ci) {
        variant = Variants.getRandomVariant(EntityType.SHEEP, ((SheepEntity)(Object)this).getRandom().nextLong(), world.getBiome(((SheepEntity)(Object)this).getBlockPos()), null, world.toServerWorld().getMoonSize(((SheepEntity)(Object)this).getBlockPos()));
        SheepHornSettings.SheepHornColour colour = SheepHornSettings.getRandomSheepHornColour(((SheepEntity)(Object)this).getRandom(), world.getBiome(((SheepEntity)(Object)this).getBlockPos()));
        if (colour != null) {
            hornColour = colour.getId();
        }
        var data = MoreMobVariants.getCustomDataNbt((Entity)(Object)this);
        data.putString(MoreMobVariants.NBT_KEY, variant.getIdentifier().toString());
        data.putString(MoreMobVariants.SHEEP_HORN_COLOUR_NBT_KEY, hornColour);
        MoreMobVariants.setCustomDataNbt((Entity)(Object)this, data);
    }

    @Inject(
            method = "createChild(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/PassiveEntity;)Lnet/minecraft/entity/passive/SheepEntity;",
            at = @At("RETURN")
    )
    private void onCreateChild(ServerWorld world, PassiveEntity entity, CallbackInfoReturnable<SheepEntity> ci) {
        SheepEntity child = ci.getReturnValue();
        MobVariant variant = Variants.getChildVariant(EntityType.SHEEP, world, ((SheepEntity)(Object)this), entity);
        var nbtParent1 = MoreMobVariants.getCustomDataNbt((SheepEntity)(Object)this);
        var nbtParent2 = MoreMobVariants.getCustomDataNbt(entity);
        String colour = "";
        if (!nbtParent1.getString(MoreMobVariants.SHEEP_HORN_COLOUR_NBT_KEY, "").isEmpty()
                && !nbtParent2.getString(MoreMobVariants.SHEEP_HORN_COLOUR_NBT_KEY, "").isEmpty()
                && entity.getRandom().nextDouble() <= SheepHornSettings.getInheritChance()) {
            colour = entity.getRandom().nextBoolean() ? nbtParent1.getString(MoreMobVariants.SHEEP_HORN_COLOUR_NBT_KEY, "") : nbtParent2.getString(MoreMobVariants.SHEEP_HORN_COLOUR_NBT_KEY, "");
        } else {
            SheepHornSettings.SheepHornColour col = SheepHornSettings.getRandomSheepHornColour(entity.getRandom(), world.getBiome(((SheepEntity)(Object)this).getBlockPos()));
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
