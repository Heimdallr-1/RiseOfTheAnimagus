package com.windanesz.morphspellpack.spell;

import com.windanesz.morphspellpack.MorphSpellPack;
import com.windanesz.morphspellpack.block.TileEntityLichPhialHolder;
import com.windanesz.morphspellpack.handler.LichHandler;
import com.windanesz.morphspellpack.registry.MSItems;
import electroblob.wizardry.entity.living.ISummonedCreature;
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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LichDrain extends SpellRay implements ILichSpell {
	public static final String HEAL_FACTOR = "heal_factor";

	public LichDrain() {
		super(MorphSpellPack.MODID, "lichdrain", SpellActions.POINT, true);
		this.particleVelocity(-0.5);
		this.particleSpacing(0.4);
		this.addProperties(DAMAGE, HEAL_FACTOR);
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
		if (EntityUtils.isLiving(target) && ticksInUse % 12 == 0) {
			float damage = this.getProperty("damage").floatValue() * modifiers.get("potency");
			EntityUtils.attackEntityWithoutKnockback(target, MagicDamage.causeDirectMagicDamage(caster, MagicDamage.DamageType.MAGIC), damage);
			if (caster != null && !(target instanceof ISummonedCreature)) {
				caster.heal(damage * this.getProperty("heal_factor").floatValue());
				((EntityPlayer) caster).getFoodStats().addStats(1, 0.1f);
			}
		}

		return true;
	}

	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers) {
		if (caster instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) caster;
			if (ItemArtefact.isArtefactActive(player, MSItems.head_soulbane)) {
				Location receptacleLocation = LichHandler.getReceptacleLocation(player);
				if (receptacleLocation != null && receptacleLocation.pos.equals(pos)) {
					TileEntity tile = world.getTileEntity(pos);
					if (tile instanceof TileEntityLichPhialHolder) {
						TileEntityLichPhialHolder phialHolder = (TileEntityLichPhialHolder) tile;
						if (!world.isRemote && phialHolder.getPhylactery() != null && ticksInUse % 5 == 0) {
							phialHolder.consumePercent(0.01f);
							int currentDuration = 0;
							if (player.isPotionActive(WizardryPotions.empowerment)) {
								currentDuration = player.getActivePotionEffect(WizardryPotions.empowerment).getDuration();
							}
							player.addPotionEffect(new PotionEffect(WizardryPotions.empowerment, currentDuration + 100, 1));
						}
						return true;
					}
				}
			}
		}
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
