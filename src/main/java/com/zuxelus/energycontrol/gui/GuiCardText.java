package com.zuxelus.energycontrol.gui;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.items.cards.ItemCardReader;
import com.zuxelus.energycontrol.tileentities.TileEntityInfoPanel;
import com.zuxelus.zlib.gui.GuiBase;
import com.zuxelus.zlib.gui.controls.GuiTextArea;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;

@Environment(EnvType.CLIENT)
public class GuiCardText extends GuiBase {
	private ICardReader reader;
	private ItemStack stack;
	private TileEntityInfoPanel panel;
	private GuiPanelBase<?> parentGui;
	private int slot;
	private GuiTextArea textArea;

	private static final int lineCount = 10;

	public GuiCardText(ItemStack card, TileEntityInfoPanel panel, GuiPanelBase<?> gui, int slot) {
		super("", 226, 146, EnergyControl.MODID + ":textures/gui/gui_text_card.png");
		this.reader = new ItemCardReader(card);
		this.stack = card;
		this.panel = panel;
		parentGui = gui;
		this.slot = slot;
	}

	@Override
	public void init() {
		super.init();
		addDrawableChild(new ButtonWidget(guiLeft + xSize - 60 - 8, guiTop + 120, 60, 20, ScreenTexts.DONE, (button) -> { actionPerformed(1); }));
		addDrawableChild(new ButtonWidget(guiLeft + 8, guiTop + 120, 60, 20, new LiteralText("Style"), (button) -> { actionPerformed(2); }));
		textArea = new GuiTextArea(textRenderer, guiLeft + 8, guiTop + 5, xSize - 16, ySize - 35, lineCount);
		addSelectableChild(textArea);
		setInitialFocus(textArea);
		String[] data = textArea.getText();
		for (int i = 0; i < lineCount; i++)
			data[i] = reader.getString("line_" + i);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
		textArea.render(matrixStack, mouseY, mouseY, partialTicks);
	}

	@Override
	public void tick() {
		super.tick();
		textArea.updateCursorCounter();
	}

	private void actionPerformed(int id) {
		switch (id) {
		case 1:
			if (textArea != null) {
				String[] lines = textArea.getText();
				if (lines != null)
					for (int i = 0; i < lines.length; i++)
						reader.setString("line_" + i, lines[i]);
			}
			reader.updateServer(stack, panel, slot);
			client.setScreen(parentGui);
			break;
		case 2:
			textArea.writeText("@");
			break;
		}
	}

	@Override
	public boolean mouseClicked(double x, double y, int p_94697_) {
		Element control = getFocused();
		if (control instanceof GuiTextArea) {
			boolean result = super.mouseClicked(x, y, p_94697_);
			setFocused(control);
			return result;
		}
		return super.mouseClicked(x, y, p_94697_);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 256) {
			actionPerformed(1);
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void onClose() {
		actionPerformed(1);
		super.onClose();
	}
}
