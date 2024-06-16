package com.windanesz.morphspellpack.command;

import com.windanesz.morphspellpack.MorphSpellPack;
import com.windanesz.morphspellpack.handler.LichHandler;
import com.windanesz.morphspellpack.items.ItemSoulPhylactery;
import com.windanesz.morphspellpack.registry.MSItems;
import com.windanesz.morphspellpack.spell.SpellTransformation;
import electroblob.wizardry.data.WizardData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;

import static com.windanesz.morphspellpack.spell.SpellTransformation.MORPH_DURATION;

public class CommandSetLich extends CommandBase {

	public static final String COMMAND = "setlich";

	private static final List<String> vals = Arrays.asList("true", "false", "givephylactery");

	public String getName() {
		return COMMAND;
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender p_71519_1_) {
		return true;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return getUnlocalizedName() + ".usage";
	}

	public static String getUnlocalizedName() {
		return "commands." + MorphSpellPack.MODID + ":" + COMMAND;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] arguments, BlockPos pos) {

		switch (arguments.length) {
			case 1:
				return getListOfStringsMatchingLastWord(arguments, server.getOnlinePlayerNames());
			case 2:
				return getListOfStringsMatchingLastWord(arguments, vals);
		}
		return super.getTabCompletions(server, sender, arguments, pos);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] arguments) throws CommandException {

		if (arguments.length != getRequiredArgsCount()) {
			throw new WrongUsageException(getUsage(sender));
		}

		if (arguments[1].equals(vals.get(2))) {
			// give an itemstack of ItemSoulPhylactery with full precent filled and belonging to the player
			EntityPlayer targetPlayer = getPlayer(server, sender, arguments[0]);
			ItemStack phylactery = new ItemStack(MSItems.charm_soul_phylactery); // Replace with the actual item
			ItemSoulPhylactery.setEntity(phylactery, targetPlayer.getName());
			ItemSoulPhylactery.addPercent(phylactery, 1.0f);
			targetPlayer.inventory.addItemStackToInventory(phylactery);
			return;
		}

		boolean lich = Boolean.parseBoolean(arguments[1]);
		EntityPlayer targetPlayer = getPlayer(server, sender, arguments[0]);
		if (targetPlayer != null) {
			LichHandler.setLich(targetPlayer, lich);
			if (lich) {
				SpellTransformation.morphPlayer(targetPlayer, LichHandler.getLichString(), -1);
			} else {
				SpellTransformation.demorphPlayer(targetPlayer);

			}
		}
	}

	public static int getRequiredArgsCount() {return 2;}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return index == 0;
	}
}

