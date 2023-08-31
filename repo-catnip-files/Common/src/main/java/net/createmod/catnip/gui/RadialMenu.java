package net.createmod.catnip.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.createmod.catnip.gui.element.RenderElement;
import net.createmod.catnip.utility.theme.Color;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class RadialMenu {

	private final int sectors;

	private int innerRadius = 20;
	private int outerRadius = 80;

	public RadialMenu(int sectors) {
		this.sectors = sectors;
	}

	public RadialMenu withInnerRadius(int innerRadius) {
		this.innerRadius = innerRadius;
		return this;
	}

	public RadialMenu withOuterRadius(int outerRadius) {
		this.outerRadius = outerRadius;
		return this;
	}

	public void draw(GuiGraphics graphics, List<? extends RenderElement> elements) {
		if (sectors < 2)
			return;

		PoseStack ms = graphics.pose();

		float sectorAngle = 360f / sectors;
		int sectorWidth = outerRadius - innerRadius;

		ms.pushPose();



		for (int i = 0; i < sectors; i++) {

			Color innerColor = Color.WHITE.setAlpha(0.05f);
			Color outerColor = Color.WHITE.setAlpha(0.3f);

			if (i == 0) {
				//innerColor.mixWith(new Color(0.8f, 0.6f, 0.1f, 0.2f), 0.5f);
				outerColor.mixWith(new Color(0.8f, 0.8f, 0.2f, 0.6f), 0.5f);
			}

			ms.pushPose();

			ms.translate(0, -3, 0);
			UIRenderHelper.drawRadialSector(graphics, innerRadius - 3, outerRadius - 3, -90 - sectorAngle / 2, sectorAngle, innerColor, outerColor);

			ms.translate(0, 3 - innerRadius - (sectorWidth / 2f), 0);
			ms.mulPose(Axis.ZP.rotationDegrees(-i * sectorAngle));

			elements
					.get(i)
					.at(-12, 12)
					.render(graphics);

			//ms.translate(0, 0, 300);
			//UIRenderHelper.streak(ms, 0, 0, 0, 56, 10);
			//UIRenderHelper.streak(ms, 90, 0, 0, 56, 10);

			ms.popPose();

			ms.mulPose(Axis.ZP.rotationDegrees(sectorAngle));
		}

		ms.popPose();
	}
}
