package net.createmod.ponder.foundation;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import net.createmod.catnip.utility.Couple;
import net.createmod.ponder.Ponder;
import net.createmod.ponder.foundation.ui.AbstractPonderScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

public class PonderLocalization {

	static final Map<ResourceLocation, String> SHARED = new HashMap<>();
	static final Map<ResourceLocation, Couple<String>> TAG = new HashMap<>();
	static final Map<ResourceLocation, String> CHAPTER = new HashMap<>();
	static final Map<ResourceLocation, Map<String, String>> SPECIFIC = new HashMap<>();

	//

	public static void registerShared(ResourceLocation key, String enUS) {
		SHARED.put(key, enUS);
	}

	public static void registerTag(ResourceLocation key, String enUS, String description) {
		TAG.put(key, Couple.create(enUS, description));
	}

	public static void registerChapter(ResourceLocation key, String enUS) {
		CHAPTER.put(key, enUS);
	}

	public static void registerSpecific(ResourceLocation sceneId, String key, String enUS) {
		SPECIFIC.computeIfAbsent(sceneId, $ -> new HashMap<>())
			.put(key, enUS);
	}

	//

	public static String getShared(ResourceLocation key) {
		if (PonderRegistry.editingModeActive())
			return SHARED.containsKey(key) ? SHARED.get(key) : ("unregistered shared entry: " + key);
		return I18n.get(langKeyForShared(key));
	}

	public static String getTag(ResourceLocation key) {
		if (PonderRegistry.editingModeActive())
			return TAG.containsKey(key) ? TAG.get(key)
				.getFirst() : ("unregistered tag entry: " + key);
		return I18n.get(langKeyForTag(key));
	}

	public static String getTagDescription(ResourceLocation key) {
		if (PonderRegistry.editingModeActive())
			return TAG.containsKey(key) ? TAG.get(key)
				.getSecond() : ("unregistered tag entry: " + key);
		return I18n.get(langKeyForTagDescription(key));
	}

	public static String getChapter(ResourceLocation key) {
		if (PonderRegistry.editingModeActive())
			return CHAPTER.containsKey(key) ? CHAPTER.get(key) : ("unregistered chapter entry: " + key);
		return I18n.get(langKeyForChapter(key));
	}

	public static String getSpecific(ResourceLocation sceneId, String k) {
		if (PonderRegistry.editingModeActive())
			return SPECIFIC.get(sceneId)
				.get(k);
		return I18n.get(langKeyForSpecific(sceneId, k));
	}

	//

	public static final String LANG_PREFIX = "ponder.";
	public static final String UI_PREFIX = "ui.";

	public static void record(String namespace, JsonObject object) {
		SHARED.forEach((k, v) -> {
			if (k.getNamespace().equals(namespace)) {
				object.addProperty(langKeyForShared(k), v);
			}
		});

		TAG.forEach((k, v) -> {
			if (k.getNamespace().equals(namespace)) {
				object.addProperty(langKeyForTag(k), v.getFirst());
				object.addProperty(langKeyForTagDescription(k), v.getSecond());
			}
		});

		CHAPTER.forEach((k, v) -> {
			if (k.getNamespace().equals(namespace)) {
				object.addProperty(langKeyForChapter(k), v);
			}
		});

		SPECIFIC.entrySet()
			.stream()
			.filter(entry -> entry.getKey().getNamespace().equals(namespace))
			.sorted(Map.Entry.comparingByKey())
			.forEach(entry -> {
				entry.getValue()
					.entrySet()
					.stream()
					.sorted(Map.Entry.comparingByKey())
					.forEach(subEntry -> object.addProperty(
						langKeyForSpecific(entry.getKey(), subEntry.getKey()), subEntry.getValue()));
			});
	}

	private static void recordGeneral(JsonObject object) {
		addGeneral(object, PonderTooltipHandler.HOLD_TO_PONDER, "Hold [%1$s] to Ponder");
		addGeneral(object, PonderTooltipHandler.SUBJECT, "Subject of this scene");
		addGeneral(object, AbstractPonderScreen.PONDERING, "Pondering about...");
		addGeneral(object, AbstractPonderScreen.IDENTIFY_MODE, "Identify mode active.\nUnpause with [%1$s]");
		addGeneral(object, AbstractPonderScreen.ASSOCIATED, "Associated Entries");

		addGeneral(object, AbstractPonderScreen.CLOSE, "Close");
		addGeneral(object, AbstractPonderScreen.IDENTIFY, "Identify");
		addGeneral(object, AbstractPonderScreen.NEXT, "Next Scene");
		addGeneral(object, AbstractPonderScreen.NEXT_UP, "Up Next:");
		addGeneral(object, AbstractPonderScreen.PREVIOUS, "Previous Scene");
		addGeneral(object, AbstractPonderScreen.REPLAY, "Replay");
		addGeneral(object, AbstractPonderScreen.THINK_BACK, "Think Back");
		addGeneral(object, AbstractPonderScreen.SLOW_TEXT, "Comfy Reading");

		addGeneral(object, AbstractPonderScreen.EXIT, "Exit");
		addGeneral(object, AbstractPonderScreen.WELCOME, "Welcome to Ponder");
		addGeneral(object, AbstractPonderScreen.CATEGORIES, "Available Categories for %1$s");
		addGeneral(object, AbstractPonderScreen.DESCRIPTION,
			"Click one of the icons below to learn about its associated Items and Blocks");
		addGeneral(object, AbstractPonderScreen.INDEX_TITLE, "Ponder Index");
	}

	private static void addGeneral(JsonObject json, String key, String enUS) {
		json.addProperty(Ponder.MOD_ID + "." + key, enUS);
	}

	public static void generateSceneLang() {
		PonderRegistry.ALL.forEach((id, list) -> {
			for (int i = 0; i < list.size(); i++)
				PonderRegistry.compileScene(i, list.get(i), null);
		});
	}

	/**
	 * Generate a JsonObject holding all Lang-entries and their enUS default that was declared in code
	 */
	public static JsonObject provideLangEntries(String modID) {
		PonderIndex.registerAll();
		PonderIndex.gatherSharedText();

		generateSceneLang();

		JsonObject object = new JsonObject();
		if (modID.equals(Ponder.MOD_ID))
			recordGeneral(object);

		record(modID, object);
		return object;
	}

	/*public static void provideRegistrateLang(AbstractRegistrate<?> registrate) {
		generateSceneLang();

		JsonObject object = new JsonObject();
		record(registrate.getModid(), object);

		for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
			registrate.addRawLang(entry.getKey(), entry.getValue().getAsString());
		}
	}*/

	//

	protected static String langKeyForShared(ResourceLocation k) {
		return k.getNamespace() + "." + LANG_PREFIX + "shared." + k.getPath();
	}

	protected static String langKeyForTag(ResourceLocation k) {
		return k.getNamespace() + "." + LANG_PREFIX + "tag." + k.getPath();
	}

	protected static String langKeyForTagDescription(ResourceLocation k) {
		return k.getNamespace() + "." + LANG_PREFIX + "tag." + k.getPath() + ".description";
	}

	protected static String langKeyForChapter(ResourceLocation k) {
		return k.getNamespace() + "." + LANG_PREFIX + "chapter." + k.getPath();
	}

	protected static String langKeyForSpecific(ResourceLocation sceneId, String k) {
		return sceneId.getNamespace() + "." + LANG_PREFIX + sceneId.getPath() + "." + k;
	}

}
