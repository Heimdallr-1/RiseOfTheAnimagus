package com.windanesz.morphspellpack.registry;

import com.windanesz.morphspellpack.MorphSpellPack;
import com.windanesz.morphspellpack.block.BlockFullLichPhialHolder;
import com.windanesz.morphspellpack.block.BlockLichPhialHolder;
import com.windanesz.morphspellpack.block.TileEntityLichPhialHolder;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@GameRegistry.ObjectHolder(MorphSpellPack.MODID)
@Mod.EventBusSubscriber
public final class MSBlocks {

		private MSBlocks() {} // No instances!

		public static final Block lich_phial_holder = placeholder();
		public static final Block lich_phial_holder_empty = placeholder();

		@SubscribeEvent
		public static void register(RegistryEvent.Register<Block> event) {

			IForgeRegistry<Block> registry = event.getRegistry();
			registerBlock(registry, "lich_phial_holder", new BlockFullLichPhialHolder());
			registerBlock(registry, "lich_phial_holder_empty", new BlockLichPhialHolder());
		}

		@Nonnull
		@SuppressWarnings("ConstantConditions")
		public static <T> T placeholder() { return null; }


	public static void registerBlock(IForgeRegistry<Block> registry, String name, Block block) {
		block.setRegistryName(MorphSpellPack.MODID, name);
		block.setTranslationKey(block.getRegistryName().toString());
		block.setCreativeTab(WizardryTabs.WIZARDRY);
		registry.register(block);
	}

	public static void registerTileEntities() {
		GameRegistry.registerTileEntity(TileEntityLichPhialHolder.class, new ResourceLocation(MorphSpellPack.MODID, "lich_phial_holder"));
	}
}
