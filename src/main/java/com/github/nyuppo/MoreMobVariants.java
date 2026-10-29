package com.github.nyuppo;

import com.github.nyuppo.config.ConfigDataLoader;
import com.github.nyuppo.networking.ClientRequestVariantPayload;
import com.github.nyuppo.networking.ServerRespondBasicVariantPayload;
import com.github.nyuppo.networking.ServerRespondVariantPayload;
import com.github.nyuppo.util.MMVTrackedData;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.core.Holder;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoreMobVariants implements ModInitializer {
	public static final String MOD_ID = "moremobvariants";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final String NBT_KEY = "VariantID";
	public static final String MUDDY_NBT_KEY = "IsMuddy";
	public static final String MUDDY_TIMEOUT_NBT_KEY = "MuddyTimeLeft";
	public static final String SHEEP_HORN_COLOUR_NBT_KEY = "HornColour";
	public static final TagKey<Block> PIG_MUD_BLOCKS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "pig_mud_blocks"));
	public static final TagKey<Biome> SPAWN_MOSSY_SKELETONS = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, "spawn_mossy_skeletons"));
	public static final TagKey<Biome> INCREASED_SANDY_SKELETONS = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, "increased_sandy_skeletons"));
	public static final TagKey<Biome> SHEEP_SPAWN_WITH_HORNS = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, "sheep_spawn_with_horns"));
	public static final TagKey<Biome> SPAWN_PALE_WOLF = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, "wolf_pale_spawns"));
	public static final TagKey<Biome> SPAWN_RUSTY_WOLF = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, "wolf_rusty_spawns"));
	public static final TagKey<Biome> SPAWN_SPOTTED_WOLF = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, "wolf_spotted_spawns"));
	public static final TagKey<Biome> SPAWN_BLACK_WOLF = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, "wolf_black_spawns"));
	public static final TagKey<Biome> SPAWN_STRIPED_WOLF = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, "wolf_striped_spawns"));
	public static final TagKey<Biome> SPAWN_SNOWY_WOLF = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, "wolf_snowy_spawns"));
	public static final TagKey<Biome> SPAWN_ASHEN_WOLF = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, "wolf_ashen_spawns"));
	public static final TagKey<Biome> SPAWN_WOODS_WOLF = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, "wolf_woods_spawns"));
	public static final TagKey<Biome> SPAWN_CHESTNUT_WOLF = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, "wolf_chestnut_spawns"));
	public static final TagKey<Biome> ADDITIONAL_WOLF_SPAWNS = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, "additional_wolf_spawns"));

	@Override
	public void onInitialize() {
		LOGGER.info("Giving mobs a fresh coat of paint...");
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(MoreMobVariants.id(MoreMobVariants.MOD_ID), new ConfigDataLoader());
		BiomeModifications.addSpawn(BiomeSelectors.tag(ADDITIONAL_WOLF_SPAWNS), MobCategory.CREATURE, EntityTypes.WOLF, 5, 4, 4);
		PayloadTypeRegistry.serverboundPlay().register(ClientRequestVariantPayload.TYPE, ClientRequestVariantPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ServerRespondBasicVariantPayload.TYPE, ServerRespondBasicVariantPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ServerRespondVariantPayload.TYPE, ServerRespondVariantPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ClientRequestVariantPayload.TYPE, (payload, context) -> {
			var server = context.server();
			var uuid = payload.uuid();
			server.execute(() -> {
				Entity entity = server.overworld().getEntity(uuid);
				if (entity == null) {
					for (ServerLevel serverWorld : server.getAllLevels()) {
						Entity entity2 = serverWorld.getEntity(uuid);
						if (entity2 != null) {
							entity = entity2;
						}
					}
				}
				if (entity != null) {
					if (entity instanceof Chicken chicken) {
						String vid = chicken.getEntityData().get(MMVTrackedData.CHICKEN_VARIANT_ID);
						if (vid != null && !vid.isEmpty()) {
							ServerPlayNetworking.send(context.player(), new ServerRespondBasicVariantPayload(entity.getId(), vid));
							return;
						}
					}
					CompoundTag nbt = getCustomDataNbt(entity);
					if (nbt.contains(NBT_KEY)) {
						boolean bl = false;
						int i = 0;
						String str = "";
						if (entity instanceof TamableAnimal) {
							bl = nbt.getBooleanOr("Sitting", false);
						}
						if (entity instanceof Pig) {
							bl = nbt.getBooleanOr(MUDDY_NBT_KEY, false);
							i = nbt.getIntOr(MUDDY_TIMEOUT_NBT_KEY, 0);
						}
						if (entity instanceof Sheep) {
							str = nbt.getStringOr(SHEEP_HORN_COLOUR_NBT_KEY, "");
						}
						var responsePayload = new ServerRespondVariantPayload(entity.getId(), nbt.getStringOr(NBT_KEY, ""), bl, i, str);
						ServerPlayNetworking.send(context.player(), responsePayload);
					}
				}
			});
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	public static boolean isTemperateBiome(Holder<Biome> biome) {
		return biome != null && !biome.is(net.minecraft.tags.BiomeTags.SPAWNS_COLD_VARIANT_FARM_ANIMALS) && !biome.is(net.minecraft.tags.BiomeTags.SPAWNS_WARM_VARIANT_FARM_ANIMALS);
	}

	public static CompoundTag getCustomDataNbt(Entity entity) {
		CustomData c = entity.get(DataComponents.CUSTOM_DATA);
		return c != null ? c.copyTag() : new CompoundTag();
	}

	public static void setCustomDataNbt(Entity entity, CompoundTag nbt) {
		entity.setComponent(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
	}

	/**
	 * Custom data is what gets saved and what clients are sent when they ask for a mob's variant, so a variant
	 * read from NBT (summons, structures, older saves) has to land there too or the mob renders as vanilla
	 * and loses its variant on the next save.
	 */
	public static void storeVariant(Entity entity, String variantId) {
		CompoundTag nbt = getCustomDataNbt(entity);
		nbt.putString(NBT_KEY, variantId);
		setCustomDataNbt(entity, nbt);
	}
}
