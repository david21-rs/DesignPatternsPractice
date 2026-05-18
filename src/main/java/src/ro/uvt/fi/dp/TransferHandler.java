package src.ro.uvt.fi.dp;

///chain of responsibility pattern : Chain of Responsibility is behavioral
/// design pattern that allows passing request along the chain of potential handlers until one of them handles request. if problem found > chain breaks
public abstract class TransferHandler {
    protected TransferHandler next;

    public TransferHandler setNext(TransferHandler next) {
        this.next = next;
        return next; /// return next
    }

    public abstract boolean handle(Account from, Account to, double amount);

    protected boolean checkNext(Account from, Account to, double amount) {
        if (next == null) return true; ///end of chain
        return next.handle(from, to, amount);
    }
}