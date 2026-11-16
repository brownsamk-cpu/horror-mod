package com.horrormod.cave;

import com.horrormod.HorrorMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/** 0.9.0 Stony Cave entities. The Stone Monster never spawns on its own: only from the Stony Cave item or its spawn egg. */
public final class CaveEntities {
	public static final ResourceKey<EntityType<?>> STONE_MONSTER_KEY = ResourceKey.create(Registries.ENTITY_TYPE, HorrorMod.id("stone_monster"));
	public static final EntityType<StoneMonster> STONE_MONSTER = Registry.register(BuiltInRegistries.ENTITY_TYPE, STONE_MONSTER_KEY,
			EntityType.Builder.<StoneMonster>of(StoneMonster::new, MobCategory.MONSTER)
					.sized(2.0F, 5.0F)
					.eyeHeight(4.3F)
					.fireImmune()
					.clientTrackingRange(10)
					.build(STONE_MONSTER_KEY));

	public static final ResourceKey<EntityType<?>> STONE_ROCK_KEY = ResourceKey.create(Registries.ENTITY_TYPE, HorrorMod.id("stone_rock"));
	public static final EntityType<StoneRock> STONE_ROCK = Registry.register(BuiltInRegistries.ENTITY_TYPE, STONE_ROCK_KEY,
			EntityType.Builder.<StoneRock>of(StoneRock::new, MobCategory.MISC)
					.sized(0.4F, 0.4F)
					.clientTrackingRange(4)
					.updateInterval(10)
					.build(STONE_ROCK_KEY));

	private CaveEntities() {
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(STONE_MONSTER, StoneMonster.createAttributes());
	}
}
