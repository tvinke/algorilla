import java.util.*;

class MemoizedFib {
    Map<Integer, Integer> cache = new HashMap<>();

    int fib(int n) {
        if (n <= 1) return n;
        return cache.computeIfAbsent(n, k -> fib(k - 1) + fib(k - 2));
    }
}
