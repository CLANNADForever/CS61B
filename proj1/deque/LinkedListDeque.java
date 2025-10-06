package deque;

import org.apache.commons.collections.FastArrayList;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T>{

    /* A nested class for the list */
    private class Node {
        public T item; // generic type of hosted class can be directly used
        public Node last;
        public Node next;
        private Node(T i, Node l, Node n) {
            item = i;
            last = l;
            next = n;
        }
    }

    private int size;
    private Node sentinel;

    /* Creates an empty linked list deque. */
    public LinkedListDeque() {
        size = 0;
        sentinel = new Node(null, null, null);
        sentinel.next = sentinel;
        sentinel.last = sentinel;
    }

    @Override
    /* Adds an item of type T to the front of the deque in constant time. */
    public void addFirst(T item) {
        Node node = new Node(item, sentinel, sentinel.next);
        sentinel.next.last = node;
        sentinel.next = node;
        size += 1;
    }

    @Override
    /* Adds an item of type T to the back of the deque in constant time.*/
    public void addLast(T item) {
        Node node = new Node(item, sentinel.last, sentinel);
        sentinel.last.next = node;
        sentinel.last = node;
        size += 1;
    }

    @Override
    /* Returns the number of items in the deque. */
    public int size() {
        return size;
    }

    @Override
    /* Prints the items in the deque from first to last, separated by a space.
    Once all the items have been printed, print out a new line.
     */
    public void printDeque() {
        Node curNode = sentinel.next;
        while (curNode != sentinel) {
            System.out.print(curNode.item.toString() + " ");
            curNode = curNode.next;
        }
        System.out.println();
    }

    @Override
    /* Removes and returns the item at the front of the deque in constant time.
    If no such item exists, returns null.
     */
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        T removedItem = sentinel.next.item;
        sentinel.next = sentinel.next.next;
        sentinel.next.last = sentinel;
        size -= 1;
        return removedItem;
    }

    @Override
    /* Removes and returns the item at the back of the deque in constant time.
    If no such item exists, returns null.
     */
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        T removeItem = sentinel.last.item;
        sentinel.last = sentinel.last.last;
        sentinel.last.next = sentinel;
        size -= 1;
        return removeItem;
    }

    @Override
    /* Gets the item at the given index, where 0 is the front, 1 is the next item, and so forth.
    If no such item exists, returns null.
    Must use iteration.
     */
    public T get(int index) {
        if (index > size() - 1) {
            return null;
        }
        Node curNode = sentinel.next;
        for (int i = 0; i < index; i++) {
            curNode = curNode.next;
        }
        return curNode.item;
    }

    /* Same as get, but uses recursion. */
    public T getRecursive(int index) {
        if (index > size() - 1) {
            return null;
        }
        return getRecursive(sentinel.next, index, 0);
    }

    /* The underlying implementation of getRecursive method. */
    private T getRecursive(Node node, int index, int curIndex) {
        if (node == sentinel) {
            return null;
        }
        if (curIndex == index) {
            return node.item;
        }
        return getRecursive(node.next, index, curIndex + 1);
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        public Node curPos;
        public LinkedListDequeIterator() {
            curPos = sentinel.next;
        }

        @Override
        public boolean hasNext() {
            return curPos != sentinel;
        }

        @Override
        public T next() {
            T returnItem = curPos.item;
            curPos = curPos.next;
            return returnItem;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Deque) {
            Deque<T> otherDeque = (Deque<T>) o;
            if (size() != otherDeque.size()) {
                return false;
            }

            for (int i = 0; i < size; i++) {
                if (!get(i).equals(otherDeque.get(i))) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }
}
