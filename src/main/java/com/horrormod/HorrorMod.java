package com.horrormod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

/** Horror Mod: spooky-fun (Halloween, not scary) things, one step at a time. Step 1 is the Ghost, step 2 the Super Phantom. */
public class HorrorMod implements ModInitializer {
	public static final String MOD_ID = "horrormod";

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModSounds.init();
		ModEntities.init();
		ModItems.init();
	}
}
