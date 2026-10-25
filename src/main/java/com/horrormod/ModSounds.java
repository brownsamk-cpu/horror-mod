package com.horrormod;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

/** The Spooky Spirit's own soft "wooo" (synthesized by tools/spooky_spirit.py). */
public final class ModSounds {
	public static final SoundEvent SPOOKY_SPIRIT_WOOO = register("entity.spooky_spirit.wooo");

	private ModSounds() {
	}

	private static SoundEvent register(String name) {
		return Registry.register(BuiltInRegistries.SOUND_EVENT, HorrorMod.id(name), SoundEvent.createVariableRangeEvent(HorrorMod.id(name)));
	}

	public static void init() {
	}
}
