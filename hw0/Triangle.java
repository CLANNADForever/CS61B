public class Triangle {
    public static void main(String[] args) {
        drawTriangles(10);
    }
    public static void drawTriangles(int n) {
        int i = 1;
        while (i <= n) {
            int j = 1;
            while(j <= i - 1){
                System.out.print('*');
                j++;
            }
            System.out.println('*');
            i++;
        }
    }
}