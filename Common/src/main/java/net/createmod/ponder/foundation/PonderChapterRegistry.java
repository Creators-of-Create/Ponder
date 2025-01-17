package net.createmod.ponder.foundation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.createmod.catnip.data.Pair;
import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.minecraft.resources.ResourceLocation;

public class PonderChapterRegistry {

	private final Map<ResourceLocation, Pair<PonderChapter, List<StoryBoardEntry>>> chapters;

	public PonderChapterRegistry() {
		chapters = new HashMap<>();
	}

	PonderChapter addChapter(@Nonnull PonderChapter chapter) {
		synchronized (chapters) {
			chapters.put(chapter.getId(), Pair.of(chapter, new ArrayList<>()));
		}
		return chapter;
	}

	@Nullable
	PonderChapter getChapter(ResourceLocation id) {
		Pair<PonderChapter, List<StoryBoardEntry>> pair = chapters.get(id);
		if (pair == null)
			return null;

		return pair.getFirst();
	}

	public void addStoriesToChapter(@Nonnull PonderChapter chapter, StoryBoardEntry... entries) {
		List<StoryBoardEntry> entryList = chapters.get(chapter.getId()).getSecond();
		synchronized (entryList) {
			Collections.addAll(entryList, entries);
		}
	}

	public List<PonderChapter> getAllChapters() {
		return chapters
				.values()
				.stream()
				.map(Pair::getFirst)
				.collect(Collectors.toList());
	}

	public List<StoryBoardEntry> getStories(PonderChapter chapter) {
		Pair<PonderChapter, List<StoryBoardEntry>> chapterPair = chapters.get(chapter.getId());
		if (chapterPair == null)
			return List.of();
		return chapterPair.getSecond();
	}

}
