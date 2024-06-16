package com.windanesz.morphspellpack.client.render;

import com.windanesz.morphspellpack.block.TileEntityLichPhialHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class TileEntityLichPhialHolderRenderer extends TileEntitySpecialRenderer<TileEntityLichPhialHolder> {

	@Override
	public void render(TileEntityLichPhialHolder te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		super.render(te, x, y, z, partialTicks, destroyStage, alpha);

		// Get the player's current coordinates
		EntityPlayer player = Minecraft.getMinecraft().player;
		double playerX = player.posX;
		double playerY = player.posY;
		double playerZ = player.posZ;

		// Calculate the actual world coordinates of the TileEntity
		double entityX = playerX + x + 0.5;
		double entityY = playerY + y + 0.5;
		double entityZ = playerZ + z + 0.5;

		// Calculate the distance to the TileEntity using relative coordinates
		double distance = Math.sqrt(
				x * x + y * y + z * z
		);


		// Only render the label if the player is within 12 blocks
		if (distance <= 5) {
			// Calculate the fill percentage
			float fillPercentage = Math.round(te.getPercentFilled() * 100);


			// Render the fill percentage as a nameplate
			renderName(te, fillPercentage + "%", x, y, z, 12);
		}
	}

	protected void renderName(TileEntityLichPhialHolder te, String str, double x, double y, double z, int maxDistance) {
		FontRenderer fontrenderer = this.getFontRenderer();
		float f = 1.6F;
		float f1 = 0.016666668F * f;
		GlStateManager.pushMatrix();
		GlStateManager.translate((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F); // Adjusted y-value
		GL11.glNormal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-this.rendererDispatcher.entityYaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(this.rendererDispatcher.entityPitch, 1.0F, 0.0F, 0.0F);
		GlStateManager.scale(-f1, -f1, f1);
		GlStateManager.disableLighting();
		GlStateManager.depthMask(false);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		int i = fontrenderer.getStringWidth(str) / 2;
		GlStateManager.disableTexture2D();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
		bufferbuilder.pos((double)(-i - 1), -1.0D, 0.0D).color(0, 0, 0, 0.9F).endVertex();
		bufferbuilder.pos((double)(-i - 1), 8.0D, 0.0D).color(0, 0, 0, 0.9F).endVertex();
		bufferbuilder.pos((double)(i + 1), 8.0D, 0.0D).color(0, 0, 0, 0.9F).endVertex();
		bufferbuilder.pos((double)(i + 1), -1.0D, 0.0D).color(0, 0, 0, 0.9F).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();
		fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, 0, 0x19d194);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}

}