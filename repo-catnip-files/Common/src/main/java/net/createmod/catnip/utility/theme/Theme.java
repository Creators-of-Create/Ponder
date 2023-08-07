package net.createmod.catnip.utility.theme;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.createmod.catnip.utility.Couple;

public class Theme {

	private static final List<Theme> THEMES = new ArrayList<>();
	public static final Theme BASE = addTheme(new Theme());

	public static <T extends Theme> T addTheme(@Nonnull T theme) {
		THEMES.add(theme);
		THEMES.sort(Comparator.comparingInt(Theme::getPriority).reversed());
		return theme;
	}

	public static void removeTheme(Theme theme) {
		THEMES.remove(theme);
	}

	public static void reload() {
		THEMES.forEach(Theme::init);
	}

	private static ColorHolder resolve(String key) {
		return THEMES
				.stream()
				.map(theme -> theme.get(key))
				.filter(Objects::nonNull)
				.findFirst()
				.map(holder -> holder.lookupKey == null ? holder : resolve(holder.lookupKey))
				.orElse(ColorHolder.MISSING);
	}

	@Nonnull public static Couple<Color> p(String key) {return resolve(key).asPair();}

	@Nonnull public static Color c(String key, boolean first) {return p(key).get(first);}

	@Nonnull public static Color c(String key) {return resolve(key).get();}

	public static int i(String key, boolean first) {return p(key).get(first).getRGB();}

	public static int i(String key) {return resolve(key).get().getRGB();}

	//-----------//

	protected final Map<String, ColorHolder> colors;
	private int priority = 0;

	protected Theme() {
		colors = new HashMap<>();
		init();
	}

	protected void init() {
		put(Key.BUTTON_IDLE, new Color(0xdd_8ab6d6, true), new Color(0x90_8ab6d6, true));
		put(Key.BUTTON_HOVER, new Color(0xff_9ABBD3, true), new Color(0xd0_9ABBD3, true));
		put(Key.BUTTON_CLICK, new Color(0xff_ffffff), new Color(0xee_ffffff));
		put(Key.BUTTON_DISABLE, new Color(0x80_909090, true), new Color(0x60_909090, true));
		put(Key.BUTTON_SUCCESS, new Color(0xcc_88f788, true), new Color(0xcc_20cc20, true));
		put(Key.BUTTON_FAIL, new Color(0xcc_f78888, true), new Color(0xcc_cc2020, true));
		put(Key.TEXT, new Color(0xff_eeeeee), new Color(0xff_a3a3a3));
		put(Key.TEXT_DARKER, new Color(0xff_a3a3a3), new Color(0xff_808080));
		put(Key.TEXT_ACCENT_STRONG, new Color(0xff_8ab6d6), new Color(0xff_8ab6d6));
		put(Key.TEXT_ACCENT_SLIGHT, new Color(0xff_ddeeff), new Color(0xff_a0b0c0));
		put(Key.STREAK, new Color(0x101010, false));
		put(Key.VANILLA_TOOLTIP_BORDER, new Color(0x50_5000ff, true), new Color(0x50_28007f, true));
		put(Key.VANILLA_TOOLTIP_BACKGROUND, new Color(0xf0_100010, true));

		put(Key.BOX_BACKGROUND_TRANSPARENT, new Color(0xdd_000000, true));
		put(Key.BOX_BACKGROUND_FLAT, new Color(0xff_000000, false));
		put(Key.NAV_BACK_ARROW, new Color(0xf0aa9999, true), new Color(0x30aa9999, true));

		put(Key.CONFIG_TITLE_A, new Color(0xffc69fbc, true), new Color(0xfff6b8bb, true));
		put(Key.CONFIG_TITLE_B, new Color(0xfff6b8bb, true), new Color(0xfffbf994, true));
		//put(Key., new Color(0x, true), new Color(0x, true));
	}

	protected void put(String key, Color c) {
		colors.put(key, ColorHolder.single(c));
	}

	protected void put(Key key, Color c) {
		put(key.get(), c);
	}

	protected void put(String key, Color c1, Color c2) {
		colors.put(key, ColorHolder.pair(c1, c2));
	}

	protected void put(Key key, Color c1, Color c2) {
		put(key.get(), c1, c2);
	}

	protected void lookup(Key key, Key source) {
		colors.put(key.get(), ColorHolder.lookup(source.get()));
	}

	@Nullable protected ColorHolder get(String key) {
		return colors.get(key);
	}

	public int getPriority() {
		return priority;
	}

	public Theme setPriority(int priority) {
		this.priority = priority;
		return this;
	}

	public static class Key {

		private static final AtomicInteger INDEX = new AtomicInteger(0);

		public static final Key BUTTON_IDLE = new Key();
		public static final Key BUTTON_HOVER = new Key();
		public static final Key BUTTON_CLICK = new Key();
		public static final Key BUTTON_DISABLE = new Key();
		public static final Key BUTTON_SUCCESS = new Key();
		public static final Key BUTTON_FAIL = new Key();

		public static final Key TEXT = new Key();
		public static final Key TEXT_DARKER = new Key();
		public static final Key TEXT_ACCENT_STRONG = new Key();
		public static final Key TEXT_ACCENT_SLIGHT = new Key();

		public static final Key STREAK = new Key();
		public static final Key VANILLA_TOOLTIP_BORDER = new Key();
		public static final Key VANILLA_TOOLTIP_BACKGROUND = new Key();

		public static final Key BOX_BACKGROUND_TRANSPARENT = new Key();
		public static final Key BOX_BACKGROUND_FLAT = new Key();
		public static final Key NAV_BACK_ARROW = new Key();

		public static final Key CONFIG_TITLE_A = new Key();
		public static final Key CONFIG_TITLE_B = new Key();

		private final String stringKey;

		public Key() {
			this.stringKey = "_" + INDEX.getAndIncrement();
		}

		public Key(String stringKey) {
			this.stringKey = stringKey;
		}

		public String get() {
			return stringKey;
		}

		//

		public Couple<Color> p() { return Theme.p(stringKey); }

		public Color c(boolean first) { return Theme.c(stringKey, first); }

		public Color c() { return Theme.c(stringKey); }

		public int i(boolean first) { return Theme.i(stringKey, first); }

		public int i() { return Theme.i(stringKey); }
	}

	protected static class ColorHolder {

		private static final ColorHolder MISSING = ColorHolder.single(Color.BLACK);

		private Couple<Color> colors = Couple.create(Color.BLACK, Color.PURPLE);
		@Nullable
		private String lookupKey;

		private static ColorHolder single(Color c) {
			ColorHolder h = new ColorHolder();
			h.colors = Couple.create(c.setImmutable(), c.setImmutable());
			return h;
		}

		private static ColorHolder pair(Color first, Color second) {
			ColorHolder h = new ColorHolder();
			h.colors = Couple.create(first.setImmutable(), second.setImmutable());
			return h;
		}

		private static ColorHolder lookup(String key) {
			ColorHolder h = new ColorHolder();
			h.lookupKey = key;
			return h;
		}

		private Color get() {
			return colors.getFirst();
		}

		private Couple<Color> asPair() {
			return colors;
		}

	}
}
