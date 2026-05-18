package src.ro.uvt.fi.dp;

/// decorator: add overdraft bhvr dynamically
public class OverdraftDecorator extends AccountDecorator {
    private double overdraftLimit;

    public OverdraftDecorator(Account wrappedAccount, double limit) {
        super(wrappedAccount);
        this.overdraftLimit = limit;
    }

    @Override
    public void retrieve(double amount) {
        if (wrappedAccount.getAmount() >= amount) {
            wrappedAccount.retrieve(amount); /// mormal retrieve
        } else if (wrappedAccount.getAmount() + overdraftLimit >= amount) {
            /// intercept and allow it to go negative
            wrappedAccount.amount -= amount;
            logger.info("Retrieved " + amount + " using OVERDRAFT on " + wrappedAccount.getAccountCode());
        } else {
            logger.warning("Overdraft limit exceeded on " + wrappedAccount.getAccountCode());
        }
    }
}