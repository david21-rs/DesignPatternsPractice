///command for deposit
package src.ro.uvt.fi.dp;

public class DepositCommand implements Command {
    private Account account;
    private double amount;

    public DepositCommand(Account account, double amount) {
        this.account = account;
        this.amount = amount;
    }

    @Override public void execute() { account.depose(amount); }
    @Override public void undo() { account.retrieve(amount); } /// reverses the deposit
}