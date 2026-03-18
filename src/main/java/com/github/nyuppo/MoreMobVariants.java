package com.github.nyuppo;

import com.github.nyuppo.config.ConfigDataLoader;
import com.github.nyuppo.networking.ClientRequestVariantPayload;
import com.github.nyuppo.networking.ServerRespondBasicVariantPayload;
import com.github.nyuppo.networking.ServerRespondVariantPayload;
import com.github.nyuppo.util.MMVTrackedData;
import net.minecraft.entity.passive.ChickenEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class MoreMobVariants implements ModInitializer {
    public static final String MOD_ID = "moremobvariants";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // NBT keys
    public static final String NBT_KEY = "VariantID";
    public static final String MUDDY_NBT_KEY = "IsMuddy"; // Muddy pigs
    public static final String MUDDY_TIMEOUT_NBT_KEY = "MuddyTimeLeft"; // Muddy pigs
    public static final String SHEEP_HORN_COLOUR_NBT_KEY = "HornColour";

    // Pig mud tag
    public static final TagKey<Block> PIG_MUD_BLOCKS = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "pig_mud_blocks"));

    // Biome tags
    public static final TagKey<Biome> SPAWN_MOSSY_SKELETONS = TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "spawn_mossy_skeletons"));
    public static final TagKey<Biome> INCREASED_SANDY_SKELETONS = TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "increased_sandy_skeletons"));
    public static final TagKey<Biome> SHEEP_SPAWN_WITH_HORNS = TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "sheep_spawn_with_horns"));
    public static final TagKey<Biome> SPAWN_PALE_WOLF = TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "wolf_pale_spawns"));
    public static final TagKey<Biome> SPAWN_RUSTY_WOLF = TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "wolf_rusty_spawns"));
    public static final TagKey<Biome> SPAWN_SPOTTED_WOLF = TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "wolf_spotted_spawns"));
    public static final TagKey<Biome> SPAWN_BLACK_WOLF = TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "wolf_black_spawns"));
    public static final TagKey<Biome> SPAWN_STRIPED_WOLF = TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "wolf_striped_spawns"));
    public static final TagKey<Biome> SPAWN_SNOWY_WOLF = TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "wolf_snowy_spawns"));
    public static final TagKey<Biome> SPAWN_ASHEN_WOLF = TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "wolf_ashen_spawns"));
    public static final TagKey<Biome> SPAWN_WOODS_WOLF = TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "wolf_woods_spawns"));
    public static final TagKey<Biome> SPAWN_CHESTNUT_WOLF = TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "wolf_chestnut_spawns"));
    public static final TagKey<Biome> ADDITIONAL_WOLF_SPAWNS = TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "additional_wolf_spawns"));

    @Override
    public void onInitialize() {
        LOGGER.info("Giving mobs a fresh coat of paint...");

        // Config
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ConfigDataLoader());

        // Make wolves spawn in more biomes
        BiomeModifications.addSpawn(BiomeSelectors.tag(ADDITIONAL_WOLF_SPAWNS), SpawnGroup.CREATURE, EntityType.WOLF, 5, 4, 4);

        // Register payloads
        PayloadTypeRegistry.playC2S().register(ClientRequestVariantPayload.ID, ClientRequestVariantPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(ServerRespondBasicVariantPayload.ID, ServerRespondBasicVariantPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(ServerRespondVariantPayload.ID, ServerRespondVariantPayload.PACKET_CODEC);

        // Server event to respond to client request for a variant
        ServerPlayNetworking.registerGlobalReceiver(ClientRequestVariantPayload.ID, ((payload, context) -> {
            var server = context.server();
            var uuid = payload.uuid();
            server.execute( () -> {
                Entity entity = server.getOverworld().getEntity(uuid);

                // If we couldn't find the mob in the overworld, start checking all other worlds
                if (entity == null) {
                    for (ServerWorld serverWorld : server.getWorlds()) {
                        Entity entity2 = serverWorld.getEntity(uuid);
                        if (entity2 != null) {
                            entity = entity2;
                        }
                    }
                }

                if (entity != null) {
                    if (entity instanceof ChickenEntity chicken) {
                        String vid = chicken.getDataTracker().get(MMVTrackedData.CHICKEN_VARIANT_ID);
                        if (vid != null && !vid.isEmpty()) {
                            ServerPlayNetworking.send(context.player(), new ServerRespondBasicVariantPayload(entity.getId(), vid));
                            return;
                        }
                    }
                    NbtCompound nbt = getCustomDataNbt(entity);
                    if (nbt.contains(NBT_KEY)) {
                        boolean bl = false;
                        int i = 0;
                        String str = "";

                        if (entity instanceof TameableEntity) {
                            bl = nbt.getBoolean("Sitting", false);
                        }

                        if (entity instanceof PigEntity) {
                            bl = nbt.getBoolean(MUDDY_NBT_KEY, false);
                            i = nbt.getInt(MUDDY_TIMEOUT_NBT_KEY, 0);
                        }

                        if (entity instanceof SheepEntity) {
                            str = nbt.getString(SHEEP_HORN_COLOUR_NBT_KEY, "");
                        }

                        var responsePayload = new ServerRespondVariantPayload(entity.getId(), nbt.getString(NBT_KEY, ""), bl, i, str);
                        ServerPlayNetworking.send(context.player(), responsePayload);
                    }
                }
            });
        }));
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static boolean isTemperateBiome(RegistryEntry<Biome> biome) {
        return biome != null && !biome.isIn(BiomeTags.SPAWNS_COLD_VARIANT_FARM_ANIMALS) && !biome.isIn(BiomeTags.SPAWNS_WARM_VARIANT_FARM_ANIMALS);
    }

    public static NbtCompound getCustomDataNbt(Entity entity) {
        NbtComponent c = entity.get(DataComponentTypes.CUSTOM_DATA);
        return c != null ? c.copyNbt() : new NbtCompound();
    }

    public static void setCustomDataNbt(Entity entity, NbtCompound nbt) {
        entity.setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }
}