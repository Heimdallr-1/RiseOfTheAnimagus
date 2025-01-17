package com.windanesz.morphspellpack;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = MorphSpellPack.MODID, name = "morphspellpack") // No fancy configs here so we can use the annotation, hurrah!
public class Settings {

	@Config.Name("General Settings")
	@Config.LangKey("settings.morphspellpack:general_settings")
	public static GeneralSettings generalSettings = new GeneralSettings();

	@SuppressWarnings("unused")
	@Mod.EventBusSubscriber(modid = MorphSpellPack.MODID)
	private static class EventHandler {
		/**
		 * Inject the new values and save to the config file when the config has been changed from the GUI.
		 *
		 * @param event The event
		 */
		@SubscribeEvent
		public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
			if (event.getModID().equals(MorphSpellPack.MODID)) {
				ConfigManager.sync(MorphSpellPack.MODID, Config.Type.INSTANCE);
			}
		}
	}

	public static class GeneralSettings {
		@Config.Name("Skinchanger spell - mob blacklist")
		@Config.Comment("Players cannot morph into these mobs using the Skinchanger spell")
		public String[] skinchanger_banned_mobs = {""};

		@Config.Name("Skinchanger spell - disallow undeads")
		@Config.Comment("Players cannot morph into undeads using the Skinchanger spell")
		public boolean skinchanger_disallow_undeads = true;

		@Config.Name("Rabbitify spell - mob blacklist")
		@Config.Comment("Players cannot morph these entities using the Rabbitify spell")
		public String[] rabbitify_banned_mobs = {"minecraft:rabbit"};

		@Config.Name("Druid Stone - list is blacklist")
		@Config.Comment("If true: the Druid Stone - mob list acts as a blacklist. If False, the list acts as an entity whitelist")
		public boolean druid_stone_list_is_blacklist = true;

		@Config.Name("Druid Stone - mob list")
		@Config.Comment("Also see the other related setting")
		public String[] druid_stone_mob_list = {"minecraft:dragon", "minecraft:wither"};

		@Config.Name("Soul Phylactery - list is blacklist")
		@Config.Comment("If true: the Soul Phylactery - mob list acts as a blacklist. If False, the list acts as an entity whitelist")
		public boolean soul_phylactery_list_is_blacklist = true;

		@Config.Name("Lich Tweak - Take Damage without Soul Phylactery")
		@Config.Comment("If true: the lich takes damage if they don't have the soul phylactery")
		public boolean soul_phylactery_requirement_damage = false;

		@Config.Name("Lich Tweak - Apply Curse of Enfeeblement without Soul Phylactery")
		@Config.Comment("If true: the lich receives the curse if they don't have the soul phylactery")
		public boolean soul_phylactery_requirement_curse = true;

		@Config.Name("Soul Phylactery - mob list")
		@Config.Comment("Also see the other related setting")
		public String[] soul_phylactery_mob_list = {"minecraft:dragon", "minecraft:wither"};

		@Config.Name("Soul Phylactery - percent gain per kill")
		@Config.Comment("Must be between 0 and 1")
		public float soul_phylactery_percent_gain_per_kill = 0.05f;

		@Config.Name("Soul Phylactery - cost of use")
		@Config.Comment("Must be between 0 and 1")
		public float soul_phylactery_cost_of_use = 0.2f;

		@Config.Name("Lich Soul Receptacle - percent loss on death")
		@Config.Comment("Must be between 0 and 1")
		public float soul_receptacle_percent_lost_on_death = 0.33f;

		@Config.Name("Umbral Veil has particles")
		public boolean umbral_veil_particles = true;

		@Config.Name("Lich Soul Receptacle - Layer III Magic Crystal Block Mana Regen Amount (per every 5 seconds)")
		public int lich_soul_receptacle_layer_3_magic_crystal_block_mana_regen_amount = 3;

		@Config.Name("Lich Soul Receptacle - Layer II Max Distance for Spell Modifier Bonus")
		@Config.Comment("This sets the maximum distance for spell modifier bonuses of a given element for the receptacle's crystal pyramid's second layer. Setting it to"
				+ "1000 for example would mean that the lich gets bonus spell modifiers for spells of the crystal's element cast within 1000 blocks of the receptacle, but these bonuses are also"
				+ "diminished the further away the spell is cast. E.g. a spell cast from 800 blocks away would get a 20% bonus, while a spell cast from 1001 blocks away would get a 0% bonus.")
		public int lich_soul_receptacle_layer_2_max_distance = 600;

		@Config.Name("Lich Soul Receptacle Pyramid - layer 1 potency bonus")
		@Config.Comment("Grants the given amount of potency (1.12 = +12%) bonus for the crystal's element. Using a value <1 will reduce potency.")
		public float lich_soul_receptacle_pyramid_layer1_potency_bonus = 1.12f;

		@Config.Name("Curse of Transformation - mob list")
		@Config.Comment("Players can be transformed to these entities")
		public String[] curse_of_transformation_mob_list = {"morphspellpack:temporary_rabbit", "minecraft:chicken", "morphspellpack:bat_minion", "minecraft:pig", "minecraft:horse",
		"minecraft:mule", "minecraft:donkey", "minecraft:cow", "minecraft:sheep", "minecraft:ocelot"};

		@Config.Name("Potion of Lycanthropy - Werewolf Entity")
		@Config.Comment("Entity that players are transformed into when cursed by Lycanthropy")
		public String lycanthropy_werewolf_entity = "minecraft:wolf";

		@Config.Name("Druid Stone effect duration")
		@Config.Comment("The Druid Stone artefact will transform the player for this duration in ticks")
		public int druid_stone_duration = 300;
	}
}
