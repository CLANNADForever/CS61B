package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;

    // You should probably define some more!
    private double maxLoad;
    private int itemsCount;
    private int bucketsCount;

    /** Constructors */
    public MyHashMap() {
        this.itemsCount = 0;
        this.bucketsCount = 16;
        this.maxLoad = 0.75;
        buckets = createTable(bucketsCount);
    }

    public MyHashMap(int initialSize) {
        this.itemsCount = 0;
        this.bucketsCount = initialSize;
        this.maxLoad = 0.75;
        buckets = createTable(bucketsCount);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this.itemsCount = 0;
        this.bucketsCount = initialSize;
        this.maxLoad = maxLoad;
        buckets = createTable(bucketsCount);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new ArrayList<Node>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] array = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            array[i] = createBucket();
        }
        return array;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    @Override
    public void clear() {
        for (int i = 0; i < bucketsCount; i++) {
            buckets[i] = createBucket();
        }
        itemsCount = 0;
    }

    private int calculateIndex(K key, int bucketsCount) {
        int hashCode = key.hashCode();
        return Math.floorMod(hashCode, bucketsCount);
    }

    private Node findNode(K key, Collection<Node> collection) {
        for (Node n : collection) {
            if (n.key.equals(key)) {
                return n;
            }
        }
        return null;
    }

    @Override
    public boolean containsKey(K key) {
        int index = calculateIndex(key, bucketsCount);
        Node n = findNode(key, buckets[index]);
        return n != null;
    }

    @Override
    public V get(K key) {
        int index = calculateIndex(key, bucketsCount);
        Node n = findNode(key, buckets[index]);
        if (n == null) return null;
        else return n.value;
    }

    @Override
    public int size() {
        return itemsCount;
    }

    @Override
    public void put(K key, V value) {

        int index = calculateIndex(key, bucketsCount);
        if (containsKey(key)) {
            Node n = findNode(key, buckets[index]);
            assert n != null;
            n.value = value;
            return;
        } else {
            Collection<Node> bucket = buckets[index];
            Node newNode = createNode(key, value);
            bucket.add(newNode);
            itemsCount++;
        }

        double loadFactor = (double) itemsCount / bucketsCount;
        if (loadFactor > maxLoad) {
            resize(2 * bucketsCount);
        }
    }

    private void resize(int newSize) {
        Collection<Node>[] newBuckets = createTable(newSize);
        for (K key : this) {
            V value = get(key);
            int newIndex = calculateIndex(key, newSize);
            Node newNode = createNode(key, value);
            newBuckets[newIndex].add(newNode);
        }
        buckets = newBuckets;
        bucketsCount = newSize;
    }

    @Override
    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        for (Collection<Node> bucket : buckets) {
            for (Node n : bucket) {
                set.add(n.key);
            }
        }
        return set;
    }

    @Override
    public V remove(K key) {
        int index = calculateIndex(key, bucketsCount);
        Collection<Node> bucket = buckets[index];
        Node n = findNode(key, bucket);
        if (n != null) {
            V value = n.value;
            bucket.remove(n);
            itemsCount--;
            return value;
        } else {
            return null;
        }
    }

    @Override
    public V remove(K key, V value) {
        int index = calculateIndex(key, bucketsCount);
        Collection<Node> bucket = buckets[index];
        Node n = findNode(key, bucket);
        if (n != null && n.value == value) {
            bucket.remove(n);
            itemsCount--;
            return value;
        } else {
            return null;
        }
    }

    @Override
    public Iterator<K> iterator() {
        Set<K> set = keySet();
        return set.iterator();
    }

}
