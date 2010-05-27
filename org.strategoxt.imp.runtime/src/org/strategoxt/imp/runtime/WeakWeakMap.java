package org.strategoxt.imp.runtime;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.spoofax.NotImplementedException;
import org.strategoxt.lang.WeakValueHashMap;

/**
 * A WeakHashMap that uses weak reference keys and soft reference values.
 * 
 * @see WeakHashMap
 * @see WeakValueHashMap
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class WeakWeakMap<K,V> implements Map<K,V> {
	
	private final WeakHashMap<K, WeakReference<V>> map = new WeakHashMap<K, WeakReference<V>>();
	
	public WeakWeakMap() {
		// Construct new instance
	}

	public void clear() {
		map.clear();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		// Can't do this using reference equality and no reference given
		throw new UnsupportedOperationException();
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		throw new NotImplementedException();
	}

	public V get(Object key) {
		WeakReference<V> ref = map.get(key);
		return ref == null ? null : ref.get();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public Set<K> keySet() {
		return map.keySet();
	}

	public V put(K key, V value) {
		if (value == null)
			throw new IllegalArgumentException("Value cannot be null");
		WeakReference<V> existing = map.put(key, new WeakReference<V>(value));
		return existing == null ? null : existing.get();
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		throw new NotImplementedException();
	}

	public V remove(Object key) {
		WeakReference<V> existing = map.remove(key);
		return existing == null ? null : existing.get();
	}

	public int size() {
		return map.size();
	}

	public Collection<V> values() {
		Collection<WeakReference<V>> results = map.values();
		Set<V> copy = new HashSet<V>(results.size());
		for (WeakReference<V> resultRef : results) {
			V result = resultRef == null ? null : resultRef.get();
			if (result != null) copy.add(result);
		}
		return copy;
	}
}
