package com.zuxelus.energycontrol.gui;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.network.NetworkHelper;
import com.zuxelus.energycontrol.tileentities.TileEntityAdvancedInfoPanel;
import com.zuxelus.zlib.gui.GuiBase;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class GuiPanelSlope extends GuiBase {
	private GuiPanelBase<?> parentGui;
	private TileEntityAdvancedInfoPanel panel;

	public GuiPanelSlope(GuiPanelBase<?> parentGui, TileEntityAdvancedInfoPanel panel) {
		super("", 171, 94, EnergyControl.MODID + ":textures/gui/gui_slope.png");
		this.parentGui = parentGui;
		this.panel = panel;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		mouseX -= guiLeft;
		mouseY -= guiTop;
		if (mouseY >= 23 && mouseY <= 89) {
			int amount = (int) ((87 - mouseY + 2) / 4);
			int offset = 0;
			if (mouseX >= 21 && mouseX <= 34) {
				offset = TileEntityAdvancedInfoPanel.OFFSET_THICKNESS;
				if (amount < 1)
					amount = 1;
			} else if (mouseX >= 79 && mouseX <= 92) {
				offset = TileEntityAdvancedInfoPanel.OFFSET_ROTATE_HOR;
				if (amount < 0)
					amount = 0;
			} else if (mouseX >= 137 && mouseX <= 150) {
				offset = TileEntityAdvancedInfoPanel.OFFSET_ROTATE_VERT;
				if (amount < 0)
					amount = 0;
			}
			NetworkHelper.updateSeverTileEntity(panel.getPos(), 10, offset + amount);
			panel.setValues(offset + amount);
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
		int textureHeight = 4 * (16 - panel.thickness);

		drawTexture(matrixStack, guiLeft + 21, guiTop + 25, 172, 0, 14, textureHeight);
		drawTexture(matrixStack, guiLeft + 79, guiTop + 25 + (panel.rotateHor < 0 ? 32 + panel.rotateHor * 4 / 7 : 32), 186, 0, 14, Math.abs(panel.rotateHor * 4 / 7));
		drawTexture(matrixStack, guiLeft + 137, guiTop + 25 + (panel.rotateVert < 0 ? 32 + panel.rotateVert * 4 / 7 : 32), 186, 0, 14, Math.abs(panel.rotateVert * 4 / 7));
	}

	@Override
	public void onClose() {
		client.setScreen(parentGui);
	}
}
