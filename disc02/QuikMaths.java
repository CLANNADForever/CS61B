import java.util.Arrays;

public class QuikMaths {
    public static void multiplyBy3(int[] A) {
        for (int x: A) {
            x = x * 3;
        }
    }
    public static void multiplyBy2(int[] A) {
        int[] B = A;
        for (int i = 0; i < B.length; i+= 1) {
            B[i] *= 2;
        }
    }
    public static void swap(int A, int B ) {
        int temp = B;
        B = A;
        A = temp;
    }
    public static void main(String[] args) {
        int[] arr;
        arr = new int[]{2, 3, 3, 4};
        multiplyBy3(arr);
        /* Value of arr: {________________________} */
        System.out.println(Arrays.toString(arr));
        arr = new int[]{2, 3, 3, 4};
        multiplyBy2(arr);
        /* Value of arr: {________________________} */
        System.out.println(Arrays.toString(arr));
        int a = 6;
        int b = 7;
        swap(a, b);
        /* Value of a: _______ Value of b: _______ */
        System.out.printf("a: %d, b: %d\n", a, b);
    }
}
