package src.ro.uvt.fi.dp;

public class EuroAcc extends Account{ ///  made the euroaccount and ron account separate from the main account class
/// because if we want to add usd account for example we can just make another class instead of
/// having to change the main class making open for extension thus respecting OCP from SOLID
    public EuroAcc (String accountCode, double amount){
        super (accountCode, amount, Account.TYPE.EUR);

    }
    @Override
    public String toString() {
        return "Account Euro: code=" + getAccountCode() + ",  amount = " + getAmount();
    }
    @Override
    public void transfer(Account c, double s) {
        /// empty cus original code didnt do anything with euro acc
        /// but it can easily be modified without changing stuff in parent class
        logger.info("Transfer attempted from EUR account " + getAccountCode() + " but EUR transfers are not supported. :("); ///we log the attempt but expect it not to work
    }

}
