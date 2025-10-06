package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T>{
    private Comparator<T> cmp;
    /* creates a MaxArrayDeque with the given Comparator. */
    public MaxArrayDeque(Comparator<T> c) {
        cmp = c;
    }

    /* Returns the maximum element in the deque as governed by the previously given Comparator. */
    public T max() {
        T maxItem = get(0);
        for (T item : this) {
            if (cmp.compare(item, maxItem) > 0) {
                maxItem = item;
            }
        }
        return maxItem;
    }

    /* Returns the maximum element in the deque as governed by the parameter Comparator c. */
    public T max(Comparator<T> c) {
        if (size() == 0) {
            return null;
        } else if (size() == 1) {
            return get(0);
        }

        T maxItem = get(0);
        for (T item : this) {
            if (c.compare(item, maxItem) > 0) {
                maxItem = item;
            }
        }
        return maxItem;
    }
}
