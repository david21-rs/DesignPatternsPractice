package src.ro.uvt.fi.dp;

/// FACTORY METHOD PATTERN: I just handle the creation logic in one place
/// now if we want to add USDAcc or KRW or whatever all we have to do is just add one more if here without touching the Client code
public class AccountFactory {
    public static Account createAccount(Account.TYPE type, String accountCode, double amount) {
        if (type == Account.TYPE.RON) {
            return new RonAcc(accountCode, amount);
        } else if (type == Account.TYPE.EUR) {
            return new EuroAcc(accountCode, amount);
        }
        return null;
    }
}