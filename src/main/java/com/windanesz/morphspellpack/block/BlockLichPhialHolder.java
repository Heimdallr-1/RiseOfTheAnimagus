package com.windanesz.morphspellpack.block;

import com.windanesz.morphspellpack.handler.LichHandler;
import com.windanesz.morphspellpack.items.ItemSoulPhylactery;
import com.windanesz.morphspellpack.registry.MSBlocks;
import com.windanesz.morphspellpack.registry.MSItems;
import com.windanesz.morphspellpack.spell.ILichSpell;
import com.windanesz.wizardryutils.tools.WizardryUtilsTools;
import electroblob.wizardry.block.BlockReceptacle;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ItemArcaneTome;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.tileentity.TileEntityReceptacle;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.Location;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.SpellProperties;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class BlockLichPhialHolder extends Block {
	private AxisAlignedBB AABB = new AxisAlignedBB(0.1825, 0.0D, 0.125D, 1.0D, 1.0D, 0.875D);

	public BlockLichPhialHolder() {
		super(Material.ROCK);
		setHardness(1.5F);
		setResistance(10.0F);
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return new AxisAlignedBB(0.1825, 0.0D, 0.1825D, 0.8125D, 0.9D, 0.8125D);
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	private boolean isHoldingCorrectWand(EntityPlayer player) {
		for (EnumHand hand : EnumHand.values()) {
			ItemStack heldItem = player.getHeldItem(hand);
			if (heldItem.getItem() instanceof ItemWand) {
				ItemWand wand = (ItemWand) heldItem.getItem();
				if (wand.element == Element.SORCERY && wand.getCurrentSpell(heldItem) == Spells.transportation) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState block, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {

		if (LichHandler.isLich(player) && player.getHeldItemMainhand().getItem() instanceof ItemArcaneTome) {
			boolean hasArtefact = ItemArtefact.isArtefactActive(player, MSItems.amulet_spellforge);
			int filledReceptacles = 0;
			Map<Element, Integer> elementCounts = new HashMap<>();
			for (BlockPos blockPos : BlockUtils.getBlockSphere(pos, 4)) {
				TileEntity tileEntity = world.getTileEntity(blockPos);
				if (!hasArtefact && tileEntity instanceof TileEntityReceptacle && ((TileEntityReceptacle) tileEntity).getElement() == Element.NECROMANCY) {
					filledReceptacles++;
				} else {
					if (hasArtefact && tileEntity instanceof TileEntityReceptacle) {
						elementCounts.put(((TileEntityReceptacle) tileEntity).getElement(), elementCounts.getOrDefault(((TileEntityReceptacle) tileEntity).getElement(), 0) + 1);
					}
				}
			}
			boolean hasFourMatchingElements = elementCounts.values().stream().anyMatch(count -> count >= 4);

			if (filledReceptacles >= 4) {
				// only get spells that are instanceof ILichSpell and pick a random one that matches the Tier ordinal with the ItemArcaneTome's metadata int
				List<Spell> spells = Spell.getAllSpells().stream().filter(spell -> spell instanceof ILichSpell).filter(spell -> spell.getTier().ordinal() == player.getHeldItemMainhand().getMetadata()).collect(Collectors.toList());
				List<Spell> unknownSpells = spells.stream().filter(spell -> !WizardData.get(player).hasSpellBeenDiscovered(spell)).collect(Collectors.toList());
				if (!spells.isEmpty()) {
					Spell spell = !unknownSpells.isEmpty() ? unknownSpells.get(world.rand.nextInt(unknownSpells.size())) : spells.get(world.rand.nextInt(spells.size()));
					ItemStack book = new ItemStack(MSItems.lich_spell_book);
					book.setItemDamage(spell.metadata());
					// also discover the spell with WizardData
					WizardData.get(player).discoverSpell(spell);
					player.getHeldItemMainhand().shrink(1);
					WizardryUtilsTools.giveStackToPlayer(player, book);
					WizardryUtilsTools.sendMessage(player, "The book has been corrupted", false);
					for (BlockPos blockPos : BlockUtils.getBlockSphere(pos, 4)) {
						TileEntity tileEntity = world.getTileEntity(blockPos);
						if (tileEntity instanceof TileEntityReceptacle && ((TileEntityReceptacle) tileEntity).getElement() == Element.NECROMANCY) {
							((TileEntityReceptacle) tileEntity).setElement(null);
						}
					}
				}
			} else if (hasFourMatchingElements) {
				Element element = elementCounts.keySet().iterator().next();

				// only get spells that are instanceof ILichSpell and pick a random one that matches the Tier ordinal with the ItemArcaneTome's metadata int
				List<Spell> spells = Spell.getAllSpells().stream().filter(spell -> spell.getElement() == element).filter(spell -> spell.getTier().ordinal() == player.getHeldItemMainhand().getMetadata()).collect(Collectors.toList());
				List<Spell> unknownSpells = spells.stream().filter(spell -> !WizardData.get(player).hasSpellBeenDiscovered(spell)).collect(Collectors.toList());
				if (!spells.isEmpty()) {
					Spell spell = !unknownSpells.isEmpty() ? unknownSpells.get(world.rand.nextInt(unknownSpells.size())) : spells.get(world.rand.nextInt(spells.size()));
					Item bookItem = Items.AIR;
					List<Item> bookTypeList = ForgeRegistries.ITEMS.getValuesCollection().stream().filter(i -> i instanceof ItemSpellBook).collect(Collectors.toList());
					for (Item currentBook : bookTypeList) {
						if (spell.applicableForItem(currentBook)) {
							bookItem = currentBook;
							break;
						}
					}
					ItemStack book = new ItemStack(bookItem);
					book.setItemDamage(spell.metadata());
					// also discover the spell with WizardData
					WizardData.get(player).discoverSpell(spell);
					player.getHeldItemMainhand().shrink(1);
					WizardryUtilsTools.giveStackToPlayer(player, book);
					WizardryUtilsTools.sendMessage(player, "The book has been corrupted", false);
					for (BlockPos blockPos : BlockUtils.getBlockSphere(pos, 4)) {
						TileEntity tileEntity = world.getTileEntity(blockPos);
						if (tileEntity instanceof TileEntityReceptacle) {
							((TileEntityReceptacle) tileEntity).setElement(null);
						}
					}
				}
			} else {
				WizardryUtilsTools.sendMessage(player, "You must surround the Soul Receptacle with 4 Spectral Dust Receptacles filled with Necromancy Spectral Dust to corrupt the book.", false);

			}
		}

		if (!world.isRemote && LichHandler.isLich(player) && isHoldingCorrectWand(player)
				&& LichHandler.getCrystalPyramid(player).get(4).filter(el -> el == Element.SORCERY).isPresent()) {
			Location location = LichHandler.getLastDeathLocation(player);
			if (location != null) {
				if (location.dimension != player.dimension) {
					LichHandler.teleportEntity(location.dimension, location.pos.getX(), location.pos.getY(), location.pos.getZ(), true, player);
					return true;
				} else {
					player.setPositionAndUpdate(location.pos.getX() + 0.5, location.pos.getY() + 1, location.pos.getZ() + 0.5);
					player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 60, 1));
				}
				LichHandler.setLastDeathLocation(player, null);
			}
		}

		if (world.isRemote && player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSoulPhylactery) {
			Vec3d origin = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
			for (int i = 0; (float) i < 40.0F; ++i) {
				double particleX = origin.x - 1.0 + 2.0 * world.rand.nextDouble();
				double particleZ = origin.z - 1.0 + 2.0 * world.rand.nextDouble();
				ParticleBuilder.create(ParticleBuilder.Type.DARK_MAGIC).pos(particleX, origin.y + 0.6, particleZ).vel(particleX - origin.x, 0.0, particleZ - origin.z).clr(2, 199, 140).spawn(world);
				particleX = origin.x + 0.5 - 1.0 + 2.0 * world.rand.nextDouble();
				particleZ = origin.z + 0.5 - 1.0 + 2.0 * world.rand.nextDouble();
				ParticleBuilder.create(ParticleBuilder.Type.SPARKLE).pos(particleX, origin.y + 0.6, particleZ).vel(particleX - origin.x, 0.0, particleZ - origin.z).time(10).clr(2, 199, 140).spawn(world);
				particleX = origin.x + 0.5 - 1.0 + 2.0 * world.rand.nextDouble();
				particleZ = origin.z + 0.5 - 1.0 + 2.0 * world.rand.nextDouble();
				world.spawnParticle(EnumParticleTypes.BLOCK_DUST, particleX, origin.y, particleZ, particleX - origin.x, 0.0, particleZ - origin.z, new int[] {
						Block.getStateId(block)});
			}
			// Spawn a sphere particle effect
			ParticleBuilder.create(ParticleBuilder.Type.SPHERE).pos(origin.add(0.5, 0.8, 0.5)).scale((float) 2 * 0.8F).time(20).fade(1).clr(2, 199, 140).spawn(world);
		}

		if (world.isRemote) {
			return true;
		}

		TileEntity tileEntity = world.getTileEntity(pos);
		ItemStack heldItem = player.getHeldItem(hand);

		// Check if the held item is an ItemSoulPhylactery and belongs to the player
		if (heldItem.getItem() instanceof ItemSoulPhylactery) {
			if (ItemSoulPhylactery.getEntity(heldItem).equals(player.getName())) {
				// If the tile entity is a TileEntityLichPhialHolder, and it's empty, set the phylactery
				if (tileEntity instanceof TileEntityLichPhialHolder) {
					TileEntityLichPhialHolder tileLichPhialHolder = (TileEntityLichPhialHolder) tileEntity;
					if (tileLichPhialHolder.isEmpty()) {
						tileLichPhialHolder.setPhylactery(heldItem);
					}
				} else {
					// If it's not a TileEntityLichPhialHolder, change the block state to one and set the phylactery
					world.setBlockState(pos, MSBlocks.lich_phial_holder.getDefaultState());
					tileEntity = world.getTileEntity(pos); // Get the tile entity again after changing the block state
					if (tileEntity instanceof TileEntityLichPhialHolder) {
						WizardryUtilsTools.sendMessage(player, "Your soul is now bound to this location", false);
						LichHandler.setReceptacleLocation(player, new Location(pos, player.dimension));
						TileEntityLichPhialHolder tileLichPhialHolder = (TileEntityLichPhialHolder) tileEntity;
						tileLichPhialHolder.setPhylactery(heldItem.copy());
						// Set the player's receptacle location
						player.getHeldItemMainhand().shrink(1);
					}
				}
				return true;
			}
		} else if (tileEntity instanceof TileEntityLichPhialHolder && player.getHeldItemMainhand().isEmpty()) {
			// If the player's hand is empty and the block is a TileEntityLichPhialHolder, retrieve the phylactery and give it to the player
			TileEntityLichPhialHolder tileLichPhialHolder = (TileEntityLichPhialHolder) tileEntity;
			if (player.isSneaking() && tileLichPhialHolder.getStackInSlot(0).getItem() instanceof ItemSoulPhylactery) {
				WizardryUtilsTools.giveStackToPlayer(player, tileLichPhialHolder.getStackInSlot(0));
				// Change the block state to empty
				world.setBlockState(pos, MSBlocks.lich_phial_holder_empty.getDefaultState());
				// Reset the receptacle location
				LichHandler.setReceptacleLocation(player, null);
				return true;
			}
		}
		return false;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState block) {

		TileEntity tileentity = world.getTileEntity(pos);

		if (tileentity instanceof TileEntityLichPhialHolder) {

			TileEntityLichPhialHolder phialHolder = (TileEntityLichPhialHolder) tileentity;

			// does not drop the result stack!
			ItemStack itemstack = phialHolder.getPhylactery();
			if (!itemstack.isEmpty()) {
				InventoryHelper.spawnItemStack(world, phialHolder.getPos().getX(), phialHolder.getPos().getY(), phialHolder.getPos().getZ(), itemstack);
			}
		}

		super.breakBlock(world, pos, block);
	}

	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(MSBlocks.lich_phial_holder_empty);
	}
}
