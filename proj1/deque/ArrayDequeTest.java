package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArrayDequeTest {
    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {

        ArrayDeque<String> lld1 = new ArrayDeque<String>();

        assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
        lld1.addFirst("front");

        // The && operator is the same as "and" in Python.
        // It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());

        lld1.addLast("middle");
        assertEquals(2, lld1.size());

        lld1.addLast("back");
        assertEquals(3, lld1.size());

        System.out.println("Printing out deque: ");
        lld1.printDeque();

    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();
        // should be empty
        assertTrue("lld1 should be empty upon initialization", lld1.isEmpty());

        lld1.addFirst(10);
        // should not be empty
        assertFalse("lld1 should contain 1 item", lld1.isEmpty());

        lld1.removeFirst();
        // should be empty
        assertTrue("lld1 should be empty after removal", lld1.isEmpty());

    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        lld1.addFirst(3);

        lld1.removeLast();
        lld1.removeFirst();
        lld1.removeLast();
        lld1.removeFirst();

        int size = lld1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);
    }

    @Test
    /* Check if you can create ArrayDeques with different parameterized types */
    public void multipleParamTest() {
        ArrayDeque<String>  lld1 = new ArrayDeque<String>();
        ArrayDeque<Double>  lld2 = new ArrayDeque<Double>();
        ArrayDeque<Boolean> lld3 = new ArrayDeque<Boolean>();

        lld1.addFirst("string");
        lld2.addFirst(3.14159);
        lld3.addFirst(true);

        String s = lld1.removeFirst();
        double d = lld2.removeFirst();
        boolean b = lld3.removeFirst();
    }

    @Test
    /* check if null is return when removing from an empty ArrayDeque. */
    public void emptyNullReturnTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals("Should return null when removeFirst is called on an empty Deque,", null, lld1.removeFirst());
        assertEquals("Should return null when removeLast is called on an empty Deque,", null, lld1.removeLast());
    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        for (int i = 0; i < 1000000; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }

        for (double i = 999999; i > 500000; i--) {
            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
        }
    }

    @Test
    /* 随机测试.由于链表版本更为可靠，所以以它为参照，来测试数组版本. */
    public void randomizedTest() {
        LinkedListDeque<Integer> LLD = new LinkedListDeque<>();
        ArrayDeque<Integer> AD = new ArrayDeque<>();

        int N = 500000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 6);
            
            if (operationNumber == 0) { 
                // test addFirst
                int randVal = StdRandom.uniform(0, 100);
                LLD.addFirst(randVal);
                AD.addFirst(randVal);
                System.out.println("addFirst(" + randVal + ")");
            } else if (operationNumber == 1) { 
                // test addLast
                int randVal = StdRandom.uniform(0, 100);
                LLD.addLast(randVal);
                AD.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 2) {
                // test size
                int LLDSize = LLD.size();
                int ADSize = AD.size();
                System.out.println("size: " + LLDSize);
                assertTrue(LLDSize == ADSize);
            } else if (operationNumber == 3) { 
                // test removeFirst
                if (LLD.isEmpty()) {
                    continue;
                }

                int item1 = LLD.removeFirst();
                int item2 = AD.removeFirst();
                System.out.println("removeFirst() equals to " + item1);
                assertTrue(item1 == item2);
            } else if (operationNumber == 4) { 
                // test removeLast
                if (LLD.isEmpty()) {
                    continue;
                }

                int item1 = LLD.removeLast();
                int item2 = AD.removeLast();
                System.out.println("removeLast() equals to " + item1);
                assertTrue(item1 == item2);
            } else if (operationNumber == 5) { // test get
                int size = LLD.size();
                if (size == 0) {
                    continue;
                }
                int randVal = StdRandom.uniform(0, size);
                int item1 = LLD.get(randVal);
                int item2 = AD.get(randVal);

                System.out.printf("get(%d) equals to %d", randVal, item1);
                assertTrue(item1 == item2);
            }
        }
    }
}
