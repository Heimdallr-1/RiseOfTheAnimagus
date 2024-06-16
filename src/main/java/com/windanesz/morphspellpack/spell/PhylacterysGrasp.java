package com.windanesz.morphspellpack.spell;

import com.windanesz.morphspellpack.MorphSpellPack;
import com.windanesz.morphspellpack.registry.MSItems;
import com.windanesz.morphspellpack.registry.MSPotions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.SpellBuff;
import net.minecraft.item.Item;

public class PhylacterysGrasp extends SpellBuff implements ILichSpell {

	public PhylacterysGrasp() {
		super(MorphSpellPack.MODID, "phylacterys_grasp", 2, 199, 140, () -> MSPotions.phylacterys_grasp);
	}

	@Override
	public boolean applicableForItem(Item item) {
		return item == MSItems.lich_spell_book || item == WizardryItems.scroll;
	}
}
