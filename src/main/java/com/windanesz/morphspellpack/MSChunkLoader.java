package com.windanesz.morphspellpack;

import com.windanesz.morphspellpack.block.IChunkLoaderTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

	public final class MSChunkLoader implements LoadingCallback {

	public static final MSChunkLoader INSTANCE = new MSChunkLoader();
	private static final String TILE_POSITION_TAG = "tilePosition";

	private MSChunkLoader() {

	}

	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world) {
		for (Ticket tk : tickets) {
			if (!tk.isPlayerTicket() && tk.getModId().startsWith(MorphSpellPack.MODID) && tk.getModData().hasKey(TILE_POSITION_TAG)) {
				getTile(world, BlockPos.fromLong(tk.getModData().getLong(TILE_POSITION_TAG)), IChunkLoaderTile.class).ifPresent(t -> t.setTicket(tk));
			}
		}
	}

	public void writeDataToTicket(Ticket tk, BlockPos pos) {
		tk.getModData().setLong(TILE_POSITION_TAG, pos.toLong());
	}

	public static <T> Optional<T> getTile(@Nullable IBlockAccess world, @Nullable BlockPos pos, Class<T> teClass) {
		if (world == null || pos == null) {
			return Optional.empty();
		}

		TileEntity te = world.getTileEntity(pos);

		if (teClass.isInstance(te)) {
			return Optional.of(teClass.cast(te));
		}

		return Optional.empty();
	}
}