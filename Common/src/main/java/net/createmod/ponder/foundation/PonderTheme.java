package net.createmod.ponder.foundation;

import net.createmod.catnip.utility.theme.Color;
import net.createmod.catnip.utility.theme.Theme;

public class PonderTheme extends Theme {

	public static final PonderTheme PONDER_THEME = addTheme(new PonderTheme());

	@Override
	protected void init() {

		put(PonderTheme.Key.PONDER_BUTTON_IDLE, new Color(0x60_c0c0ff, true), new Color(0x30_c0c0ff, true));
		put(PonderTheme.Key.PONDER_BUTTON_HOVER, new Color(0xf0_c0c0ff, true), new Color(0xa0_c0c0ff, true));
		put(PonderTheme.Key.PONDER_BUTTON_CLICK, new Color(0xff_ffffff), new Color(0xdd_ffffff));
		put(PonderTheme.Key.PONDER_BUTTON_DISABLE, new Color(0x80_909090, true), new Color(0x20_909090, true));

		put(PonderTheme.Key.PONDER_BACKGROUND_TRANSPARENT, new Color(0xdd_000000, true));
		put(PonderTheme.Key.PONDER_BACKGROUND_FLAT, new Color(0xff_000000, false));
		put(PonderTheme.Key.PONDER_BACKGROUND_IMPORTANT, new Color(0xdd_0e0e20, true));
		put(PonderTheme.Key.PONDER_IDLE, new Color(0x40ffeedd, true), new Color(0x20ffeedd, true));
		put(PonderTheme.Key.PONDER_HOVER, new Color(0x70ffffff, true), new Color(0x30ffffff, true));
		put(PonderTheme.Key.PONDER_HIGHLIGHT, new Color(0xf0ffeedd, true), new Color(0x60ffeedd, true));
		put(PonderTheme.Key.TEXT_WINDOW_BORDER, new Color(0x607a6000, true), new Color(0x207a6000, true));
		put(PonderTheme.Key.PONDER_PROGRESSBAR, new Color(0x80ffeedd, true), new Color(0x50ffeedd, true));
		put(PonderTheme.Key.PONDER_MISSING_MODDED, new Color(0x70_984500, true), new Color(0x70_692400, true));
		lookup(PonderTheme.Key.PONDER_MISSING_VANILLA, Theme.Key.VANILLA_TOOLTIP_BORDER);
	}

	public static void loadClass() {}

	public static class Key {

		public static final Theme.Key PONDER_BACKGROUND_TRANSPARENT = new Theme.Key();
		public static final Theme.Key PONDER_BACKGROUND_FLAT = new Theme.Key();
		public static final Theme.Key PONDER_BACKGROUND_IMPORTANT = new Theme.Key();
		public static final Theme.Key PONDER_IDLE = new Theme.Key();
		public static final Theme.Key PONDER_HOVER = new Theme.Key();
		public static final Theme.Key PONDER_HIGHLIGHT = new Theme.Key();
		public static final Theme.Key TEXT_WINDOW_BORDER = new Theme.Key();
		public static final Theme.Key PONDER_PROGRESSBAR = new Theme.Key();
		public static final Theme.Key PONDER_MISSING_MODDED = new Theme.Key();
		public static final Theme.Key PONDER_MISSING_VANILLA = new Theme.Key();

		public static final Theme.Key PONDER_BUTTON_IDLE = new Theme.Key();
		public static final Theme.Key PONDER_BUTTON_HOVER = new Theme.Key();
		public static final Theme.Key PONDER_BUTTON_CLICK = new Theme.Key();
		public static final Theme.Key PONDER_BUTTON_DISABLE = new Theme.Key();

	}
}
