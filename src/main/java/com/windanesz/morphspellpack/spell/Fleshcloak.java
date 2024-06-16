package com.windanesz.morphspellpack.spell;

import com.windanesz.morphspellpack.handler.LichHandler;
import com.windanesz.morphspellpack.registry.MSItems;
import com.windanesz.morphspellpack.registry.MSPotions;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import me.ichun.mods.morph.common.handler.PlayerMorphHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class Fleshcloak extends SpellTransformation implements ILichSpell {

	public Fleshcloak() {
		super("fleshcloak", SpellActions.SUMMON, false, "");
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		if (!world.isRemote && LichHandler.isLich(caster)) {

			if (caster.isSneaking() && caster.isPotionActive(MSPotions.fleshcloak)) {
				caster.removePotionEffect(MSPotions.fleshcloak);
				SpellTransformation.morphPlayer((EntityPlayer)  caster, LichHandler.getLichString(), -1);

				return true;
			}

			int duration = (int) (getProperty(DURATION).intValue() * modifiers.get(WizardryItems.duration_upgrade) * (ItemArtefact.isArtefactActive(caster, MSItems.ring_transformation) ? 2 : 1));

			if (ItemArtefact.isArtefactActive(caster, MSItems.amulet_fleshcloak)) {
				duration = Integer.MAX_VALUE;
			}

			caster.addPotionEffect(new PotionEffect(MSPotions.fleshcloak, duration));
			if (PlayerMorphHandler.getInstance().forceDemorph((EntityPlayerMP) caster)) {
				WizardData data = WizardData.get((EntityPlayer) caster);
				if (data != null) {
					data.setVariable(MORPH_DURATION, 0);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean applicableForItem(Item item) {
		return item == MSItems.lich_spell_book || item == WizardryItems.scroll;
	}
}
