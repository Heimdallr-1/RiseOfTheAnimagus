package com.windanesz.morphspellpack.client.gui;

import com.windanesz.morphspellpack.MorphSpellPack;
import com.windanesz.morphspellpack.registry.MSItems;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.client.gui.GuiButtonTurnPage;
import electroblob.wizardry.registry.WizardrySounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.RandomStringUtils;
import org.lwjgl.input.Keyboard;

import java.util.Random;

public class GuiLichTome extends GuiScreen {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(MorphSpellPack.MODID, "textures/gui/lich_tome.png");
	protected static final ResourceLocation TEXTURE_DOUBLE_PAGE = new ResourceLocation(MorphSpellPack.MODID, "textures/gui/lich_tome.png");
	private static final int PAGE_BUTTON_INSET_X = 22, PAGE_BUTTON_INSET_Y = 13;

	private final int xSize = 288;
	private final int ySize = 180;
	protected int textureWidth = 512;
	protected int textureHeight = 256;
	boolean doublePage = false;

	private GuiButton nextPageButton;
	private GuiButton prevPageButton;

	private ItemStack ritualBook;
	private String glyphName;
	private int currentPage = 0;

	public GuiLichTome(ItemStack ritualBook) {
		super();
		this.ritualBook = ritualBook;
	}

	public ResourceLocation getTexture() {
		return doublePage && currentPage == 0 ? TEXTURE_DOUBLE_PAGE : TEXTURE;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		int left = this.width / 2 - xSize / 2;
		int top = this.height / 2 - this.ySize / 2;
		this.drawDefaultBackground();
		this.drawBackgroundLayer(left, top, mouseX, mouseY);
		super.drawScreen(mouseX, mouseY, partialTicks); // Just draws the buttons
		this.drawForegroundLayer(left, top, mouseX, mouseY);
	}

	/**
	 * Draws the background of the ritual info GUI. This is called before buttons are drawn.
	 *
	 * @param left   The x-coordinate of the left-hand edge of the GUI
	 * @param top    The y-coordinate of the top edge of the GUI
	 * @param mouseX The current x position of the mouse pointer
	 * @param mouseY The current y position of the mouse pointer
	 */
	@SuppressWarnings("Duplicates")
	protected void drawBackgroundLayer(int left, int top, int mouseX, int mouseY) {
		// Draw book background texture
		Minecraft.getMinecraft().renderEngine.bindTexture(getTexture());
		DrawingUtils.drawTexturedRect(left, top, 0, 0, xSize, ySize, textureWidth, textureHeight);
	}

