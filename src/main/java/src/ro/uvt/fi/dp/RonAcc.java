package src.ro.uvt.fi.dp;

public class RonAcc extends Account{
    public RonAcc (String accountCode, double amount){
        super (accountCode, amount, Account.TYPE.RON);

    }
    @Override
    public String toString() {
        return "Account RON: code=" + getAccountCode() + ",  amount = " + getAmount();
    }
    /// moved transfer logic to subclass
    @Override
    public void transfer(Account c, double s) {
        logger.info("Transfer initiated from " + getAccountCode() + " for amount: " + s);
        c.retrieve(s);
        depose(s);
    }

}
