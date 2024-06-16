package com.windanesz.morphspellpack.spell;

import com.windanesz.morphspellpack.MorphSpellPack;
import com.windanesz.morphspellpack.items.ItemSoulPhylactery;
import com.windanesz.morphspellpack.registry.MSItems;
import com.windanesz.wizardryutils.integration.baubles.BaublesIntegration;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.List;
import java.util.ResourceBundle;

public class SoulCompass extends Spell implements ILichSpell {

	public SoulCompass() {
		super(MorphSpellPack.MODID, "soul_compass", SpellActions.POINT_UP, false);
		addProperties(EFFECT_RADIUS, EFFECT_DURATION);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		double radius = getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.range_upgrade);

		ItemStack phylactery = findPhylactery(caster);

		if (phylactery != ItemStack.EMPTY && ItemSoulPhylactery.getPercentFilled(phylactery) == 1f) {
			String entityName = ItemSoulPhylactery.getEntity(phylactery);

			EntityLivingBase closestEntity = null;
			double closestDistance = Double.MAX_VALUE;

			for (EntityLivingBase entity : EntityUtils.getEntitiesWithinRadius(radius, caster.posX, caster.posY, caster.posZ, world, EntityLivingBase.class)) {
				ResourceLocation key = EntityList.getKey(entity);
				if (key != null && entityName.equals(key.toString())) {
					double distance = caster.getDistance(entity);
					if (distance < closestDistance) {
						closestEntity = entity;
						closestDistance = distance;
					}
				}
			}

			if (closestEntity != null) {
				closestEntity.addPotionEffect(new PotionEffect(MobEffects.GLOWING, (int) (getProperty(EFFECT_DURATION).intValue() * modifiers.get(WizardryItems.duration_upgrade)), 0));
				return true;
			}
		}

		// if no entity found or phylactery is not full, send msg
		if (!world.isRemote) {
			caster.sendStatusMessage(new TextComponentTranslation("spell.morphspellpack:soul_compass.no_nearby_mobs"), false);
		}
		return true;
	}

	private ItemStack findPhylactery(EntityPlayer player) {
		List<ItemStack> artefactStacks = BaublesIntegration.getEquippedArtefactStacks(player, ItemArtefact.Type.CHARM);
		if (!artefactStacks.isEmpty() && artefactStacks.get(0).getItem() instanceof ItemSoulPhylactery) {
			return artefactStacks.get(0);
		}

		for (ItemStack item : player.inventory.mainInventory) {
			if (item.getItem() instanceof ItemSoulPhylactery) {
				return item;
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean applicableForItem(Item item) {
		return item == MSItems.lich_spell_book || item == WizardryItems.scroll;
	}
}
