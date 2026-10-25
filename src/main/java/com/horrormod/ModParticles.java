package com.horrormod;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModParticles {
	/** A little harmless bat (from the Bat Potion). */
	public static final SimpleParticleType BAT = Registry.register(BuiltInRegistries.PARTICLE_TYPE, HorrorMod.id("bat"), FabricParticleTypes.simple());

	private ModParticles() {
	}

	public static void init() {
	}
}
