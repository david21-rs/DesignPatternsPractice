package src.ro.uvt.fi.dp;

import java.io.*;
import java.util.*;

/// SINGLETON - shared data store for both guis
/// keeps everything in sync -- if the officer opens a new account, the client portal sees it immediately
/// also handles persistence via serialization to bank_save.dat
public class BankDataStore {

    private static BankDataStore instance;

    /// parallel maps because Client.accounts is private and we cant iterate it from outside
    /// so we track everything ourselves here and keep the two in sync
    private final Map<String, Client>        clients             = new LinkedHashMap<>();
    private final Map<String, List<String>>  clientToAccountCodes = new LinkedHashMap<>();
    private final Map<String, Account>       accountMap          = new LinkedHashMap<>();
    private final Map<String, Double>        overdraftLimits     = new LinkedHashMap<>();

    private BankDataStore() {
        if (!loadFromDisk()) buildDemoData();
    }

    public static BankDataStore getInstance() {
        if (instance == null) instance = new BankDataStore();
        return instance;
    }



    public Collection<Client> getClients() { return clients.values(); }

    public Client findClient(String name) { return clients.get(name); }

    public boolean clientExists(String name) { return clients.containsKey(name); }

    public boolean accountCodeExists(String code) { return accountMap.containsKey(code); }

    /// returns the live Account objects for a client -- never null, may be empty list
    public List<Account> getAccountsForClient(String clientName) {
        List<String> codes = clientToAccountCodes.get(clientName);
        if (codes == null) return Collections.emptyList();
        List<Account> result = new ArrayList<>();
        for (String code : codes) {
            Account a = accountMap.get(code);
            if (a != null) result.add(a);
        }
        return result;
    }

    public double getOverdraftLimit(String accountCode) {
        return overdraftLimits.getOrDefault(accountCode, 0.0);
    }

    /// Builder pattern - Client.ClientBuilder does the actual construction
    public void addClient(String name, String address) {
        Client c = new Client.ClientBuilder(name).address(address).build();
        clients.put(name, c);
        clientToAccountCodes.put(name, new ArrayList<>());
    }

    /// Factory pattern - AccountFactory.createAccount handles instantiation
    public void addAccountToClient(String clientName, String code, Account.TYPE type, double amount) {
        Client c = clients.get(clientName);
        if (c == null) return;
        Account a = AccountFactory.createAccount(type, code, amount);
        c.addAccount(a);
        accountMap.put(code, a);
        clientToAccountCodes.get(clientName).add(code);
    }

    /// Decorator pattern - wraps the account in an OverdraftDecorator
    /// also updates accountMap so getAccountsForClient returns the decorated version
    public void applyOverdraft(String clientName, String code, double limit) {
        Account base = accountMap.get(code);
        if (base == null) return;
        /// unwrap if already decorated so we dont stack decorators on each other
        Account raw = (base instanceof AccountDecorator)
                ? ((AccountDecorator) base).wrappedAccount
                : base;
        Account decorated = new OverdraftDecorator(raw, limit);
        accountMap.put(code, decorated);
        overdraftLimits.put(code, limit);
        /// update client reference too
        Client c = clients.get(clientName);
        if (c != null) {
            c.addAccount(decorated); /// client now holds the decorated version
        }
    }



    private static final String SAVE_FILE = "bank_save.dat";

    public void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            SaveData data = new SaveData();
            for (Map.Entry<String, Client> e : clients.entrySet()) {
                ClientRecord cr = new ClientRecord();
                cr.name    = e.getKey();
                cr.address = e.getValue().getAddress();
                List<String> codes = clientToAccountCodes.get(cr.name);
                if (codes != null) {
                    for (String code : codes) {
                        Account a = accountMap.get(code);
                        if (a == null) continue;
                        AccountRecord ar = new AccountRecord();
                        ar.code     = code;
                        ar.type     = a.getType();
                        ar.amount   = a.getAmount();
                        ar.interest = a.getInterest();
                        ar.overdraft = overdraftLimits.getOrDefault(code, 0.0);
                        cr.accounts.add(ar);
                    }
                }
                data.clients.add(cr);
            }
            oos.writeObject(data);
        } catch (IOException ex) {
            System.err.println("BankDataStore: save failed -- " + ex.getMessage());
        }
    }

    private boolean loadFromDisk() {
        File f = new File(SAVE_FILE);
        if (!f.exists()) return false;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            SaveData data = (SaveData) ois.readObject();
            for (ClientRecord cr : data.clients) {
                addClient(cr.name, cr.address);
                for (AccountRecord ar : cr.accounts) {
                    addAccountToClient(cr.name, ar.code, ar.type, ar.amount);
                    if (ar.overdraft > 0) applyOverdraft(cr.name, ar.code, ar.overdraft);
                }
            }
            return true;
        } catch (Exception ex) {
            System.err.println("BankDataStore: load failed (" + ex.getMessage() + "), using demo data");
            return false;
        }
    }

    /// demo data so you have something to log in with on first run
    private void buildDemoData() {
        addClient("Ionescu Ion",     "Str. Victoriei 12, Cluj");
        addClient("Marinescu Marin", "Bd. Unirii 5, Timisoara");
        addClient("Vasilescu Vasile","Aleea Parcului 3, Iasi");

        addAccountToClient("Ionescu Ion",      "EUR124",  Account.TYPE.EUR, 2400.00);
        addAccountToClient("Ionescu Ion",      "RON1234", Account.TYPE.RON, 1500.00);
        addAccountToClient("Marinescu Marin",  "RON126",  Account.TYPE.RON,  400.00);
        addAccountToClient("Vasilescu Vasile", "EUR128",  Account.TYPE.EUR, 5200.00);
    }

    /// serialization helpers

    static class SaveData implements Serializable {
        List<ClientRecord> clients = new ArrayList<>();
    }

    static class ClientRecord implements Serializable {
        String name, address;
        List<AccountRecord> accounts = new ArrayList<>();
    }

    static class AccountRecord implements Serializable {
        String code;
        Account.TYPE type;
        double amount, interest, overdraft;
    }
}