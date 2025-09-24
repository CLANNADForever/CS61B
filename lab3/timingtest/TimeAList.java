package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeAList {
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
        timeAListConstruction();
    }

    public static void timeAListConstruction() {
        // TODO: YOUR CODE HERE
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();

        for (int i = 0; i < 8; i++) {
            int curLen = 1000 * (int)Math.pow(2, i);
            AList<Integer> testAList = new AList<>();

            // 插入对应个数元素并计时
            Stopwatch sw = new Stopwatch();
            for (int j = 0; j < curLen; j++) {
                testAList.addLast(1);
            }
            double timeInSecond = sw.elapsedTime();

            // 存储相应结果
            Ns.addLast(curLen);
            times.addLast(timeInSecond);
            opCounts.addLast(curLen);
        }

        printTimingTable(Ns, times, opCounts);
    }
}
