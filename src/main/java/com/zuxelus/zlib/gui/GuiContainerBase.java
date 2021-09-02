package com.zuxelus.zlib.gui;

import java.text.DecimalFormat;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiContainerBase<T extends Container> extends ContainerScreen<T> {
	private static final int oX[] = {0, -1, 0, 1};
	private static final int oY[] = {-1, 0, 1, 0};
	private static final int MASKR = 0xFF0000;
	private static final int MASKG = 0x00FF00;
	private static final int MASKB = 0x0000FF;
	protected static final int GREEN = 0x55FF55;
	protected static final int RED = 0xFF5555;
	protected static final int GREENGLOW = multiplyColorComponents(GREEN, 0.16F);
	protected static final int REDGLOW = multiplyColorComponents(RED, 0.16F);
	protected DecimalFormat fraction = new DecimalFormat("##0.00");

	private final ResourceLocation texture;

	public GuiContainerBase(T container, PlayerInventory inv, ITextComponent name, ResourceLocation texture) {
		super(container, inv, name);
		this.texture = texture;
	}

	@Override
	protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bind(texture);
		blit(matrixStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}

	public void drawCenteredText(MatrixStack matrixStack, ITextComponent text, int x, int y) {
		drawCenteredText(matrixStack, text, x, y, 0x404040);
	}

	public void drawRightAlignedText(MatrixStack matrixStack, String text, int x, int y) {
		drawRightAlignedText(matrixStack, text, x, y, 0x404040);
	}

	public void drawLeftAlignedText(MatrixStack matrixStack, String text, int x, int y) {
		drawLeftAlignedText(matrixStack, text, x, y, 0x404040);
	}

	public void drawCenteredText(MatrixStack matrixStack, ITextComponent text, int x, int y, int color) {
		IReorderingProcessor ireorderingprocessor = text.getVisualOrderText();
		font.draw(matrixStack, ireorderingprocessor, (x - font.width(ireorderingprocessor)) / 2, y, color);
	}

	public void drawRightAlignedText(MatrixStack matrixStack, String text, int x, int y, int color) {
		font.draw(matrixStack, text, x - font.width(text), y, color);
	}

	public void drawLeftAlignedText(MatrixStack matrixStack, String text, int x, int y, int color) {
		font.draw(matrixStack, text, x, y, color);
	}

	public void drawRightAlignedGlowingText(MatrixStack matrixStack, String text, int x, int y, int color, int glowColor) {
		drawGlowingText(matrixStack, text, x - font.width(text), y, color, glowColor);
	}

	public void drawGlowingText(MatrixStack matrixStack, String text, int x, int y, int color, int glowColor) {
		for (int i = 0; i < 4; i++)
			font.draw(matrixStack, text, x + oX[i], y + oY[i], glowColor);
		font.draw(matrixStack, text, x, y, color);
	}

	public void drawCenteredGlowingText(MatrixStack matrixStack, String text, int x, int y, int color, int glowColor) {
		drawGlowingText(matrixStack, text, x - font.width(text) / 2, y, color, glowColor);
	}

	public static int multiplyColorComponents(int color, float brightnessFactor) {
		return ((int) (brightnessFactor * (color & MASKR)) & MASKR) | ((int) (brightnessFactor * (color & MASKG)) & MASKG) | ((int) (brightnessFactor * (color & MASKB)) & MASKB);
	}

	protected TextFieldWidget addTextFieldWidget(int left, int top, int width, int height, boolean isEnabled, String text) {
		TextFieldWidget textBox = new TextFieldWidget(font, leftPos + left, topPos + top, width, height, null, StringTextComponent.EMPTY);
		textBox.setEditable(isEnabled);
		textBox.changeFocus(isEnabled);
		textBox.setValue(text);
		children.add(textBox);
		setInitialFocus(textBox);
		return textBox;
	}
}
