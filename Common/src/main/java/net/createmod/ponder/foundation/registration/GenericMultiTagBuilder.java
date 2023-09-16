package net.createmod.ponder.foundation.registration;

import net.createmod.ponder.api.registration.MultiTagBuilder;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class GenericMultiTagBuilder<T> implements MultiTagBuilder {

	private PonderTagRegistrationHelper<T> helper;

	public class Tag implements MultiTagBuilder.Tag<T> {

		Iterable<ResourceLocation> tags;

		public Tag(PonderTagRegistrationHelper<T> helper, Iterable<ResourceLocation> tags) {
			GenericMultiTagBuilder.this.helper = helper;
			this.tags = tags;
		}

		@Override
		public Tag add(T component) {
			tags.forEach(tag -> helper.addTagToComponent(component, tag));
			return this;
		}
	}

	public class Component implements MultiTagBuilder.Component {

		Iterable<T> components;

		public Component(PonderTagRegistrationHelper<T> helper, Iterable<T> components) {
			GenericMultiTagBuilder.this.helper = helper;
			this.components = components;
		}

		@Override
		public Component add(ResourceLocation tag) {
			components.forEach(component -> helper.addTagToComponent(component, tag));
			return this;
		}
	}

}
