package org.strategoxt.imp.runtime.util;

public interface IObservable<T> {
	public abstract void addObserver(IObserver<T> observer);
	public abstract void removeObserver(IObserver<T> observer);
}
