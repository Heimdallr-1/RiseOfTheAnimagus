package com.windanesz.morphspellpack.registry;

import com.windanesz.morphspellpack.MorphSpellPack;
import electroblob.wizardry.inventory.ContainerBookshelf;
import net.minecraft.util.ResourceLocation;

import static electroblob.wizardry.block.BlockBookshelf.registerBookModelTexture;

public class BookshelfItems {

	public static void preInitBookShelfModelTextures() {
		registerBookModelTexture(() -> MSItems.lich_spell_book, new ResourceLocation(MorphSpellPack.MODID, "blocks/lich_spell_book"));

	}

	public static void InitBookshelfItems() {

		ContainerBookshelf.registerBookItem(MSItems.lich_spell_book);
	}
}
