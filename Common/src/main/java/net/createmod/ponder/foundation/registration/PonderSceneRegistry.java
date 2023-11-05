package net.createmod.ponder.foundation.registration;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import javax.annotation.Nullable;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.api.registration.SceneRegistryAccess;
import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class PonderSceneRegistry implements SceneRegistryAccess {

	private final PonderLocalization localization;
	private final Multimap<ResourceLocation, StoryBoardEntry> scenes;

	private boolean allowRegistration = true;

	public PonderSceneRegistry(PonderLocalization localization) {
		this.localization = localization;
		scenes = LinkedHashMultimap.create();
	}

	public void clearRegistry() {
		scenes.clear();
		allowRegistration = true;
	}

	//

	public void addStoryBoard(StoryBoardEntry entry) {
		if (!allowRegistration)
			throw new IllegalStateException("Registration Phase has already ended!");

		scenes.put(entry.getComponent(), entry);
	}

	//

	@Override
	public Collection<Map.Entry<ResourceLocation, StoryBoardEntry>> getRegisteredEntries() {
		return scenes.entries();
	}

	@Override
	public boolean doScenesExistForId(ResourceLocation id) {
		return scenes.containsKey(id);
	}

	//

	@Override
	public List<PonderScene> compile(ResourceLocation id) {
		if (PonderIndex.editingModeActive())
			PonderIndex.reload();

		Collection<StoryBoardEntry> entries = scenes.get(id);

		if (entries.isEmpty()) return Collections.emptyList();

		return compile(entries);

	}

	@Override
	public List<PonderScene> compile(Collection<StoryBoardEntry> entries) {
		if (PonderIndex.editingModeActive()) {
			localization.clearShared();
			PonderIndex.gatherSharedText();
		}

		List<PonderScene> scenes = new ArrayList<>();

		for (StoryBoardEntry storyBoard : entries) {
			StructureTemplate activeTemplate = loadSchematic(storyBoard.getSchematicLocation());
			PonderLevel level = new PonderLevel(BlockPos.ZERO, Minecraft.getInstance().level);
			activeTemplate.placeInWorld(level, BlockPos.ZERO, BlockPos.ZERO, new StructurePlaceSettings(), level.random,
										Block.UPDATE_CLIENTS);
			level.createBackup();
			PonderScene scene = compileScene(localization, storyBoard, level);
			scene.begin();
			scenes.add(scene);
		}

		return scenes;
	}

	public static PonderScene compileScene(PonderLocalization localization, StoryBoardEntry sb, @Nullable PonderLevel level) {
		PonderScene scene = new PonderScene(level, localization, sb.getNamespace(), sb.getComponent(), sb.getTags(),
											sb.getOrderingEntries());
		SceneBuilder builder = scene.builder();
		sb.getBoard().program(builder, scene.getSceneBuildingUtil());
		return scene;
	}

	public static StructureTemplate loadSchematic(ResourceLocation location) {
		return loadSchematic(Minecraft.getInstance().getResourceManager(), location);
	}

	public static StructureTemplate loadSchematic(ResourceManager resourceManager, ResourceLocation location) {
		String namespace = location.getNamespace();
		String path = "ponder/" + location.getPath() + ".nbt";
		ResourceLocation location1 = new ResourceLocation(namespace, path);

		Optional<Resource> optionalResource = resourceManager.getResource(location1);
		if (optionalResource.isEmpty()) {
			Ponder.LOGGER.error("Ponder schematic missing: " + location1);

			return new StructureTemplate();
		}

		Resource resource = optionalResource.get();
		try (InputStream inputStream = resource.open()) {
			return loadSchematic(inputStream);
		} catch (IOException e) {
			Ponder.LOGGER.error("Failed to read ponder schematic: " + location1, e);
		}

		return new StructureTemplate();
	}

	public static StructureTemplate loadSchematic(InputStream resourceStream) throws IOException {
		StructureTemplate t = new StructureTemplate();
		DataInputStream stream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(resourceStream)));
		CompoundTag nbt = NbtIo.read(stream, new NbtAccounter(0x20000000L));
		t.load(nbt);
		return t;
	}
}
