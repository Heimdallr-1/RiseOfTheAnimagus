package com.windanesz.morphspellpack.spell;

import com.windanesz.morphspellpack.MorphSpellPack;
import com.windanesz.morphspellpack.Settings;
import com.windanesz.morphspellpack.Utils;
import com.windanesz.morphspellpack.registry.MSItems;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.IStoredVariable;
import electroblob.wizardry.data.Persistence;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.SpellRay;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class Skinchanger extends SpellRay {

	public static final String MAX_MOB_HP = "max_mob_hp";
	public static final IStoredVariable<String> LAST_SKINCHANGER_CREATURE = IStoredVariable.StoredVariable.ofString("lastSkinChangerCreature", Persistence.ALWAYS);

	public Skinchanger() {
		super(MorphSpellPack.MODID, "skinchanger", SpellActions.POINT, false);
		addProperties(DURATION, MAX_MOB_HP);
		WizardData.registerStoredVariables(LAST_SKINCHANGER_CREATURE);
	}

	@Override
	public boolean canBeCastBy(TileEntityDispenser dispenser) {
		return false;
	}

	@Override
	public boolean canBeCastBy(EntityLiving npc, boolean override) {
		return false;
	}

	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		Vec3d look = caster.getLookVec();
		Vec3d origin = new Vec3d(caster.posX, caster.posY + (double)caster.getEyeHeight() - 0.25, caster.posZ);
		if (!this.isContinuous && world.isRemote && !Wizardry.proxy.isFirstPerson(caster)) {
			origin = origin.add(look.scale(1.2));
		}

		boolean hasRing = ItemArtefact.isArtefactActive(caster, MSItems.ring_skinchanger);

		if (!hasRing || caster.isSneaking()) {
			if (!this.shootSpell(world, origin, look, caster, ticksInUse, modifiers)) {
				return false;
			} else {
				if (this.casterSwingsArm(world, caster, hand, ticksInUse, modifiers)) {
					caster.swingArm(hand);
				}

				this.playSound(world, caster, ticksInUse, -1, modifiers, new String[0]);
				return true;
			}
		}

		WizardData data = WizardData.get(caster);
		String entity = data.getVariable(LAST_SKINCHANGER_CREATURE);
		if (entity == null) {
			return false;
		}
		int duration = (int) (getProperty(DURATION).intValue() * modifiers.get(WizardryItems.duration_upgrade));
		SpellTransformation.morphPlayer(caster, entity, duration);
		return true;
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit,
			@Nullable EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers) {

		if (caster instanceof EntityPlayer && !world.isRemote && target instanceof EntityLivingBase) {
			EntityLivingBase livingTarget = (EntityLivingBase) target;
			int maxAllowedHP = getProperty(MAX_MOB_HP).intValue();
			if (livingTarget.getMaxHealth() >= maxAllowedHP) {
				Utils.sendMessage(caster, "spell.morphspellpack:skinchanger.mob_too_strong", true);
				return false;
			}

			String entity = EntityList.getKey(livingTarget).toString();
			List<String> bannedMobs = Arrays.asList(Settings.generalSettings.skinchanger_banned_mobs);
			if (bannedMobs.contains(entity)) {
				Utils.sendMessage(caster, "spell.morphspellpack:skinchanger.mob_not_allowed", true);
				return false;
			}

			if (Settings.generalSettings.skinchanger_disallow_undeads &&
					(livingTarget.isPotionActive(WizardryPotions.curse_of_undeath) || livingTarget.isEntityUndead())) {
				Utils.sendMessage(caster, "spell.morphspellpack:skinchanger.undead_not_allowed", true);
				return false;
			}

			int duration = (int) (getProperty(DURATION).intValue() * modifiers.get(WizardryItems.duration_upgrade));
			WizardData data = WizardData.get((EntityPlayer) caster);
			data.setVariable(LAST_SKINCHANGER_CREATURE, entity);
			SpellTransformation.morphPlayer(caster, entity, duration);
			return true;
		}

		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit,
			@Nullable EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers) {
		return false;
	}

	@Override
	protected boolean onMiss(World world, @Nullable EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers) {
		return false;
	}
}
