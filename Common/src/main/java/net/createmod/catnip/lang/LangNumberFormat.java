package net.createmod.catnip.lang;

import java.text.NumberFormat;
import java.util.Locale;

import net.createmod.catnip.platform.CatnipClientServices;
import net.minecraft.util.Mth;

public class LangNumberFormat {

	private NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
	public static LangNumberFormat numberFormat = new LangNumberFormat();

	public NumberFormat get() {
		return format;
	}

	public void update() {
		format = NumberFormat.getInstance(CatnipClientServices.CLIENT_HOOKS.getCurrentLocale());
		format.setMaximumFractionDigits(2);
		format.setMinimumFractionDigits(0);
		format.setGroupingUsed(true);
	}

	public static String format(double d) {
		if (Mth.equal(d, 0))
			d = 0;
		return numberFormat.get()
			.format(d)
			.replace("\u00A0", " ");
	}

}
