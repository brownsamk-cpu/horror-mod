package com.horrormod;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;

/**
 * The Mysterious Witch's 4 brand-new potions, each with its own custom status effect. Names, colors and durations come
 * from data/horrormod/mysterious_potions.json, which tools/mysterious_witch.py writes. Vanilla turns every registered
 * potion into a drinkable, splash and lingering potion in the Creative menu automatically.
 */
public final class MysteriousPotions {
	public record Entry(String id, int color, int duration, Holder<MobEffect> effect, Holder<Potion> potion) {
	}

	public static final Map<String, Entry> ALL = new LinkedHashMap<>();
	public static final Holder<MobEffect> BOUNCY;
	public static final Holder<MobEffect> GLOW;
	public static final Holder<MobEffect> BAT;
	public static final Holder<MobEffect> SLIME_FEET;

	static {
		JsonArray data;
		try (var in = MysteriousPotions.class.getResourceAsStream("/data/horrormod/mysterious_potions.json")) {
			data = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonArray();
		} catch (Exception e) {
			throw new RuntimeException("Horror Mod: can't read mysterious_potions.json", e);
		}
		for (JsonElement el : data) {
			JsonObject o = el.getAsJsonObject();
			String id = o.get("id").getAsString();
			int color = Integer.parseInt(o.get("color").getAsString().substring(1), 16);
			int duration = o.get("duration").getAsInt();
			MobEffect effect = switch (id) {
				case "bouncy" -> new MysteriousEffects.Bouncy(color);
				case "glow" -> new MysteriousEffects.Glow(color);
				case "bat" -> new MysteriousEffects.Bat(color);
				case "slime_feet" -> new MysteriousEffects.SlimeFeet(color);
				default -> throw new IllegalStateException("unknown potion " + id);
			};
			Holder<MobEffect> eh = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, HorrorMod.id(id), effect);
			Holder<Potion> ph = Registry.registerForHolder(BuiltInRegistries.POTION, ResourceKey.create(Registries.POTION, HorrorMod.id(id)),
					new Potion("horrormod." + id, new MobEffectInstance(eh, duration)));
			ALL.put(id, new Entry(id, color, duration, eh, ph));
		}
		BOUNCY = ALL.get("bouncy").effect();
		GLOW = ALL.get("glow").effect();
		BAT = ALL.get("bat").effect();
		SLIME_FEET = ALL.get("slime_feet").effect();
	}

	private MysteriousPotions() {
	}

	public static List<Holder<Potion>> potions() {
		List<Holder<Potion>> out = new ArrayList<>();
		ALL.values().forEach(e -> out.add(e.potion()));
		return out;
	}

	public static void init() {
	}
}
