package com.zuxelus.zlib.gui.controls;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiTextArea extends Gui {
	private final int lineCount;
	private int maxStringLength = 32;
	private int cursorCounter;
	private int cursorPosition;
	private int cursorLine;
	private boolean isFocused;
	private String[] text;

	private final FontRenderer fontRenderer;

	private final int xPos;
	private final int yPos;
	private final int width;
	private final int height;

	public GuiTextArea(FontRenderer fontRenderer, int xPos, int yPos, int width, int height, int lineCount) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.width = width;
		this.height = height;
		this.fontRenderer = fontRenderer;
		this.lineCount = lineCount;
		text = new String[lineCount];
		for (int i = 0; i < lineCount; i++)
			text[i] = "";
	}

	public String[] getText() {
		return text;
	}

	public void drawTextBox() {
		drawRect(xPos - 1, yPos - 1, xPos + width + 1, yPos + height + 1, 0xFFA0A0A0);
		drawRect(xPos, yPos, xPos + width, yPos + height, 0xFF000000);
		int textColor = 0xE0E0E0;

		int textLeft = xPos + 4;
		int textTop = yPos + (height - lineCount * (fontRenderer.FONT_HEIGHT + 1)) / 2;

		for (int i = 0; i < lineCount; i++)
			fontRenderer.drawStringWithShadow(text[i], textLeft, textTop + (fontRenderer.FONT_HEIGHT + 1) * i, textColor);
		textTop += (fontRenderer.FONT_HEIGHT + 1) * cursorLine;
		int cursorPositionX = textLeft + fontRenderer.getStringWidth(text[cursorLine].substring(0, Math.min(text[cursorLine].length(), cursorPosition))) - 1;
		boolean drawCursor = isFocused && cursorCounter / 6 % 2 == 0;
		if (drawCursor)
			drawCursorVertical(cursorPositionX, textTop - 1, cursorPositionX + 1, textTop + 1 + fontRenderer.FONT_HEIGHT);
	}

	private void drawCursorVertical(int left, int top, int right, int bottom) {
		if (left < right) {
			int i = left;
			left = right;
			right = i;
		}

		if (top < bottom) {
			int j = top;
			top = bottom;
			bottom = j;
		}

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.enableColorLogic();
		GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
		bufferbuilder.pos(left, bottom, 0.0D).endVertex();
		bufferbuilder.pos(right, bottom, 0.0D).endVertex();
		bufferbuilder.pos(right, top, 0.0D).endVertex();
		bufferbuilder.pos(left, top, 0.0D).endVertex();
		tessellator.draw();
		GlStateManager.disableColorLogic();
		GlStateManager.enableTexture2D();
	}

	public void setCursorPosition(int x, int y) {
		if (y >= text.length)
			y = text.length - 1;
		cursorPosition = x;
		cursorLine = y;
		int lineLength = text[y].length();

		if (cursorPosition < 0)
			cursorPosition = 0;

		if (cursorPosition > lineLength)
			cursorPosition = lineLength;
	}

	public void deleteFromCursor(int count) {
		if (text[cursorLine].length() != 0) {
			boolean back = count < 0;
			String curLine = text[cursorLine];
			int left = back ? cursorPosition + count : cursorPosition;
			int right = back ? cursorPosition : cursorPosition + count;
			String newLine = "";

			if (left >= 0)
				newLine = curLine.substring(0, left);

			if (right < curLine.length())
				newLine = newLine + curLine.substring(right);

			text[cursorLine] = newLine;

			if (back)
				setCursorPosition(cursorPosition + count, cursorLine);
		}
	}

	public void writeText(String additionalText) {
		String newLine = "";
		String filteredText = ChatAllowedCharacters.filterAllowedCharacters(additionalText);
		int freeCharCount = this.maxStringLength - text[cursorLine].length();

		if (text[cursorLine].length() > 0)
			newLine = newLine + text[cursorLine].substring(0, cursorPosition);

		if (freeCharCount < filteredText.length())
			newLine = newLine + filteredText.substring(0, freeCharCount);
		else
			newLine = newLine + filteredText;

		if (text[cursorLine].length() > 0 && cursorPosition < text[cursorLine].length())
			newLine = newLine + text[cursorLine].substring(cursorPosition);

		text[cursorLine] = newLine;
		setCursorPosition(cursorPosition + filteredText.length(), cursorLine);
	}

	private void setCursorLine(int delta) {
		int newCursorLine = cursorLine + delta;
		if (newCursorLine < 0)
			newCursorLine = 0;
		if (newCursorLine >= lineCount)
			newCursorLine = lineCount - 1;
		cursorPosition = Math.min(cursorPosition, text[newCursorLine].length());
		cursorLine = newCursorLine;
	}

	public void mouseClicked(int x, int y, int par3) {
		isFocused = x >= xPos && x < xPos + width && y >= yPos && y < yPos + height;
	}

	public boolean isFocused() {
		return isFocused;
	}

	public void setFocused(boolean focused) {
		isFocused = focused;
	}

	public boolean textAreaKeyTyped(char typedChar, int keyCode) {
		if (!isFocused)
			return false;
		
		switch (typedChar) {
		case 1:
			setCursorPosition(text[cursorLine].length(), cursorLine);
			return true;
		case 13:
			setCursorLine(1);
			return true;
		default:
			switch (keyCode) {
			case 14:// backspace
				deleteFromCursor(-1);
				return true;
			case Keyboard.KEY_HOME:
				setCursorPosition(0, cursorLine);
				return true;
			case Keyboard.KEY_LEFT:
				setCursorPosition(cursorPosition - 1, cursorLine);
				return true;
			case Keyboard.KEY_RIGHT:
				setCursorPosition(cursorPosition + 1, cursorLine);
				return true;
			case Keyboard.KEY_UP:
				setCursorLine(-1);
				return true;
			case Keyboard.KEY_DOWN:
				setCursorLine(1);
				return true;
			case Keyboard.KEY_END:
				setCursorPosition(text[cursorLine].length(), cursorLine);
				return true;
			case Keyboard.KEY_DELETE:
				deleteFromCursor(1);
				return true;
			default:
				if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
					writeText(Character.toString(typedChar));
					return true;
				}
				return false;
			}
		}
	}
}
