package com.windanesz.morphspellpack.block;

import com.windanesz.morphspellpack.MSChunkLoader;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockFullLichPhialHolder extends BlockLichPhialHolder implements ITileEntityProvider {

	public BlockFullLichPhialHolder() {
		super();
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityLichPhialHolder();
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, placer, stack);
		if (!world.isRemote) {
			MSChunkLoader.getTile(world, pos, TileEntityLichPhialHolder.class).ifPresent(TileEntityLichPhialHolder::setupInitialTicket);
		}
	}
}
