package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T>{
    private int size;
    private int startIndex; // 将要作为首位插入点的索引
    private int endIndex; // 将要作为末尾插入点的索引
    private T[] array;


    /* Creates an empty linked list deque. */
    public ArrayDeque() {
        size = 0;
        array = (T[]) new Object[8];
        startIndex = array.length - 1;
        endIndex = 0;
    }

    @Override
    /* Adds an item of type T to the front of the deque in constant time,
    except during resizing operations. */
    public void addFirst(T item) {
        if (size == array.length) {
            resize(2 * size);
            startIndex = array.length - 1;
            endIndex = size;
        }

        array[startIndex] = item;
        // 更新索引，为下次操作做准备
        startIndex -= 1; // 所有index的操作同理，都可能越界，都应循环到另一头，之后使用三目运算，此处作示例。
        if (startIndex == -1) {
            startIndex = array.length - 1;
        }
        size += 1;
    }

    @Override
    /* Adds an item of type T to the back of the deque in constant time, except during resizing operations. */
    public void addLast(T item) {
        if (size == array.length) {
            resize(2 * size);
            startIndex = array.length - 1;
            endIndex = size;
        }

        array[endIndex] = item;
        endIndex = endIndex + 1 == array.length ? 0 : endIndex + 1;
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

    @Override
    /* Removes and returns the item at the front of the deque in constant time, except during resizing operations.
    If no such item exists, returns null.
     */
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }

        if (size >= 16 && size <= array.length / 4) {
            resize(array.length / 4);
            startIndex = array.length - 1; // 然后itemStartIndex为0，这是符合定义的，因为resize之后第一个元素在索引0
            endIndex = 0; // 与startIndex对称，同理
        }

        int itemStartIndex = startIndex + 1 == array.length ? 0 : startIndex + 1;
        T removedItem = array[itemStartIndex];
        startIndex = startIndex + 1 == array.length ? 0 : startIndex + 1;
        size -= 1;

        return removedItem;
    }

    @Override
    /* Removes and returns the item at the back of the deque in constant time, except during resizing operations.
    If no such item exists, returns null.
     */
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }

        if (size >= 16 && size <= array.length / 4) {
            resize(array.length / 4);
            startIndex = array.length - 1; // 然后itemStartIndex为0，这是符合定义的，因为resize之后第一个元素在索引0
            endIndex = 0; // 与startIndex对称，同理
        }

        int itemEndIndex = endIndex - 1 == -1 ? array.length - 1 : endIndex - 1;
        T removeItem = array[itemEndIndex];
        endIndex = endIndex - 1 == -1 ? array.length - 1 : endIndex - 1;
        size -= 1;
        return removeItem;
    }

    @Override
    /* Gets the item at the given index, where 0 is the front, 1 is the next item, and so forth.
    If no such item exists, returns null.
     */
    public T get(int index) {
        if (index > size - 1) {
            return null;
        }
        int idx = startIndex + 1 == array.length ? 0 : startIndex + 1;
        for (int t = 0; t < index; t++) {
            idx = idx + 1 == array.length ? 0 : idx + 1;
        }
        return array[idx];
    }

    /* 调整实际数组大小。规定新数组所有元素均从前面开始，即0到size - 1. */
    private void resize(int newSize) {
        T[] newArray = (T[]) new Object[newSize];
        // 实际有元素的索引
        int itemStartIndex = startIndex + 1 == array.length ? 0 : startIndex + 1;
        int itemEndIndex = endIndex - 1 == -1 ? array.length - 1 : endIndex - 1;

        if (itemStartIndex < itemEndIndex) {
            System.arraycopy(array, itemStartIndex, newArray, 0, size);
        } else {
            System.arraycopy(array, itemStartIndex, newArray, 0, array.length - itemStartIndex);
            System.arraycopy(array, 0, newArray, array.length - itemStartIndex, itemEndIndex + 1);
        }
        array = newArray;
    }

    private class ArrayDequeIterator implements Iterator<T> {
        public int curPos;
        public ArrayDequeIterator() {
            curPos = 0;
        }

        @Override
        public boolean hasNext() {
            return curPos <= size() - 1;
        }

        @Override
        public T next() {
            T returnItem = get(curPos);
            curPos++;
            return returnItem;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Deque) {
            Deque<T> otherDeque = (Deque<T>) o;
            if (size() != otherDeque.size()) {
                return false;
            }

            for (int i = 0; i < size; i++) {
                if (get(i) != otherDeque.get(i)) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }
}
