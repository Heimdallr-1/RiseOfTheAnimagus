package com.windanesz.morphspellpack.spell;

import com.windanesz.morphspellpack.MorphSpellPack;
import com.windanesz.morphspellpack.handler.LichHandler;
import com.windanesz.morphspellpack.registry.MSItems;
import com.windanesz.morphspellpack.registry.MSPotions;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.SpellBuff;
import electroblob.wizardry.util.SpellModifiers;
import me.ichun.mods.morph.common.handler.PlayerMorphHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class UmbralVeil extends SpellBuff implements ILichSpell {

	public UmbralVeil() {
		super(MorphSpellPack.MODID, "umbral_veil", 0,0,0, () -> MSPotions.umbral_veil);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		if (ItemArtefact.isArtefactActive(caster, MSItems.ring_shadows)) {
			modifiers.set(WizardryItems.duration_upgrade, modifiers.get(WizardryItems.duration_upgrade) * 2, false);
		}
		return super.cast(world, caster, hand, ticksInUse, modifiers);
	}

	@Override
	public boolean applicableForItem(Item item) {
		return item == MSItems.lich_spell_book || item == WizardryItems.scroll;
	}
}
