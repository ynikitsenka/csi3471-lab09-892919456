package edu.baylor.csi3471.setup;

public class Calculator {
    // Type a comment prompt and press Enter:
    // compute factorial of an integer n with validation
    public static int factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Input must be a non-negative integer.");
        }
        int result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

}