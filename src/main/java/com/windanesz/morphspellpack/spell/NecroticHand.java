package com.windanesz.morphspellpack.spell;

import com.windanesz.morphspellpack.MorphSpellPack;
import com.windanesz.morphspellpack.handler.LichHandler;
import com.windanesz.morphspellpack.registry.MSItems;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.SpellBuff;
import electroblob.wizardry.spell.SpellRay;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class NecroticHand extends SpellRay implements ILichSpell {

	public static final String DAMAGE_INCREASE_PER_LAYER = "damage_increase_per_layer";

	public NecroticHand() {
		super(MorphSpellPack.MODID, "necrotic_hand", SpellActions.POINT, false);
		this.soundValues(1, 1, 0.4f);
		addProperties(DAMAGE, EFFECT_DURATION, EFFECT_STRENGTH, DAMAGE_INCREASE_PER_LAYER);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers) {
		// Fire can damage armour stands, so this includes them
		if (target instanceof EntityLivingBase) {
			Optional<Element> effect = Optional.empty();
			float damage = getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY);
			if (caster instanceof EntityPlayer) {
				int count = 1;
				EntityPlayer player = (EntityPlayer) caster;
				if (LichHandler.isLich(player)) {
					for (Optional<Element> optionalEntity : LichHandler.getCrystalPyramid(player)) {
						if (optionalEntity.isPresent()) {
							count++;
							if (count == 5) {
								effect = optionalEntity;
							}
						}
					}
				}
				damage *= 1 + getProperty(DAMAGE_INCREASE_PER_LAYER).floatValue() * count;
			}
			if (!MagicDamage.isEntityImmune(DamageType.WITHER, target)) {
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.WITHER), damage);

				if (effect.isPresent()) {
					switch (effect.get()) {
						case FIRE:
							target.setFire((int) (getProperty(EFFECT_DURATION).floatValue() / 20 * modifiers.get(WizardryItems.duration_upgrade)));
							break;
						case ICE:
							((EntityLivingBase) target).addPotionEffect(new PotionEffect(WizardryPotions.frost,
									(int) (getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
									getProperty(EFFECT_STRENGTH).intValue() + SpellBuff.getStandardBonusAmplifier(modifiers.get(SpellModifiers.POTENCY))));
							break;
						case LIGHTNING:
							((EntityLivingBase) target).addPotionEffect(new PotionEffect(WizardryPotions.paralysis,
									(int) (getProperty(EFFECT_DURATION).floatValue() * 0.5 * modifiers.get(WizardryItems.duration_upgrade)),
									getProperty(EFFECT_STRENGTH).intValue() + SpellBuff.getStandardBonusAmplifier(modifiers.get(SpellModifiers.POTENCY))));
							break;
						case EARTH:
							((EntityLivingBase) target).addPotionEffect(new PotionEffect(MobEffects.POISON,
									(int) (getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
									getProperty(EFFECT_STRENGTH).intValue() + SpellBuff.getStandardBonusAmplifier(modifiers.get(SpellModifiers.POTENCY))));
						case NECROMANCY:
							((EntityLivingBase) target).addPotionEffect(new PotionEffect(MobEffects.WITHER,
									(int) (getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
									getProperty(EFFECT_STRENGTH).intValue() + SpellBuff.getStandardBonusAmplifier(modifiers.get(SpellModifiers.POTENCY))));
							break;
						case HEALING:
							((EntityLivingBase) target).addPotionEffect(new PotionEffect(MobEffects.BLINDNESS,
									(int) (getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
									getProperty(EFFECT_STRENGTH).intValue() + SpellBuff.getStandardBonusAmplifier(modifiers.get(SpellModifiers.POTENCY))));
							break;
						case SORCERY:
							((EntityLivingBase) target).addPotionEffect(new PotionEffect(WizardryPotions.arcane_jammer,
									(int) (getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
									getProperty(EFFECT_STRENGTH).intValue() + SpellBuff.getStandardBonusAmplifier(modifiers.get(SpellModifiers.POTENCY))));
							break;
					}
				}
			}
		}
		return true;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers) {
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers) {
		return false;
	}

	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
		ParticleBuilder.create(ParticleBuilder.Type.DARK_MAGIC).pos(x, y, z).clr(0.2F, 0.0F, 0.3F).spawn(world);
		ParticleBuilder.create(ParticleBuilder.Type.DARK_MAGIC).pos(x, y, z).clr(0.1F, 0.0F, 0.0F).spawn(world);
		ParticleBuilder.create(ParticleBuilder.Type.SPARKLE).pos(x, y, z).time(12 + world.rand.nextInt(8)).clr(0.4F, 0.0F, 0.0F).spawn(world);
	}

	@Override
	public boolean applicableForItem(Item item) {
		return item == MSItems.lich_spell_book || item == WizardryItems.scroll;
	}
}
