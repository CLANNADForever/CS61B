package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V>{
    private BSTNode root;

    private class BSTNode {
        private int size;
        private BSTNode left;
        private BSTNode right;
        private K key;
        private V val;

        public BSTNode(K key, V val) {
            this.key = key;
            this.val = val;
            left = null;
            right = null;
            size = 1;
        }

        private boolean isLeaf() {
            return left == null && right == null;
        }

        /** 将新映射的节点添加到节点处，或是改变节点的映射 */
        private void addNode(K k, V v) {
            int cmp = k.compareTo(key);
            if (cmp == 0) { // 相等则直接改变映射
                val = v;
            } else if (cmp > 0) {
                if (right == null) {
                    right = new BSTNode(k, v);
                } else {
                    right.addNode(k, v);
                }
                size++;
            } else {
                if (left == null) {
                    left = new BSTNode(k, v);
                } else {
                    left.addNode(k, v);
                }
                size++;
            }
        }
    }

    public BSTMap() {
        root = null;
    }

    @Override
    public void clear() {
        root = null;
    }

    @Override
    public boolean containsKey(K key) {
        return get(key, root) != null;
    }

//    private boolean containsKey(K key, BSTNode node) {
//        if (node == null) {
//            return false;
//        }
//        if (node.key == key) {
//            return true;
//        }
//        return containsKey(key, node.left) || containsKey(key, node.right);
//    }

    @Override
    public V get(K key) {
        return get(key, root);
    }

    private V get(K key, BSTNode node) {
        if (node == null) {
            return null;
        }

        int cmp = key.compareTo(node.key);
        if (cmp == 0) {
            return node.val;
        } else if (cmp > 0) {
            return get(key, node.right);
        } else {
            return get(key, node.left);
        }
    }

    @Override
    public int size() {
        return size(root);
    }

    private int size(BSTNode node) {
        if (node == null) {
            return 0;
        } else {
            return node.size;
        }
    }

    @Override
    public void put(K key, V value) {
        if (size() == 0) {
            root = new BSTNode(key, value);
        } else {
            root.addNode(key, value);
        }
    }



    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException("Unsupported method.");
    }

    public void printInOrder() {

    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException("Unsupported method.");
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException("Unsupported method.");
    }

    public Iterator<K> iterator() {
        throw new UnsupportedOperationException("Unsupported method.");
    }
}
