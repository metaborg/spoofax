package org.strategoxt.imp.runtime.util;

public interface IObserver<T> {
	public abstract void notify(T t);
}
