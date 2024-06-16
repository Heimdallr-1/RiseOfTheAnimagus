package com.windanesz.morphspellpack.potion;

import com.windanesz.morphspellpack.Settings;
import com.windanesz.morphspellpack.spell.SpellTransformation;
import com.windanesz.wizardryutils.registry.EntityRegistry;
import electroblob.wizardry.potion.Curse;
import electroblob.wizardry.potion.PotionMagicEffect;
import me.ichun.mods.morph.common.handler.PlayerMorphHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Objects;

public class PotionLycanthropy extends Curse {

	public PotionLycanthropy() {
		super(false, 0x000000, new ResourceLocation("morphspellpack", "textures/gui/potion_icons/curse_of_lycanthropy.png"));
		this.setPotionName("potion.morphspellpack:lycanthropy.name");
	}

	@Override
	public void performEffect(EntityLivingBase entityLivingBase, int amplifier) {
		if (entityLivingBase.ticksExisted % 60 == 0 && entityLivingBase instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) entityLivingBase;
			if (!player.world.isDaytime()) {
				if (!PlayerMorphHandler.getInstance().hasMorph(entityLivingBase.getName(), Side.SERVER)) {
				// It's night, transform the player into a werewolf
				int duration = 1200 * (amplifier + 1); // 1 minute per level
					SpellTransformation.morphPlayer(player, Settings.generalSettings.lycanthropy_werewolf_entity, duration);
				}
			} else if (PlayerMorphHandler.getInstance().hasMorph(entityLivingBase.getName(), Side.SERVER)) {
				Entity morphEntity = PlayerMorphHandler.getInstance().getMorphEntity(entityLivingBase.world, entityLivingBase.getName(), Side.SERVER);
				if (morphEntity != null) {
					ResourceLocation morphEntityKey = EntityList.getKey(morphEntity);
					if (morphEntityKey != null && Objects.equals(morphEntityKey.toString(), Settings.generalSettings.lycanthropy_werewolf_entity)) {
						// It's day, revert the player back to human form
						SpellTransformation.demorphPlayer(player);
					}
				}
			}
		}
	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		return duration >= 1;
	}
}