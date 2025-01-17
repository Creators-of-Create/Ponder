package net.createmod.catnip.placement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlacementHelpers {
	private static final List<IPlacementHelper> HELPERS = new ArrayList<>();

	private static final List<IPlacementHelper> HELPERS_VIEW = Collections.unmodifiableList(HELPERS);

	public static int register(IPlacementHelper helper) {
		HELPERS.add(helper);
		return HELPERS.size() - 1;
	}

	public static IPlacementHelper get(int id) {
		if (id < 0 || id >= HELPERS.size())
			throw new ArrayIndexOutOfBoundsException("id " + id + " for placement helper not known");

		return HELPERS.get(id);
	}

	public static List<IPlacementHelper> getHelpersView() {
		return HELPERS_VIEW;
	}
}
