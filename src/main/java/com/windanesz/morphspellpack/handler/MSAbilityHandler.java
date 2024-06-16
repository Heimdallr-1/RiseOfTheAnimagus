package com.windanesz.morphspellpack.handler;

import com.windanesz.morphspellpack.ability.AbilityDisguise;
import com.windanesz.morphspellpack.ability.AbilityItemSpawn;
import com.windanesz.morphspellpack.ability.AbilityLichPhylactery;
import com.windanesz.morphspellpack.ability.AbilityLichSunburn;
import com.windanesz.morphspellpack.ability.AbilityPassivePotion;
import com.windanesz.morphspellpack.ability.AbilityPigMushroomSpawn;
import com.windanesz.morphspellpack.ability.AbilityPotionResistance;
import com.windanesz.morphspellpack.ability.AbilitySelfDetonate;
//import com.windanesz.morphspellpack.ability.AbilitySniffPlayer;
import com.windanesz.morphspellpack.ability.AbilitySniffPlayer;
import com.windanesz.morphspellpack.ability.AbilitySpell;
import com.windanesz.morphspellpack.ability.AbilitySpellFromArtefact;
import com.windanesz.morphspellpack.ability.AbilityTimedPotion;
import com.windanesz.morphspellpack.ability.AbilityWaterBreath;
import com.windanesz.morphspellpack.ability.AbilityWebWalk;
import com.windanesz.morphspellpack.ability.AbilityHover;
import me.ichun.mods.morph.common.handler.AbilityHandler;

public class MSAbilityHandler {

	public static void preInit() {
		AbilityHandler.getInstance().registerAbility(AbilitySpell.name, AbilitySpell.class);
		AbilityHandler.getInstance().registerAbility(AbilitySpellFromArtefact.name, AbilitySpellFromArtefact.class);
		AbilityHandler.getInstance().registerAbility(AbilityHover.name, AbilityHover.class);
		AbilityHandler.getInstance().registerAbility(AbilitySelfDetonate.name, AbilitySelfDetonate.class);
		AbilityHandler.getInstance().registerAbility(AbilityWebWalk.name, AbilityWebWalk.class);
		AbilityHandler.getInstance().registerAbility(AbilityDisguise.name, AbilityDisguise.class);
		AbilityHandler.getInstance().registerAbility(AbilityTimedPotion.name, AbilityTimedPotion.class);
		AbilityHandler.getInstance().registerAbility(AbilityLichPhylactery.name, AbilityLichPhylactery.class);
		AbilityHandler.getInstance().registerAbility(AbilityPotionResistance.name, AbilityPotionResistance.class);
		AbilityHandler.getInstance().registerAbility(AbilityWaterBreath.name, AbilityWaterBreath.class);
		AbilityHandler.getInstance().registerAbility(AbilityItemSpawn.name, AbilityItemSpawn.class);
		AbilityHandler.getInstance().registerAbility(AbilityPigMushroomSpawn.name, AbilityPigMushroomSpawn.class);
		AbilityHandler.getInstance().registerAbility(AbilityPassivePotion.name, AbilityPassivePotion.class);
		AbilityHandler.getInstance().registerAbility(AbilitySniffPlayer.name, AbilitySniffPlayer.class);
		AbilityHandler.getInstance().registerAbility(AbilityLichSunburn.name, AbilityLichSunburn.class);
	}
}
