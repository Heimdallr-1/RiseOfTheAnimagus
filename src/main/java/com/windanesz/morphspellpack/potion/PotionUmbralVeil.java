package com.windanesz.morphspellpack.potion;

import com.windanesz.morphspellpack.MorphSpellPack;
import com.windanesz.morphspellpack.registry.MSItems;
import electroblob.wizardry.entity.living.EntityShadowWraith;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.potion.PotionMagicEffectParticles;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class PotionUmbralVeil extends PotionMagicEffectParticles {

	public PotionUmbralVeil() {
		super(false, 0, new ResourceLocation(MorphSpellPack.MODID, "textures/gui/potion_icons/umbral_veil.png"));
		setBeneficial();
	}

	@Override
	public void spawnCustomParticle(World world, double x, double y, double z) {
		MorphSpellPack.proxy.renderUmbralVeil(world, x, y, z);
	}

	@Override
	public void performEffect(EntityLivingBase entitylivingbase, int strength) {
		if (!entitylivingbase.world.isRemote && entitylivingbase.ticksExisted % 20 == 0 && entitylivingbase instanceof EntityPlayer && ItemArtefact.isArtefactActive((EntityPlayer) entitylivingbase, MSItems.ring_shadows)
		&& !((EntityPlayer) entitylivingbase).getCooldownTracker().hasCooldown(MSItems.ring_shadows)) {
			List<EntityMob> entities = EntityUtils.getEntitiesWithinRadius(8, entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ, entitylivingbase.world, EntityMob.class);
			for (EntityLivingBase entity : entities) {
				if (AllyDesignationSystem.isAllied(entitylivingbase, entity)) continue;
				shootOrb(entitylivingbase.world, entitylivingbase, entity, Spells.darkness_orb, new SpellModifiers());
				((EntityPlayer) entitylivingbase).getCooldownTracker().setCooldown(MSItems.ring_shadows, 200);
				break;
			}
		}
	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		return true;
	}

	public static boolean shootOrb(World world, EntityLivingBase caster, @Nullable Entity t, Spell spell, SpellModifiers modifiers) {
		boolean result = false;
		// very ugly hack but it works, since the cast method with EntityPlayer doesn't have a target argument
		if (t instanceof EntityLivingBase) {
			EntityLivingBase target = (EntityLivingBase) t;
			EntityShadowWraith mage = new EntityShadowWraith(world);
			mage.setCaster(caster);
			Vec3d playerPos = caster.getPositionVector();
			// Get target entity's position
			Vec3d offset = target.getPositionVector().subtract(caster.getPositionVector()).normalize().scale(0.5);
			Vec3d newPosition = playerPos.add(offset);
			mage.setPosition(newPosition.x, newPosition.y, newPosition.z);
			result = spell.cast(caster.world, mage, EnumHand.MAIN_HAND, 0, target, modifiers);
			world.removeEntity(mage);
			target.setRevengeTarget(caster);
		}
		return result;
	}
}