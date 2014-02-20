package org.strategoxt.imp.runtime.util;

import java.util.Collection;
import java.util.LinkedList;

public class Observable<T> implements IObservable<T> {
	private Collection<IObserver<T>> observers = new LinkedList<>();

	@Override
	public void addObserver(IObserver<T> observer) {
		observers.add(observer);
	}

	@Override
	public void removeObserver(IObserver<T> observer) {
		observers.remove(observer);
	}

	public void notifyObservers(T t) {
		for(IObserver<T> observer : observers) {
			observer.notify(t);
		}
	}
}
