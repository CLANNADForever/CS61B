package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove(){
        BuggyAList<Integer> buggyOne = new BuggyAList<>();
        AListNoResizing<Integer> correctOne = new AListNoResizing<>();

        for (int i = 0; i < 3; i++) {
            buggyOne.addLast(i);
            correctOne.addLast(i);
        }

        assertTrue(buggyOne.removeLast() == correctOne.removeLast());
        assertTrue(buggyOne.removeLast() == correctOne.removeLast());
        assertTrue(buggyOne.removeLast() == correctOne.removeLast());
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> buggyL = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                buggyL.addLast(randVal);
//                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                int buggySize = buggyL.size();
//                System.out.println("size: " + size);
                assertTrue(size == buggySize);
            } else if (operationNumber == 2) {
                // getLast
                if (L.size() == 0) {
                    continue;
                }
                int l = L.getLast();
                int buggyl = buggyL.getLast();
//                System.out.println("getLast() equals to " + l);
                assertTrue(l == buggyl);
            } else if (operationNumber == 3) {
                // removeLast
                if (L.size() == 0) {
                    continue;
                }
                int l = L.removeLast();
                int buggyl = buggyL.removeLast();
//                System.out.println("removeLast() equals to " + l);
                assertTrue(l == buggyl);
            }
        }
    }
}
