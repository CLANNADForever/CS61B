package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.*;

public class MaxDequeTest {
    private class compareByNum<T> implements Comparator<T> {
        @Override
        public int compare(Object o1, Object o2) {
            return (int) o1 - (int) o2;
        }

    }

    @Test
    public void testMax() {
        Comparator<Integer> c = new compareByNum<>();
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<>(c);
        for (int i = 0; i < 100; i++) {
            mad.addLast(i);
        }

        assertEquals(99, (int)mad.max());
    }

    @Test
    public void testEmptyMax() {
        Comparator<Integer> c = new compareByNum<>();
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<>(c);
        assertEquals(null, mad.max());
    }

    @Test
    public void testOnlyOneMax() {
        Comparator<Integer> c = new compareByNum<>();
        MaxArrayDeque<Integer> mad = new MaxArrayDeque<>(c);
        mad.addLast(999);
        assertEquals(999, (int) mad.max());
    }

}
