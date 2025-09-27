package deque;

import java.lang.reflect.Array;

public class ArrayDeque<T> {
    int size;
    int startIndex; // 将要作为首位插入点的索引
    int endIndex; // 将要作为末尾插入点的索引
    private T[] array;

    /* Creates an empty linked list deque. */
    public ArrayDeque() {
        size = 0;
        array = (T[]) new Object[8];
        startIndex = array.length - 1;
        endIndex = 0;
    }

    /* Adds an item of type T to the front of the deque in constant time, except during resizing operations. */
    public void addFirst(T item) {
        array[startIndex] = item;
        // 更新索引，为下次操作做准备
        startIndex -= 1; // 所有index的操作同理，都可能越界，都应循环到另一头，之后使用三目运算，此处作示例。
        if (startIndex == -1) {
            startIndex = array.length - 1;
        }
        size += 1;
    }

    /* Adds an item of type T to the back of the deque in constant time, except during resizing operations. */
    public void addLast(T item) {
        array[endIndex] = item;
        endIndex = endIndex + 1 == array.length ? 0 : endIndex + 1;
        size += 1;
    }

    /* Returns true if deque is empty, false otherwise. */
    public boolean isEmpty() {
        return size == 0;
    }

    /* Returns the number of items in the deque. */
    public int size() {
        return size;
    }

    /* Prints the items in the deque from first to last, separated by a space.
    Once all the items have been printed, print out a new line.
     */
    public void printDeque() {
        // 实际有元素的索引
        int itemStartIndex = startIndex + 1 == array.length ? 0 : startIndex + 1;
        int itemEndIndex = endIndex - 1 == -1 ? array.length - 1 : endIndex - 1;

        if (itemStartIndex > itemEndIndex) {
            for (int i = itemStartIndex; i < array.length; i++) {
                System.out.print(array[i].toString() + " ");
            }
            for (int i = 0; i <= itemEndIndex; i++) {
                System.out.print(array[i].toString() + " ");
            }
        } else if (itemStartIndex < itemEndIndex) {
            for (int i = itemStartIndex; i <= itemEndIndex; i++) {
                System.out.print(array[i].toString() + " ");
            }
        } else {
            System.out.print(array[startIndex].toString() + " ");
        }

        System.out.println();
    }

    /* Removes and returns the item at the front of the deque in constant time, except during resizing operations.
    If no such item exists, returns null.
     */
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        int itemStartIndex = startIndex + 1 == array.length ? 0 : startIndex + 1;
        T removedItem = array[itemStartIndex];
        startIndex = startIndex + 1 == array.length ? 0 : startIndex + 1;
        size -= 1;
        return removedItem;
    }

    /* Removes and returns the item at the back of the deque in constant time, except during resizing operations.
    If no such item exists, returns null.
     */
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        int itemEndIndex = endIndex - 1 == -1 ? array.length - 1 : endIndex - 1;
        T removeItem = array[itemEndIndex];
        endIndex = endIndex - 1 == -1 ? array.length - 1 : endIndex - 1;
        size -= 1;
        return removeItem;
    }

    /* Gets the item at the given index, where 0 is the front, 1 is the next item, and so forth.
    If no such item exists, returns null.
     */
    public T get(int index) {
        if (index > size() - 1) {
            return null;
        }
        int idx = startIndex + 1 == array.length ? 0 : startIndex + 1;
        for (int t = 0; t < index; t++, idx = idx + 1 == array.length ? 0 : idx + 1);
        return array[idx];
    }

}
