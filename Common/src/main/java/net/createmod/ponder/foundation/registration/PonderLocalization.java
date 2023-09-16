package net.createmod.ponder.foundation.registration;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import net.createmod.catnip.utility.Couple;
import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.registration.LangRegistryAccess;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderTooltipHandler;
import net.createmod.ponder.foundation.ui.AbstractPonderScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

public class PonderLocalization implements LangRegistryAccess {

	public final Map<ResourceLocation, String> shared = new HashMap<>();
	public final Map<ResourceLocation, Couple<String>> tag = new HashMap<>();
	public final Map<ResourceLocation, Map<String, String>> specific = new HashMap<>();

	//

	public void clearAll() {
		shared.clear();
		tag.clear();
		specific.clear();
	}

	public void clearShared() {
		shared.clear();
	}

	public void registerShared(ResourceLocation key, String enUS) {
		shared.put(key, enUS);
	}

	public void registerTag(ResourceLocation key, String title, String description) {
		tag.put(key, Couple.create(title, description));
	}

	public void registerSpecific(ResourceLocation sceneId, String key, String enUS) {
		specific.computeIfAbsent(sceneId, $ -> new HashMap<>())
				.put(key, enUS);
	}

	//

	@Override
	public String getShared(ResourceLocation key) {
		if (PonderIndex.editingModeActive())
			return shared.containsKey(key) ? shared.get(key) : ("unregistered shared entry: " + key);
		return I18n.get(langKeyForShared(key));
	}

	@Override
	public String getTagName(ResourceLocation key) {
		if (PonderIndex.editingModeActive())
			return tag.containsKey(key) ? tag.get(key)
					.getFirst() : ("unregistered tag entry: " + key);
		return I18n.get(langKeyForTag(key));
	}

	@Override
	public String getTagDescription(ResourceLocation key) {
		if (PonderIndex.editingModeActive())
			return tag.containsKey(key) ? tag.get(key)
					.getSecond() : ("unregistered tag entry: " + key);
		return I18n.get(langKeyForTagDescription(key));
	}

	@Override
	public String getSpecific(ResourceLocation sceneId, String k) {
		if (PonderIndex.editingModeActive())
			try {
				return specific.get(sceneId).get(k);
			} catch (Exception e) {
				return "MISSING_SPECIFIC";
			}
		return I18n.get(langKeyForSpecific(sceneId, k));
	}

	//

	public static final String LANG_PREFIX = "ponder.";
	public static final String UI_PREFIX = "ui.";

	public void record(String namespace, JsonObject object) {
		shared.forEach((k, v) -> {
			if (k.getNamespace().equals(namespace)) {
				object.addProperty(langKeyForShared(k), v);
			}
		});

		tag.forEach((k, v) -> {
			if (k.getNamespace().equals(namespace)) {
				object.addProperty(langKeyForTag(k), v.getFirst());
				object.addProperty(langKeyForTagDescription(k), v.getSecond());
			}
		});

		specific.entrySet()
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

	private void recordGeneral(JsonObject object) {
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

	private void addGeneral(JsonObject json, String key, String enUS) {
		json.addProperty(Ponder.MOD_ID + "." + key, enUS);
	}

	public void generateSceneLang() {
		PonderIndex.getSceneAccess()
				.getRegisteredEntries()
				.forEach(entry -> PonderSceneRegistry.compileScene(this, entry.getValue(), null));
	}

	/**
	 * Generate a JsonObject holding all Lang-entries and their enUS default that was declared in code
	 */
	@Override
	public JsonObject provideLangEntries(String modID) {
		PonderIndex.registerAll();
		PonderIndex.gatherSharedText();

		generateSceneLang();

		JsonObject object = new JsonObject();
		if (modID.equals(Ponder.MOD_ID))
			recordGeneral(object);

		record(modID, object);
		return object;
	}

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

	protected static String langKeyForSpecific(ResourceLocation sceneId, String k) {
		return sceneId.getNamespace() + "." + LANG_PREFIX + sceneId.getPath() + "." + k;
	}

}
