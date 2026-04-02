package com.github.nyuppo;

import com.github.nyuppo.client.render.MMVEntityModelLayers;
import com.github.nyuppo.client.render.entity.feature.PigMudFeatureRenderer;
import com.github.nyuppo.client.render.entity.feature.ShearedWoolColorFeatureRenderer;
import com.github.nyuppo.client.render.entity.feature.SheepHornsFeatureRenderer;
import com.github.nyuppo.networking.ServerRespondBasicVariantPayload;
import com.github.nyuppo.networking.ServerRespondVariantPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MoreMobVariantsClient implements ClientModInitializer {
	@SuppressWarnings("unchecked")
	private static <T extends EntityRenderState> RenderLayer<T, ? extends EntityModel<T>> castLayer(RenderLayer<?, ?> layer) {
		return (RenderLayer<T, ? extends EntityModel<T>>) layer;
	}

	Set<EntityType<?>> validEntities = Set.of(EntityType.CAT, EntityType.CHICKEN, EntityType.COW, EntityType.PIG, EntityType.SHEEP, EntityType.SKELETON, EntityType.SPIDER, EntityType.WOLF, EntityType.ZOMBIE);
	public static final Map<Integer, String> VARIANT_CACHE = new ConcurrentHashMap<>();
	private static final Map<Integer, Long> LAST_VARIANT_REQUEST = new ConcurrentHashMap<>();
	private static final long REQUEST_COOLDOWN_MS = 2000;
	public static void requestVariantIfNeeded(Entity entity) {
		if (entity == null || entity.level() == null) return;
		int id = entity.getId();
		if (VARIANT_CACHE.containsKey(id)) return;
		long now = System.currentTimeMillis();
		if (now - LAST_VARIANT_REQUEST.getOrDefault(id, 0L) < REQUEST_COOLDOWN_MS) return;
		LAST_VARIANT_REQUEST.put(id, now);
		ClientPlayNetworking.send(new com.github.nyuppo.networking.ClientRequestVariantPayload(entity.getUUID()));
	}

	@Override
	public void onInitializeClient() {
		LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityRenderer instanceof PigRenderer pigRenderer) {
				registrationHelper.register(castLayer(new PigMudFeatureRenderer(pigRenderer)));
			} else if (entityRenderer instanceof SheepRenderer sheepRenderer) {
				registrationHelper.register(castLayer(new ShearedWoolColorFeatureRenderer(sheepRenderer)));
				registrationHelper.register(castLayer(new SheepHornsFeatureRenderer(sheepRenderer, context.bakeLayer(MMVEntityModelLayers.SHEEP_HORNS))));
			}
		});

		ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (validEntities.contains(entity.getType())) {
				ClientPlayNetworking.send(new com.github.nyuppo.networking.ClientRequestVariantPayload(entity.getUUID()));
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(ServerRespondBasicVariantPayload.TYPE, (payload, context) -> {
			var client = context.client();
			int id = payload.entityId();
			String variantId = payload.variantId();
			VARIANT_CACHE.put(id, variantId);
			client.execute(() -> {
				if (client.level != null) {
					Entity entity = client.level.getEntity(id);
					if (entity != null) {
						CompoundTag nbt = MoreMobVariants.getCustomDataNbt(entity);
						nbt.putString(MoreMobVariants.NBT_KEY, variantId);
						MoreMobVariants.setCustomDataNbt(entity, nbt);
					}
				}
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(ServerRespondVariantPayload.TYPE, (payload, context) -> {
			var client = context.client();
			int id = payload.entityId();
			String variantId = payload.variant();
			boolean bl = payload.sittingOrMuddy();
			int i = payload.muddyTimeout();
			String str = payload.sheepHornColour();
			VARIANT_CACHE.put(id, variantId);
			client.execute(() -> {
				if (client.level != null) {
					Entity entity = client.level.getEntity(id);
					if (entity != null) {
						CompoundTag nbt = MoreMobVariants.getCustomDataNbt(entity);
						nbt.putString(MoreMobVariants.NBT_KEY, variantId);
						if (entity instanceof TamableAnimal) {
							nbt.putBoolean("Sitting", bl);
						}
						if (entity instanceof Pig) {
							nbt.putBoolean(MoreMobVariants.MUDDY_NBT_KEY, bl);
							nbt.putInt(MoreMobVariants.MUDDY_TIMEOUT_NBT_KEY, i);
						}
						if (entity instanceof Sheep) {
							nbt.putString(MoreMobVariants.SHEEP_HORN_COLOUR_NBT_KEY, str);
						}
						MoreMobVariants.setCustomDataNbt(entity, nbt);
					}
				}
			});
		});

		ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
			VARIANT_CACHE.remove(entity.getId());
			LAST_VARIANT_REQUEST.remove(entity.getId());
		});
	}
}
