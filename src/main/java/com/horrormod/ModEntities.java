package com.horrormod;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

public final class ModEntities {
	public static final ResourceKey<EntityType<?>> SPOOKY_SPIRIT_KEY = ResourceKey.create(Registries.ENTITY_TYPE, HorrorMod.id("spooky_spirit"));

	/** The Spooky Spirit: a small clear, see-through ghost that floats around. */
	public static final EntityType<SpookySpirit> SPOOKY_SPIRIT = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			SPOOKY_SPIRIT_KEY,
			EntityType.Builder.<SpookySpirit>of(SpookySpirit::new, MobCategory.AMBIENT)
					.sized(0.6F, 1.1F)
					.eyeHeight(0.8F)
					.clientTrackingRange(8)
					.build(SPOOKY_SPIRIT_KEY));

	public static final ResourceKey<EntityType<?>> SUPER_PHANTOM_KEY = ResourceKey.create(Registries.ENTITY_TYPE, HorrorMod.id("super_phantom"));

	/** The Super Phantom: a big dark phantom that snatches one little item, never hurts anyone. */
	public static final EntityType<SuperPhantom> SUPER_PHANTOM = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			SUPER_PHANTOM_KEY,
			EntityType.Builder.<SuperPhantom>of(SuperPhantom::new, MobCategory.AMBIENT)
					.sized(1.6F, 0.8F)
					.eyeHeight(0.4F)
					.clientTrackingRange(10)
					.build(SUPER_PHANTOM_KEY));

	private ModEntities() {
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(SPOOKY_SPIRIT, SpookySpirit.createAttributes());
		SpawnPlacements.register(SPOOKY_SPIRIT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpookySpirit::checkSpiritSpawnRules);
		// Rare: weight 2 (bats are 10), one spirit at a time, only in the dark (at night or in caves).
		BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.AMBIENT, SPOOKY_SPIRIT, 2, 1, 1);
		FabricDefaultAttributeRegistry.register(SUPER_PHANTOM, SuperPhantom.createAttributes());
		SpawnPlacements.register(SUPER_PHANTOM, SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING, SuperPhantom::checkSuperPhantomSpawnRules);
		// Very rare: weight 1 plus an extra 1-in-4 check, only at night under the open sky.
		BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.AMBIENT, SUPER_PHANTOM, 1, 1, 1);
	}
}
