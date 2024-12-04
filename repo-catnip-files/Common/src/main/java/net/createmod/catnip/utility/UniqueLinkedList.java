package net.createmod.catnip.utility;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class UniqueLinkedList<E> extends LinkedList<E> {
	@Serial
	private static final long serialVersionUID = 1L;
	private final HashSet<E> contained = new HashSet<>();

	@Override
	public boolean contains(Object o) {
		return contained.contains(o);
	}

	@Override
	public E poll() {
		E e = super.poll();
		contained.remove(e);
		return e;
	}

	@Override
	public boolean add(E e) {
		if (contained.add(e))
			return super.add(e);
		else
			return false;
	}

	@Override
	public void add(int index, E element) {
		if (contained.add(element))
			super.add(index, element);
	}

	@Override
	public void addFirst(E e) {
		if (contained.add(e))
			super.addFirst(e);
	}

	@Override
	public void addLast(E e) {
		if (contained.add(e))
			super.addLast(e);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		List<E> filtered = new ArrayList<>();
		for (E i : c)
			if (!contained.contains(i))
				filtered.add(i);
		return super.addAll(filtered);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		List<E> filtered = new ArrayList<>();
		for (E i : c)
			if (!contained.contains(i))
				filtered.add(i);
		return super.addAll(index, filtered);
	}

	@Override
	public boolean remove(Object o) {
		contained.remove(o);
		return super.remove(o);
	}

	@Override
	public E remove(int index) {
		E e = super.remove(index);
		contained.remove(e);
		return e;
	}

	@Override
	public E removeFirst() {
		E e = super.removeFirst();
		contained.remove(e);
		return e;
	}

	@Override
	public E removeLast() {
		E e = super.removeLast();
		contained.remove(e);
		return e;
	}

	@Override
	public void clear() {
		super.clear();
		contained.clear();
	}
}
