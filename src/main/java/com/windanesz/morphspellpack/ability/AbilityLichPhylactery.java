package com.windanesz.morphspellpack.ability;

import com.windanesz.morphspellpack.Settings;
import com.windanesz.morphspellpack.block.TileEntityLichPhialHolder;
import com.windanesz.morphspellpack.handler.LichHandler;
import com.windanesz.morphspellpack.items.ItemSoulPhylactery;
import com.windanesz.morphspellpack.registry.MSItems;
import com.windanesz.wizardryutils.integration.baubles.BaublesIntegration;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import me.ichun.mods.morph.api.ability.Ability;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

import java.util.UUID;

public class AbilityLichPhylactery extends Ability {

	public static final String name = "lichNeedsPhylactery";

	@SuppressWarnings("unused")
	public AbilityLichPhylactery() {
	}

	@Override
	public String getType() {
		return name;
	}

	@Override
	public void tick() {
		boolean hasPhylactery = hasPhylactery((EntityPlayer) getParent());
		if (!getParent().world.isRemote && (Settings.generalSettings.soul_phylactery_requirement_damage || Settings.generalSettings.soul_phylactery_requirement_curse)
				&& getParent() instanceof EntityPlayer && getParent().ticksExisted % 40 == 0 && !((EntityPlayer) getParent()).isCreative()) {
			EntityPlayer lich = (EntityPlayer) getParent();
			if (Settings.generalSettings.soul_phylactery_requirement_damage && !hasPhylactery && !getParent().world.isRemote) {
				EntityUtils.attackEntityWithoutKnockback(lich, MagicDamage.causeDirectMagicDamage(lich,
						MagicDamage.DamageType.WITHER), 0.5f);
			}

			if (Settings.generalSettings.soul_phylactery_requirement_curse) {
				if (hasPhylactery) {
					// lich wears phylactery
					if (ItemArtefact.isArtefactActive(lich, MSItems.charm_soul_phylactery)) {
						ItemStack phylactery = BaublesIntegration.getEquippedArtefactStacks(lich, ItemArtefact.Type.CHARM).get(0);
						if (ItemSoulPhylactery.getEntity(phylactery).equals(lich.getName())) {
							TileEntityLichPhialHolder.applyHPModifier(lich, ItemSoulPhylactery.getPercentFilled(phylactery));
						}
					}
					if (lich.isPotionActive(WizardryPotions.curse_of_enfeeblement) && lich.getActivePotionEffect(WizardryPotions.curse_of_enfeeblement).getDuration() < 2000) {
						lich.removePotionEffect(WizardryPotions.curse_of_enfeeblement);
					}
				}
				if (!hasPhylactery && lich.ticksExisted > 200) {
					// add temporary curse
					lich.addPotionEffect(new PotionEffect(WizardryPotions.curse_of_enfeeblement, 1000, 3));
					if (lich.ticksExisted < 300) {
						lich.setHealth(lich.getMaxHealth() * 0.4f);
					}
					IAttributeInstance maxHealthAttribute = lich.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
					AttributeModifier modifier = new AttributeModifier(UUID.fromString("3c7db14f-4c7e-4e3c-8970-5fdaf7f6a9b3"), "Phylactery max health limiter", -1 + 0.1f, 1);
					if (maxHealthAttribute.getModifier(modifier.getID()) != null) {
						maxHealthAttribute.removeModifier(modifier);
					}
				}
			}
		}
	}

	public boolean hasPhylactery(EntityPlayer player) {
		if (ItemArtefact.isArtefactActive(player, MSItems.charm_soul_phylactery)) {
			ItemStack phylactery = BaublesIntegration.getEquippedArtefactStacks(player, ItemArtefact.Type.CHARM).get(0);
			if (ItemSoulPhylactery.getEntity(phylactery).equals(player.getName())) {
				return true;
			}
		}

		if (player.ticksExisted % 181 == 0 && !player.world.isRemote) {
			LichHandler.checkReceptacleIntegrity(player);
		}
		return LichHandler.getReceptacleLocation(player) != null;
	}
}
