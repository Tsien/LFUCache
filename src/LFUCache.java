import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;

/**
 * A simple thread-safe LFU cache
 */

/**
 * @author feichao
 *
 */
public class LFUCache<K, V> {
	private final Map<K, Pair<Integer, V>> cache;  // <key, <frequency, value>>
	private final LinkedHashSet<K>[] freqList;
	private int minFreq;  // the minimum frequency in the cache

	private final int capacity;
    private final int evict_num;
    
    /*
     * Update frequency of the key-value pair in the cache if the key exists.
     * Increase the frequency of this pair and move it to the next slots in the frequency list
     * If the frequency reaches the capacity, move it the end of current slot.
     */
	private synchronized void touch(K key) {
		if (cache.containsKey(key)) {
			int freq = cache.get(key).getKey();
			V val = cache.get(key).getValue();
			freqList[freq].remove(key);
			
			if (freq + 1 < capacity) {
				cache.put(key, new Pair<>(freq + 1, val));
				freqList[freq + 1].add(key);
				if (freq == minFreq && freqList[minFreq].isEmpty()) {
					++minFreq;
				}
			}
			else {
				// LRU: put the most recent visited to the end of set
				freqList[freq].add(key);				
			}
		}
	}
	
	/*
	 * Evict the least frequent elements in the cache
	 */
	private synchronized void evict() {
		for (int i = 0; i < evict_num && minFreq < capacity; ++i) {
			K key = (K) freqList[minFreq].iterator().next();
			freqList[minFreq].remove(key);
			cache.remove(key);
			while (minFreq < capacity && freqList[minFreq].isEmpty()) {
				++minFreq;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public LFUCache(int cap, double evictFactor) {
        if (cap <= 0 || evictFactor <= 0 || evictFactor >= 1) {
            throw new IllegalArgumentException("Eviction factor or Capacity is illegal.");
        }
		capacity = cap;
		evict_num = Math.min(cap, (int)Math.ceil(cap * evictFactor));
		minFreq = 0;
		
		cache = new HashMap<K, Pair<Integer, V>>();
		freqList = new LinkedHashSet[cap];
		for (int i = 0; i < cap; ++i) {
			freqList[i] = new LinkedHashSet<K>();
		}
	}
	
	public synchronized V get(K key) {
		if (!cache.containsKey(key)) {
			return null;
		}
		// update frequency
		touch(key);
		return cache.get(key).getValue();
	}
	
	public synchronized void set(K key, V value) {
		if (cache.containsKey(key)) {
			Integer freq = cache.get(key).getKey();
			cache.put(key, new Pair<>(freq, value));  // update value
			touch(key);  // update frequency
			return ;
		}
		if (cache.size() >= capacity) {
			evict();
		}
		minFreq = 0;
		cache.put(key, new Pair<>(minFreq, value));
		freqList[minFreq].add(key);
	}
	
	public synchronized List<Pair<K, V>> mget(List<K> keys) {
		List<Pair<K, V>> ret = new ArrayList<Pair<K, V>>();
		for (K key : keys) {
			ret.add(new Pair<>(key, get(key)));
		}
		return ret;
	}
	
	public synchronized void mset(List<Pair<K, V>> pairs) {
		for (Pair<K, V> pair : pairs) {
			set(pair.getKey(), pair.getValue());
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized Integer incr(K key, Integer delta) {
		Integer value = (Integer) get(key);
		value = value == null ? delta : value + delta;
        set(key, (V) value);
        return value;
	}
	
	public synchronized Integer decr(K key, Integer delta) {
		return incr(key, -delta);
	}

	public void print() {
		int f = minFreq;
		System.out.println("=========================");
		System.out.println("What is in cache?");
		while (f < capacity) {
			for (Object key : freqList[f]) {
				System.out.print("(" + key + ", " + cache.get(key).getValue() + " : " + cache.get(key).getKey() + "), ");
			}
			++f;
		}
		System.out.println("\n=========================");
	}	
}
