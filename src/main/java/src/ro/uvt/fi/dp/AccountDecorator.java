package src.ro.uvt.fi.dp;

///decorator implementation: an abstract wrapper that looks and acts like an account
/// decorator used to attach additional responsibilities to an object
public abstract class AccountDecorator extends Account {
    protected Account wrappedAccount;

    public AccountDecorator(Account wrappedAccount) {
        super(wrappedAccount.getAccountCode(), 0, wrappedAccount.getType());
        this.wrappedAccount = wrappedAccount;
    }

    @Override public double getAmount() { return wrappedAccount.getAmount(); }
    @Override public void depose(double amount) { wrappedAccount.depose(amount); }
    @Override public void retrieve(double amount) { wrappedAccount.retrieve(amount); }
    @Override public void transfer(Account c, double s) { wrappedAccount.transfer(c, s); }
    @Override public String toString() { return wrappedAccount.toString(); }
}