package com.zuxelus.energycontrol.renderers;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.tileentities.TileEntityTimer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;

public class TileEntityTimerRenderer extends TileEntitySpecialRenderer<TileEntityTimer> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(
			EnergyControl.MODID + ":textures/blocks/timer/all.png");
	private static final CubeRenderer model = new CubeRenderer(2, 0, 2, 28, 14, 28, 128, 64, 0, 0);
	private static final CubeRenderer DESTROY = new CubeRenderer(2, 0, 2, 28, 14, 28, 32, 32, 0, 0);

	@Override
	public void renderTileEntityAt(TileEntityTimer te, double x, double y, double z, float partialTicks, int destroyStage) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		switch (te.getFacing()) {
		case UP:
			switch (te.getRotation()) {
			case NORTH:
				GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(-1.0F, 0.0F, -1.0F);
				break;
			case SOUTH:
				break;
			case WEST:
				GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(0.0F, 0.0F, -1.0F);
				break;
			case EAST:
				GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(-1.0F, 0.0F, 0.0F);
				break;
			}
			break;
		case NORTH:
			GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.translate(-1.0F, -1.0F, -1.0F);
			switch (te.getRotation()) {
			case UP:
				GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(-1.0F, 0.0F, -1.0F);
				break;
			case DOWN:
				break;
			case EAST:
				GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(0.0F, 0.0F, -1.0F);
				break;
			case WEST:
				GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(-1.0F, 0.0F, 0.0F);
				break;
			}
			break;
		case SOUTH:
			GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.translate(0.0F, 0.0F, -1.0F);
			switch (te.getRotation()) {
			case UP:
				GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(-1.0F, 0.0F, -1.0F);
				break;
			case DOWN:
				break;
			case WEST:
				GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(0.0F, 0.0F, -1.0F);
				break;
			case EAST:
				GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(-1.0F, 0.0F, 0.0F);
				break;
			}
			break;
		case DOWN:
			GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.translate(0.0F, -1.0F, -1.0F);
			break;
		case WEST:
			GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.translate(0.0F, -1.0F, -1.0F);
			switch (te.getRotation()) {
			case UP:
				GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(-1.0F, 0.0F, -1.0F);
				break;
			case DOWN:
				break;
			case NORTH:
				GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(0.0F, 0.0F, -1.0F);
				break;
			case SOUTH:
				GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(-1.0F, 0.0F, 0.0F);
				break;
			}
			break;
		case EAST:
			GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.translate(-1.0F, 0.0F, -1.0F);
			switch (te.getRotation()) {
			case UP:
				GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(-1.0F, 0.0F, -1.0F);
				break;
			case DOWN:
				break;
			case SOUTH:
				GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(0.0F, 0.0F, -1.0F);
				break;
			case NORTH:
				GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(-1.0F, 0.0F, 0.0F);
				break;
			}
			break;
		}

		if (destroyStage > -1) {
			bindTexture(DESTROY_STAGES[destroyStage]);
			DESTROY.render(0.03125F);
		} else {
			bindTexture(TEXTURE);
			model.render(0.03125F);
			String time = te.getTimeString();
			GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.translate(0.5F, 0.575F, -0.4376F);
			GlStateManager.scale(0.015625F, 0.015625F, 0.015625F);
			getFontRenderer().drawString(time, -getFontRenderer().getStringWidth(time) / 2, -getFontRenderer().FONT_HEIGHT, 0x000000);
		}
		GlStateManager.popMatrix();
	}
}
