package net.createmod.ponder.foundation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public class PonderTagRegistry {

	private final Multimap<ResourceLocation, PonderTag> tags;
	private final Multimap<PonderChapter, PonderTag> chapterTags;

	private final List<PonderTag> listedTags;

	public PonderTagRegistry() {
		tags = LinkedHashMultimap.create();
		chapterTags = LinkedHashMultimap.create();
		listedTags = new ArrayList<>();
	}

	public Set<PonderTag> getTags(ResourceLocation item) {
		return ImmutableSet.copyOf(tags.get(item));
	}

	public Set<PonderTag> getTags(PonderChapter chapter) {
		return ImmutableSet.copyOf(chapterTags.get(chapter));
	}

	public Set<ResourceLocation> getItems(PonderTag tag) {
		return tags.entries()
			.stream()
			.filter(e -> e.getValue() == tag)
			.map(Map.Entry::getKey)
			.collect(ImmutableSet.toImmutableSet());
	}

	public Set<PonderChapter> getChapters(PonderTag tag) {
		return chapterTags.entries()
			.stream()
			.filter(e -> e.getValue() == tag)
			.map(Map.Entry::getKey)
			.collect(ImmutableSet.toImmutableSet());
	}

	public List<PonderTag> getListedTags() {
		return listedTags;
	}

	public void listTag(PonderTag tag) {
		listedTags.add(tag);
	}

	public void add(PonderTag tag, ResourceLocation item) {
		synchronized (tags) {
			tags.put(item, tag);
		}
	}

	public void add(PonderTag tag, PonderChapter chapter) {
		synchronized (chapterTags) {
			chapterTags.put(chapter, tag);
		}
	}

	public ItemBuilder forItems(ResourceLocation... items) {
		return new ItemBuilder(this, items);
	}

	public TagBuilder forTag(PonderTag tag) {
		return new TagBuilder(this, tag);
	}

	public static class ItemBuilder {
		private final PonderTagRegistry registry;
		private final Collection<ResourceLocation> items;

		protected ItemBuilder(PonderTagRegistry registry, ResourceLocation... items) {
			this.registry = registry;
			this.items = Arrays.asList(items);
		}

		protected ItemBuilder(PonderTagRegistry registry, Collection<ResourceLocation> items) {
			this.registry = registry;
			this.items = items;
		}

		public ItemBuilder addTag(PonderTag tag) {
			items.forEach(i -> registry.add(tag, i));
			return this;
		}

	}

	public static class TagBuilder {
		private final PonderTagRegistry registry;
		private final PonderTag tag;

		protected TagBuilder(PonderTagRegistry registry, PonderTag tag) {
			this.registry = registry;
			this.tag = tag;
		}

		public TagBuilder add(ResourceLocation item) {
			registry.add(tag, item);
			return this;
		}

		public TagBuilder add(ItemLike item) {
			return add(CatnipServices.REGISTRIES.getKeyOrThrow(item.asItem()));
		}
	}

}
