package src.ro.uvt.fi.dp;

public class BankConfig {
    /// Before the numbers were hard coded which was bad because it violated SRP since account had too
    /// many reasons to change ( we needed to update the hard coded numbers manually)
    /// ideally there should be a txt file with the interest stuff but since we just have to refactor for
    /// this lab ill do that later --> lab 2 changes
    ///
    ///
    /// SINGLETON PATTERN: store the single shared instance here _>
    /// singleton in Java is a design pattern that restricts a class to have only one instance and provides a global point of access to that instance-
    /// singleton also helps here cus these interest thresholds are only laoded in memory once and shared globally so no data inconsistency
    private static BankConfig instance;
    private final double RON_THRESHOLD = 500.0; ///we make all these private for encapsulation purposes and to enforce singleton
    private final double RON_INTEREST_BELOW_THRESHOLD = 0.03;
    private final double RON_INTEREST_ABOVE_THRESHOLD = 0.08;
    private final double EUR_INTEREST = 0.01;

    /// private constructor guarantees no one can use 'new BankConfig()' directly
    private BankConfig() {}

    /// global access point to get the only instance
    public static BankConfig getInstance() {
        if (instance == null) {
            instance = new BankConfig();
        }
        return instance;
    }

    /// the amazing getters
    public double getRonThreshold() { return RON_THRESHOLD; }
    public double getRonInterestBelowThreshold() { return RON_INTEREST_BELOW_THRESHOLD; }
    public double getRonInterestAboveThreshold() { return RON_INTEREST_ABOVE_THRESHOLD; }
    public double getEurInterest() { return EUR_INTEREST; }
}