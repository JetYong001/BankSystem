package models.InterestRate;

public class InterestRateCalculator {
    // Declares necessary variables
    private static final double r = 0.0025;

    public static double calcInterestRate(double P, double t) {
        // I = Prt
        return (P * r * t);
    }
}

