package src.ro.uvt.fi.dp;

/// interface actions need to flw
public interface Command {
    void execute();
    void undo();
}