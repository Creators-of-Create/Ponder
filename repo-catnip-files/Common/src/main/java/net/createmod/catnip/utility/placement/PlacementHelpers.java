package net.createmod.catnip.utility.placement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class PlacementHelpers {

	private static final List<IPlacementHelper> helpers = new ArrayList<>();

	public static int register(IPlacementHelper helper) {
		helpers.add(helper);
		return helpers.size() - 1;
	}

	public static IPlacementHelper get(int id) {
		if (id < 0 || id >= helpers.size())
			throw new ArrayIndexOutOfBoundsException("id " + id + " for placement helper not known");

		return helpers.get(id);
	}

	public static Stream<IPlacementHelper> streamHelpers() {
		return helpers.stream();
	}

}
