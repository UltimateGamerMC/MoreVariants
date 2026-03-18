package com.github.nyuppo;

import com.github.nyuppo.client.render.entity.feature.PigMudFeatureRenderer;
import com.github.nyuppo.client.render.entity.feature.ShearedWoolColorFeatureRenderer;
import com.github.nyuppo.client.render.entity.feature.SheepHornsFeatureRenderer;
import com.github.nyuppo.networking.ClientRequestVariantPayload;
import com.github.nyuppo.networking.MMVNetworkingConstants;
import com.github.nyuppo.networking.ServerRespondBasicVariantPayload;
import com.github.nyuppo.networking.ServerRespondVariantPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.render.entity.PigEntityRenderer;
import net.minecraft.client.render.entity.SheepEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MoreMobVariantsClient implements ClientModInitializer {
    Set<EntityType> validEntities = Set.of(EntityType.CAT, EntityType.CHICKEN, EntityType.COW, EntityType.PIG, EntityType.SHEEP, EntityType.SKELETON, EntityType.SPIDER, EntityType.WOLF, EntityType.ZOMBIE);
    public static final Map<Integer, String> VARIANT_CACHE = new ConcurrentHashMap<>();
    private static final Map<Integer, Long> LAST_VARIANT_REQUEST = new ConcurrentHashMap<>();
    private static final long REQUEST_COOLDOWN_MS = 2000;

    public static void requestVariantIfNeeded(Entity entity) {
        if (entity == null || entity.getEntityWorld() == null) return;
        int id = entity.getId();
        if (VARIANT_CACHE.containsKey(id)) return;
        long now = System.currentTimeMillis();
        if (now - LAST_VARIANT_REQUEST.getOrDefault(id, 0L) < REQUEST_COOLDOWN_MS) return;
        LAST_VARIANT_REQUEST.put(id, now);
        ClientPlayNetworking.send(new ClientRequestVariantPayload(entity.getUuid()));
    }

    @Override
    public void onInitializeClient() {
        // Add render layers
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityRenderer instanceof PigEntityRenderer) {
                registrationHelper.register((net.minecraft.client.render.entity.feature.FeatureRenderer) new PigMudFeatureRenderer((FeatureRendererContext) entityRenderer));
            } else if (entityRenderer instanceof SheepEntityRenderer) {
                registrationHelper.register((net.minecraft.client.render.entity.feature.FeatureRenderer) new ShearedWoolColorFeatureRenderer((FeatureRendererContext) entityRenderer));
                registrationHelper.register((net.minecraft.client.render.entity.feature.FeatureRenderer) new SheepHornsFeatureRenderer((FeatureRendererContext) entityRenderer));
            }
        });

        // Client event to request variant when a mob is loaded
        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            // Check if entity can have variants so we don't make useless requests
            if (validEntities.contains(entity.getType())) {
                ClientPlayNetworking.send(new ClientRequestVariantPayload(entity.getUuid()));
            }
        });

        // Client event to handle response from server about basic mob variants
        ClientPlayNetworking.registerGlobalReceiver(ServerRespondBasicVariantPayload.ID, ((payload, context) -> {
            var client = context.client();
            int id = payload.entityId();
            String variantId = payload.variantId();
            VARIANT_CACHE.put(id, variantId);
            client.execute(() -> {
                if (client.world != null) {
                    Entity entity = client.world.getEntityById(id);
                    if (entity != null) {
                        NbtCompound nbt = MoreMobVariants.getCustomDataNbt(entity);
                        nbt.putString(MoreMobVariants.NBT_KEY, variantId);
                        MoreMobVariants.setCustomDataNbt(entity, nbt);
                    }
                }
            });
        }));


        // Client event to handle response from server about complex mob variants
        ClientPlayNetworking.registerGlobalReceiver(ServerRespondVariantPayload.ID, ((payload, context) -> {
            var client = context.client();
            int id = payload.entityId();
            String variantId = payload.variant();

            //read all three buffer values regardless, in the Netty loop. use as needed in the client.
            boolean bl = payload.sittingOrMuddy();
            int i = payload.muddyTimeout();
            String str = payload.sheepHornColour();

            VARIANT_CACHE.put(id, variantId);
            client.execute(() -> {
                if (client.world != null) {
                    Entity entity = client.world.getEntityById(id);
                    if (entity != null) {
                        NbtCompound nbt = MoreMobVariants.getCustomDataNbt(entity);
                        nbt.putString(MoreMobVariants.NBT_KEY, variantId);
                        if (entity instanceof TameableEntity) {
                            nbt.putBoolean("Sitting", bl);
                        }
                        if (entity instanceof PigEntity) {
                            nbt.putBoolean(MoreMobVariants.MUDDY_NBT_KEY, bl);
                            nbt.putInt(MoreMobVariants.MUDDY_TIMEOUT_NBT_KEY, i);
                        }
                        if (entity instanceof SheepEntity) {
                            nbt.putString(MoreMobVariants.SHEEP_HORN_COLOUR_NBT_KEY, str);
                        }
                        MoreMobVariants.setCustomDataNbt(entity, nbt);
                    }
                }
            });
        }));

        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            VARIANT_CACHE.remove(entity.getId());
            LAST_VARIANT_REQUEST.remove(entity.getId());
        });
    }

}