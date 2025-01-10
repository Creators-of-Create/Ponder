package net.createmod.catnip.utility.layout;

import java.util.Objects;
import java.util.function.BiConsumer;

public final class PaginationState {
	private final boolean usesPagination;
	private int pageIndex;
	private final int elementsPerPage;
	private final int elementCount;

	public PaginationState() {
		this(false, 1, 1);
	}

	public PaginationState(boolean usesPagination, int elementsPerPage, int elementCount) {
		this(usesPagination, 0, elementsPerPage, elementCount);
	}

	public PaginationState(boolean usesPagination, int pageIndex, int elementsPerPage, int elementCount) {
		this.usesPagination = usesPagination;
		this.pageIndex = pageIndex;
		this.elementsPerPage = elementsPerPage;
		this.elementCount = elementCount;
	}

	public boolean usesPagination() {
		return usesPagination;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public int getMaxPages() {
		if (!usesPagination)
			return 1;

		return (int) Math.ceil((double) elementCount / elementsPerPage);
	}

	public int getElementsPerPage() {
		return elementsPerPage;
	}

	public int getElementCount() {
		return elementCount;
	}

	public int getStartIndex() {
		return pageIndex * elementsPerPage;
	}

	public int getCurrentPageElementCount() {
		if (!usesPagination)
			return elementCount;

		return Math.min(elementsPerPage, elementCount - (pageIndex * elementsPerPage));
	}

	/**
	 * @param consumer gets called once for each element of the current page.
	 *                 gets passed the index of the element in terms of the current page, as well as the overall element list
	 */
	public void iterateForCurrentPage(BiConsumer<Integer, Integer> consumer) {
		for (int i = 0; i < getCurrentPageElementCount(); i++) {
			consumer.accept(i, i + getStartIndex());
		}
	}

	public boolean hasPreviousPage() {
		if (!usesPagination)
			return false;

		return pageIndex > 0;
	}

	public boolean hasNextPage() {
		if (!usesPagination)
			return false;

		return (pageIndex + 1) * elementsPerPage < elementCount;
	}

	public void nextPage() {
		if (hasNextPage())
			pageIndex++;
	}

	public void previousPage() {
		if (hasPreviousPage())
			pageIndex--;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (PaginationState) obj;
		return this.usesPagination == that.usesPagination &&
				this.pageIndex == that.pageIndex &&
				this.elementsPerPage == that.elementsPerPage &&
				this.elementCount == that.elementCount;
	}

	@Override
	public int hashCode() {
		return Objects.hash(usesPagination, pageIndex, elementsPerPage, elementCount);
	}

	@Override
	public String toString() {
		return "PaginationState[" +
				"usesPagination=" + usesPagination + ", " +
				"pageIndex=" + pageIndex + ", " +
				"elementsPerPage=" + elementsPerPage + ", " +
				"elementCount=" + elementCount + ']';
	}


}
