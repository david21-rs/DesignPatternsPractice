package src.ro.uvt.fi.dp;
import java.util.Stack;

/// command pattern: keeps a history of everything that happens
public class TransactionManager {
    private Stack<Command> history = new Stack<>();

    public void executeCommand(Command c) {
        c.execute();
        history.push(c); /// save to history
    }

    public void undoLast() {
        if (!history.isEmpty()) {
            Command c = history.pop();
            c.undo(); /// reverse mistake if needed
        }
    }
}