	/**
	 * Draws the foreground of the ritual info GUI. This is called after buttons are drawn.
	 *
	 * @param left   The x-coordinate of the left-hand edge of the GUI
	 * @param top    The y-coordinate of the top edge of the GUI
	 * @param mouseX The current x position of the mouse pointer
	 * @param mouseY The current y position of the mouse pointer
	 */
	protected void drawForegroundLayer(int left, int top, int mouseX, int mouseY) {
		String descString = "gui." + ritualBook.getItem().getRegistryName().toString() + ".desc";
		String fullDesc = Wizardry.proxy.translate(descString);
		boolean discovered = true;
		int splitIndex = discovered ? 380 : 200;
		if (doublePage) {
			int i = 380;

			for (int j = 0; j < 30; j++) {
				if (fullDesc.length() > i && Character.isWhitespace(fullDesc.charAt(i))) {
					break;
				}
				if (j < 15) {
					i++;
				} else {
					i--;
				}
			}
			splitIndex = i;
		}

		String name = "aa";
		String rightSideDesc = "";

		if (discovered) {
			String nameString = "gui." + (ritualBook.getItem().getRegistryName() + ".title");
			name = Wizardry.proxy.translate(nameString);
		}

		String desc = fullDesc;
		this.fontRenderer.setUnicodeFlag(true);
		this.fontRenderer.drawString(name, left + 20, top + 20, 0);
		this.fontRenderer.setUnicodeFlag(false);

		if (doublePage && currentPage == 0) {
			doublePage = true;
			desc = fullDesc.substring(0, splitIndex);
			rightSideDesc = fullDesc.substring(splitIndex);
		} else {
			RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();
			ItemStack stackTooltip = ItemStack.EMPTY;
			//int patternWidth = ritual.getRitualProperties().width;
			//int patternHeight = ritual.getRitualProperties().height;
//			int offset = patternWidth * 16 / 2;
//			int x = left + 146 + 48 + 16 - offset;
//			int y = top + 16 + 74 + -offset;
//			int index = 0;
//			for (int j = 0; j < patternHeight; j++) {
//				for (int k = 0; k < patternWidth; k++) {
//					int xPos = x + 16 * k;
//					if (ritual.getRitualProperties().pattern.get(index).getMatchingStacks().length > 0) {
//						Item item = ritual.getRitualProperties().pattern.get(index).getMatchingStacks()[0].getItem();
//						//					matrix[j][k] = ritual.getRitualProperties().pattern.get(index).getMatchingStacks()[0].getItem();
//						ItemStack stack = new ItemStack(item);
//						itemRenderer.renderItemAndEffectIntoGUI(stack, xPos, y);
//						if (mouseX >= xPos && mouseX <= xPos + 16 && mouseY >= y && mouseY <= y + 16) {
//							stackTooltip = stack;
//						}
//					}
//
//					index++;
//				}
//				y += 16;
//			}
			if (stackTooltip != ItemStack.EMPTY) {
				renderToolTip(stackTooltip, mouseX, mouseY);
			}
		}

		if (discovered) {
			// left side
			if (currentPage == 0) {
				this.fontRenderer.setUnicodeFlag(true);
				this.fontRenderer.drawSplitString(desc, left + 17, top + 44, 118, 0);
				this.fontRenderer.setUnicodeFlag(false);
			}

			// right side
			if (doublePage && currentPage == 0) {
				this.fontRenderer.setUnicodeFlag(true);
				this.fontRenderer.drawSplitString(rightSideDesc, left + 17 + 133, top + 14, 118, 0);
				this.fontRenderer.setUnicodeFlag(false);
			}

		}

	}

	@Override
	public void initGui() {
		super.initGui();
		int buttonID = 0;
		final int left = this.width / 2 - this.xSize / 2;
		final int top = this.height / 2 - this.ySize / 2;
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();

		doublePage = true;

		if (doublePage) {

			this.buttonList.add(nextPageButton = new GuiButtonTurnPage(buttonID++, left + xSize - PAGE_BUTTON_INSET_X - GuiButtonTurnPage.WIDTH,
					top + ySize - PAGE_BUTTON_INSET_Y - GuiButtonTurnPage.HEIGHT, GuiButtonTurnPage.Type.NEXT_PAGE, TEXTURE, textureWidth, textureHeight));

			this.buttonList.add(prevPageButton = new GuiButtonTurnPage(buttonID++, left + PAGE_BUTTON_INSET_X,
					top + ySize - PAGE_BUTTON_INSET_Y - GuiButtonTurnPage.HEIGHT, GuiButtonTurnPage.Type.PREVIOUS_PAGE, TEXTURE, textureWidth, textureHeight));
			prevPageButton.visible = currentPage > 0;
		}

		this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(WizardrySounds.MISC_BOOK_OPEN, 1));
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return Wizardry.settings.booksPauseGame;
	}

	private String generateRandomName(Random random) {

		String name = "";

		for (int i = 0; i < random.nextInt(2) + 2; i++) {
			name = name + RandomStringUtils.random(3 + random.nextInt(5), "abcdefghijklmnopqrstuvwxyz") + " ";
		}

		return name.trim();
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		int lastPage = 1;
		if (button == nextPageButton) {
			if (currentPage < lastPage)
				currentPage++;

		} else if (button == prevPageButton) {
			if (currentPage > 0)
				currentPage--;
		}

		if (currentPage == 0) {
			prevPageButton.visible = false;
			nextPageButton.visible = true;
		} else {
			nextPageButton.visible = false;
			prevPageButton.visible = true;

		}

	}
}
