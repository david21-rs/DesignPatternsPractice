package src.ro.uvt.fi.dp;

public class LimitCheckHandler extends TransferHandler {
    @Override
    public boolean handle(Account from, Account to, double amount) {
        if (amount > 6700) { /// 6700 RON daily limit
            from.logger.warning("Transfer Blocked: Exceeds 6700 limit!");
            return false; /// break the chain
        }
        return checkNext(from, to, amount);
    }
}