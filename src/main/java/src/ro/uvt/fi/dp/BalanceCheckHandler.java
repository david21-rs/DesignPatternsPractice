package src.ro.uvt.fi.dp;

public class BalanceCheckHandler extends TransferHandler {
    @Override
    public boolean handle(Account from, Account to, double amount) {
        if (from.getAmount() < amount) {
            from.logger.warning("Transfer Blocked: Insufficient funds!");
            return false; /// break chain
        }
        return checkNext(from, to, amount);
    }
}