package timingtest;
import edu.princeton.cs.algs4.Stopwatch;
import org.checkerframework.checker.units.qual.A;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();

        for (int i = 0; i < 8; i++) {
            int curLen = 1000 * (int)Math.pow(2, i);
            // 先构建好SLList
            SLList<Integer> testSLList = new SLList<>();
            for (int j = 0; j < curLen; j++) {
                testSLList.addLast(1);
            }

            // 取固定次数元素并计时
            Stopwatch sw = new Stopwatch();
            int M = 10000;
            for (int j = 0; j < M; j++) {
                testSLList.getLast();
            }
            double timeInSecond = sw.elapsedTime();

            // 存储相应结果
            Ns.addLast(curLen);
            times.addLast(timeInSecond);
            opCounts.addLast(M);
        }

        printTimingTable(Ns, times, opCounts);
    }

}
