public class math {

    public static void main(String[] args) {
        int x, y;
        int sum = 0;

        for (x = 1; x <= 10; x++) {
            sum = 60 - 4 * x;
            for (y = 1; y <= 10; y++) {
                if (sum == (5 * y)) System.out.print("(" + x + "," + y + ")");

            }
        }


    }
} 
