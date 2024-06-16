package com.windanesz.morphspellpack;

import electroblob.wizardry.entity.living.EntityShadowWraith;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public final class Utils {

	private Utils() {} // no instances

	/**
	 * Shorthand method to do instance check and sideonly checks for player messages
	 */
	public static void sendMessage(Entity player, String translationKey, boolean actionBar, Object... args) {
		if (player instanceof EntityPlayer && !player.world.isRemote) {
			((EntityPlayer) player).sendStatusMessage(new TextComponentTranslation(translationKey, args), actionBar);
		}
	}


}
