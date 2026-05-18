package src.ro.uvt.fi.dp;
///  the reason for this interface's existence is said in bank class
public interface InterfCLient {
    String getName();
    Account getAccount(String accountCode);
}
