# Thread-Safe LFU Cache in Java

A simple thread-safe LFU cache supporting  *amortized* `O(1)` SET, GET, INCR, DECR, and linear time MSET, MGET.

## Cache eviction policy

The critical observation to make is that the user of the cache does not care what the current LFU ordering is. The only concern of the caller is that the cache maintains a threshold size and a high hit rate. The main goal of this LFU cache is to get a high hit ratio. This implementation is based on LFU with some small changes: 
  
    * When there is a tie in terms of frequency, the least recent used pair will be evicted first.
    * Since the user does not really care the order of current LFU, set the upper bound of frequency to the capacity of the cache in order to accommodate shifts in the set of popular objects. So objects were frequently accessed in the past are still eligible for replacement. 
    * Also, the user can configure how many objects(eviction factor) they want to evict when reaching the capacity of the cache. 

## API

### `LFUCache<KeyType, ValueType>(int capacity, float evictionFactor)`

Create a new LFU cache.

* `capacity` : the size of the cache.
* `evictionFactor`: the percentage of elements in the cache for replacement when reaching capacity. For example, `capacity = 10` and `evictionFactor = 0.2`, then two elements will be evicted when reaching capacity.
* Return: the newly created LFU cache

### SET: `set(Key key, Value value)`

Set key to hold the value. If key already holds a value, it is overwritten.

### GET: `get(Key key)`

Get the value of key. If the key does not exist, return `null`. 

### MGET: `mget(List<Key> keys)`

Returns the values of all specified keys. For every key that does not exist, `null` is returned. 

### MSET: `mset(List<Pair<Key, Value>> pairs)`

Sets the given keys to their respective values. MSET replaces existing values with new values, just as regular SET. 

### INCR: `incr(Key key, Integer delta)`

Increments the value stored at `key` by `delta`. If the `key` does not exist, it is set to 0 before performing the operation.

Notes:
    * only works for integer value
    * this function will increase the frequency of the element by 2

### DECR: `decr(Key key, Integer delta)`

Decrements the value stored at `key` by `delta`. If the `key` does not exist, it is set to 0 before performing the operation.

Notes:
    * only works for integer value
    * this function will increase the frequency of the element by 2


## Limitations

* The throughput will be small under high concurrent situation. 
    * Possible solution: Using Shard(like `ConcurrentHashMap` in java) to improve throughput under high concurrent situation. 

## Detailed Implementation

* Two main Data Structure:
    * a hashmap: `Map<Key, Pair<Integer, Value>> cache`
        * mapping `key` to a pair; 
        * the key of the pair is the frequency of the `key`, the value of the pair is the value of the `key`
    * a fixed-size array of linkedhashset: `LinkedHashSet[] freqList`
        * the size is the capacity of the cache.
        * each entry in the array is a linkedhashset.
        * `freqList[i]` stores elements whoes frequency is `i`.

## Usage

```java
// create a cache with size of 10
// 10 * 0.2 = 2 elements will be evicted when reading capacity
LFUCache<Integer, Integer> cache = new LFUCache<Integer, Integer>(10, 0.2);

```

## Play around testing

Run `java LFUCacheTest` under `LFUCache/bin` directory. Enjoy!

## Reference

* [An O(1) algorithm for implementing the LFU cache eviction scheme](http://dhruvbird.com/lfu.pdf)
* [How to implement a Least Frequently Used (LFU) cache?
](https://stackoverflow.com/questions/21117636/how-to-implement-a-least-frequently-used-lfu-cache)
* [Design a Cache System](http://blog.gainlo.co/index.php/2016/05/17/design-a-cache-system/)
* [Writing a very fast cache service with millions of entries in Go](https://allegro.tech/2016/03/writing-fast-cache-service-in-go.html#eviction)
* [The Evolution of Advanced Caching in the Facebook CDN](https://research.fb.com/the-evolution-of-advanced-caching-in-the-facebook-cdn/)
