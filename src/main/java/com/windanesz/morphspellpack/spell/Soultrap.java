package com.windanesz.morphspellpack.spell;

import com.windanesz.morphspellpack.MorphSpellPack;
import com.windanesz.morphspellpack.block.TileEntityLichPhialHolder;
import com.windanesz.morphspellpack.handler.LichHandler;
import com.windanesz.morphspellpack.registry.MSItems;
import com.windanesz.morphspellpack.registry.MSPotions;
import com.windanesz.wizardryutils.capability.SummonedCreatureData;
import com.windanesz.wizardryutils.entity.ai.EntityAIMinionOwnerHurtByTarget;
import com.windanesz.wizardryutils.entity.ai.EntityAIMinionOwnerHurtTarget;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.SpellRay;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.Location;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Soultrap extends SpellRay implements ILichSpell {


	public Soultrap() {
		super(MorphSpellPack.MODID, "soultrap", SpellActions.POINT, false);
		this.particleVelocity(-0.5);
		this.particleSpacing(0.4);
		this.addProperties(DAMAGE, DURATION);
		this.soundValues(0.6F, 1.0F, 0.0F);
	}

	protected SoundEvent[] createSounds() {
		return this.createContinuousSpellSounds();
	}

	protected void playSound(World world, EntityLivingBase entity, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds) {
		this.playSoundLoop(world, entity, ticksInUse);
	}

	protected void playSound(World world, double x, double y, double z, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds) {
		this.playSoundLoop(world, x, y, z, ticksInUse, duration);
	}

	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers) {
			if (target instanceof EntityLivingBase && ((EntityLivingBase) target).isPotionActive(MSPotions.conjured_soul)) {
				return false;
			}
			float damage = this.getProperty("damage").floatValue() * modifiers.get("potency");
			EntityUtils.attackEntityWithoutKnockback(target, MagicDamage.causeDirectMagicDamage(caster, MagicDamage.DamageType.WITHER), damage);
			if (target != null && target instanceof EntityLivingBase && ((EntityLivingBase) target).getHealth() == 0) {
				if (target instanceof EntityPlayer) return true;
				if (!world.isRemote) {

					// Try and find a nearby floor space
					BlockPos pos = target.getPosition();

					// If there was no floor around and the entity isn't a flying one, the spell fails.
					// As per the javadoc for findNearbyFloorSpace, there's no point trying the rest of the minions.
					if (pos == null) {return false;}

					Entity minion = EntityList.newEntity(target.getClass(), caster.world);
					minion.setPositionAndRotation(target.posX, target.posY, target.posZ, target.rotationYaw, target.rotationPitch);
					if (minion == null) {return false;}

					minion.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					if (minion instanceof EntityLiving) {
						((EntityLiving) minion).onInitialSpawn(world.getDifficultyForLocation(new BlockPos(minion)), (IEntityLivingData) null);
					}

					if (minion instanceof EntityCreature) {
						// target enemies that hurt the owner
						((EntityLiving) minion).targetTasks.addTask(1, new EntityAIMinionOwnerHurtByTarget((EntityCreature) minion));
						// target enemies targeted by the owner
						((EntityLiving) minion).targetTasks.addTask(2, new EntityAIMinionOwnerHurtTarget((EntityCreature) minion));
					}

					if (minion instanceof EntityLivingBase) {

						SummonedCreatureData data = SummonedCreatureData.get((EntityLivingBase) minion);
						if (data == null) {return false;}
						data.setLifetime((int) (getProperty(DURATION).intValue() * modifiers.get(WizardryItems.duration_upgrade)));
						data.setFollowOwner(true);
						data.setCaster(caster);
						((EntityLivingBase) minion).addPotionEffect(new PotionEffect(MSPotions.conjured_soul, Integer.MAX_VALUE));
						world.spawnEntity(minion);
					}
				}
			}

		return true;
	}

	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers) {
		return false;
	}

	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers) {
		return true;
	}

	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
		if (world.rand.nextInt(5) == 0) {
			ParticleBuilder.create(ParticleBuilder.Type.DARK_MAGIC).pos(x, y, z).clr(2, 199, 140).spawn(world);
		}

		ParticleBuilder.create(ParticleBuilder.Type.SPARKLE).pos(x, y, z).vel(vx, vy, vz).time(8 + world.rand.nextInt(6)).clr(2, 199, 140).spawn(world);
	}

	@Override
	public boolean applicableForItem(Item item) {
		return item == MSItems.lich_spell_book || item == WizardryItems.scroll;
	}
}
