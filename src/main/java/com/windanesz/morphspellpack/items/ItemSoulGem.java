package com.windanesz.morphspellpack.items;

import com.windanesz.morphspellpack.Settings;
import com.windanesz.morphspellpack.handler.LichHandler;
import com.windanesz.morphspellpack.spell.SpellTransformation;
import com.windanesz.wizardryutils.integration.baubles.BaublesIntegration;
import com.windanesz.wizardryutils.item.ITickableArtefact;
import com.windanesz.wizardryutils.tools.WizardryUtilsTools;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.IWorkbenchItem;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.util.BlockUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static com.windanesz.morphspellpack.items.ItemSoulPhylactery.ENTITY_TAG;
import static com.windanesz.morphspellpack.items.ItemSoulPhylactery.getPercentFilled;

public class ItemSoulGem extends ItemArtefact implements IWorkbenchItem {

	public ItemSoulGem(EnumRarity rarity, Type type) {
		super(rarity, type);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced) {
		if (world != null && stack.hasTagCompound()) {
			int fill = (int) (getPercentFilled(stack) * 100);
			Wizardry.proxy.addMultiLineDescription(tooltip, "item.morphspellpack:charm_soul_gem.status", fill);
		} else {
			Wizardry.proxy.addMultiLineDescription(tooltip, "item.morphspellpack:charm_soul_gem.desc_initial");
		}
		super.addInformation(stack, world, tooltip, advanced);
	}

	@Override
	public int getSpellSlotCount(ItemStack stack) {return 0;}

	@Override
	public boolean onApplyButtonPressed(EntityPlayer player, Slot centre, Slot crystals, Slot upgrade, Slot[] spellBooks) {
		if (centre.getStack().hasTagCompound()) {
			NBTTagCompound nbt = centre.getStack().getTagCompound();
			return true;
		}

		return false;
	}

	@Override
	public boolean showTooltip(ItemStack stack) {
		return false;
	}

	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.DRINK;
	}

	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack itemstack = playerIn.getHeldItem(handIn);

		if (getPercentFilled(itemstack) >= Settings.generalSettings.soul_phylactery_cost_of_use) {
			playerIn.setActiveHand(handIn);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);

		} else {
			return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
		}
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack itemstack, World worldIn, EntityLivingBase entityLiving) {
		if (entityLiving instanceof EntityPlayer && LichHandler.isLich(entityLiving)
				&& getPercentFilled(itemstack) >= Settings.generalSettings.soul_phylactery_cost_of_use) {
			ItemSoulPhylactery.consumePercent(itemstack, Settings.generalSettings.soul_phylactery_cost_of_use * 0.5f);
			((EntityPlayer) entityLiving).getFoodStats().addStats(2, 0.1f);
			return itemstack;
		}

		return itemstack;
	}

	public int getMaxItemUseDuration(ItemStack stack) {
		return 32;
	}

}
