package net.createmod.catnip.utility;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Strings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;

import net.createmod.catnip.platform.CatnipClientServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public final class FontHelper {

	public static final int maxWidthPerLine = 200;

	private FontHelper() {}

	public static List<String> cutString(Font font, String text, int maxWidthPerLine) {
		// Split words
		List<String> words = new LinkedList<>();
		BreakIterator iterator = BreakIterator.getLineInstance(CatnipClientServices.CLIENT_HOOKS.getCurrentLocale());
		iterator.setText(text);
		int start = iterator.first();
		for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
			String word = text.substring(start, end);
			words.add(word);
		}
		// Apply hard wrap
		List<String> lines = new LinkedList<>();
		StringBuilder currentLine = new StringBuilder();
		int width = 0;
		for (String word : words) {
			int newWidth = font.width(word);
			if (width + newWidth > maxWidthPerLine) {
				if (width > 0) {
					String line = currentLine.toString();
					lines.add(line);
					currentLine = new StringBuilder();
					width = 0;
				} else {
					lines.add(word);
					continue;
				}
			}
			currentLine.append(word);
			width += newWidth;
		}
		if (width > 0) {
			lines.add(currentLine.toString());
		}
		return lines;
	}

	public static void drawSplitString(PoseStack ms, Font font, String text, int x, int y, int width,
		int color) {
		List<String> list = cutString(font, text, width);
		Matrix4f matrix4f = ms.last()
			.pose();

		for (String s : list) {
			float f = (float) x;
			if (font.isBidirectional()) {
				int i = font.width(font.bidirectionalShaping(s));
				f += (float) (width - i);
			}

			draw(font, s, f, (float) y, color, matrix4f, false);
			y += 9;
		}
	}

	private static int draw(Font font, String p_228078_1_, float p_228078_2_, float p_228078_3_,
		int p_228078_4_, Matrix4f p_228078_5_, boolean p_228078_6_) {
		if (p_228078_1_ == null) {
			return 0;
		} else {
			MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource.immediate(Tesselator.getInstance()
				.getBuilder());
			int i = font.drawInBatch(p_228078_1_, p_228078_2_, p_228078_3_, p_228078_4_, p_228078_6_, p_228078_5_,
				irendertypebuffer$impl, false, 0, LightTexture.FULL_BRIGHT);
			irendertypebuffer$impl.endBatch();
			return i;
		}
	}

	public static List<Component> cutStringTextComponent(String c, ChatFormatting defaultColor,
		ChatFormatting highlightColor) {
		return cutTextComponent(new TextComponent(c), defaultColor, highlightColor, 0);
	}

	public static List<Component> cutTextComponent(Component c, ChatFormatting defaultColor,
		ChatFormatting highlightColor) {
		return cutTextComponent(c, defaultColor, highlightColor, 0);
	}

	public static List<Component> cutStringTextComponent(String c, ChatFormatting defaultColor,
		ChatFormatting highlightColor, int indent) {
		return cutTextComponent(new TextComponent(c), defaultColor, highlightColor, indent);
	}

	public static List<Component> cutTextComponent(Component c, ChatFormatting defaultColor,
		ChatFormatting highlightColor, int indent) {
		String s = c.getString();

		// Apply markup
		String markedUp = s;// .replaceAll("_([^_]+)_", highlightColor + "$1" + defaultColor);

		// Split words
		List<String> words = new LinkedList<>();
		BreakIterator iterator = BreakIterator.getLineInstance(CatnipClientServices.CLIENT_HOOKS.getCurrentLocale());
		iterator.setText(markedUp);
		int start = iterator.first();
		for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
			String word = markedUp.substring(start, end);
			words.add(word);
		}

		// Apply hard wrap
		Font font = Minecraft.getInstance().font;
		List<String> lines = new LinkedList<>();
		StringBuilder currentLine = new StringBuilder();
		int width = 0;
		for (String word : words) {
			int newWidth = font.width(word.replaceAll("_", ""));
			if (width + newWidth > maxWidthPerLine) {
				if (width > 0) {
					String line = currentLine.toString();
					lines.add(line);
					currentLine = new StringBuilder();
					width = 0;
				} else {
					lines.add(word);
					continue;
				}
			}
			currentLine.append(word);
			width += newWidth;
		}
		if (width > 0) {
			lines.add(currentLine.toString());
		}

		// Format
		MutableComponent lineStart = new TextComponent(Strings.repeat(" ", indent));
		lineStart.withStyle(defaultColor);
		List<Component> formattedLines = new ArrayList<>(lines.size());
		Couple<ChatFormatting> f = Couple.create(highlightColor, defaultColor);

		boolean currentlyHighlighted = false;
		for (String string : lines) {
			MutableComponent currentComponent = lineStart.plainCopy();
			String[] split = string.split("_");
			for (String part : split) {
				currentComponent.append(new TextComponent(part).withStyle(f.get(currentlyHighlighted)));
				currentlyHighlighted = !currentlyHighlighted;
			}

			formattedLines.add(currentComponent);
			currentlyHighlighted = !currentlyHighlighted;
		}

		return formattedLines;
	}
}
