public class Calculator {
    int sum(int n) {
        if (n <= 0) return 0;
        return n + sum(n - 1);
    }

    int sum(int a, int b) {
        return a + b;
    }
}
