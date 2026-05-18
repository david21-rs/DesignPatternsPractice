package src.ro.uvt.fi.dp;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/// OFFICER DASHBOARD - the commander-facing window
/// patterns: Chain of Responsibility (transfer validation), Decorator (overdraft application),
/// Singleton (BankDataStore), Factory (account creation), Builder (client creation), Command (transfers)
public class OfficerGUI extends JFrame {

    private final BankDataStore      store = BankDataStore.getInstance();
    private final TransactionManager txMgr = new TransactionManager();
    private final TransferHandler    chain;

    private static final DateTimeFormatter TFMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // left panel
    private DefaultListModel<String> listModel;
    private JList<String>            clientList;

    // right panel
    private JLabel             clientInfoLbl;
    private DefaultTableModel  tableModel;
    private JTable             accountTable;
    private EldenTheme.EldenButton openAccBtn, overdraftBtn, transferBtn;

    // log
    private JTextArea logArea;

    private Client selectedClient;

    public OfficerGUI() {
        super("BCR Bank – Officer Dashboard");
        TransferHandler b = new BalanceCheckHandler();
        b.setNext(new LimitCheckHandler());
        chain = b;

        EldenTheme.styleDialogs();
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        EldenTheme.styleDialogs();

        setSize(970, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { store.save(); log("Data saved — farewell, Commander."); dispose(); }
        });
        buildUI();
        refreshList();
        log("Officer dashboard opened. Welcome, Commander.");
        setVisible(true);
    }


    private void buildUI() {
        EldenTheme.EldenBackground root = new EldenTheme.EldenBackground();
        root.setLayout(new BorderLayout(0, 0));
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);

        JPanel mid = new JPanel(new BorderLayout(0, 0));
        mid.setOpaque(false);
        mid.add(buildSplit(), BorderLayout.CENTER);
        mid.add(buildLog(),   BorderLayout.SOUTH);
        root.add(mid, BorderLayout.CENTER);
    }

//header
    private JPanel buildHeader() {
        JPanel h = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(6, 4, 2, 248));
                g2.fillRect(0, 0, getWidth(), getHeight());
                EldenTheme.paintSep(g2, 14, getHeight() - 1, getWidth() - 28);
                g2.dispose();
            }
        };
        h.setOpaque(false);
        h.setPreferredSize(new Dimension(970, 52));

        JLabel title = new JLabel("⬥  BCR BANK  —  COMMANDER'S CHAMBER  ⬥");
        title.setFont(EldenTheme.serif(Font.BOLD, 15f));
        title.setForeground(EldenTheme.GOLD_HI);
        title.setBounds(16, 14, 600, 24);
        h.add(title);

        JLabel clock = new JLabel();
        clock.setFont(EldenTheme.serif(Font.PLAIN, 11f));
        clock.setForeground(EldenTheme.PARCH_D);
        clock.setBounds(850, 17, 110, 18);
        h.add(clock);
        new Timer(1000, e -> clock.setText(LocalTime.now().format(TFMT))).start();

        return h;
    }

    //split pane

    private JSplitPane buildSplit() {
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildLeft(), buildRight());
        sp.setDividerLocation(248);
        sp.setDividerSize(3);
        sp.setOpaque(false);
        sp.setBorder(null);
        /// colour the divider gold
        sp.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) { // <--- FIXED!
                        g.setColor(EldenTheme.GOLD_LO);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        super.paint(g); // Draws the standard divider features on top
                    }
                };
            }
        });
        return sp;
    }

    // left pane : client list

    private JPanel buildLeft() {
        JPanel p = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(EldenTheme.PANEL.getRed(), EldenTheme.PANEL.getGreen(),
                        EldenTheme.PANEL.getBlue(), 215));
                ((Graphics2D)g).fillRect(0, 0, getWidth(), getHeight());
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 6));

        JLabel heading = new JLabel("TARNISHED SOULS");
        heading.setFont(EldenTheme.serif(Font.BOLD, 12f));
        heading.setForeground(EldenTheme.GOLD);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));
        p.add(heading, BorderLayout.NORTH);

        listModel  = new DefaultListModel<>();
        clientList = new JList<>(listModel);
        clientList.setOpaque(true);
        clientList.setBackground(new Color(10, 7, 4));
        clientList.setForeground(EldenTheme.PARCH);
        clientList.setSelectionBackground(new Color(50, 38, 10));
        clientList.setSelectionForeground(EldenTheme.GOLD_HI);
        clientList.setFont(EldenTheme.serif(Font.PLAIN, 13f));
        clientList.setFixedCellHeight(30);
        clientList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foc) {
                super.getListCellRendererComponent(l, v, i, sel, foc);
                setBackground(sel ? new Color(50, 38, 10) : (i % 2 == 0 ? new Color(10, 7, 4) : new Color(14, 10, 6)));
                setForeground(sel ? EldenTheme.GOLD_HI : EldenTheme.PARCH);
                setFont(EldenTheme.serif(Font.PLAIN, 13f));
                setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                setText("⬥  " + v);
                return this;
            }
        });
        clientList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && clientList.getSelectedValue() != null)
                loadClient(clientList.getSelectedValue());
        });

        JScrollPane scroll = styledScroll(clientList);
        p.add(scroll, BorderLayout.CENTER);

        EldenTheme.EldenButton addBtn = new EldenTheme.EldenButton("+ SUMMON TARNISHED", EldenTheme.GOLD);
        addBtn.setPreferredSize(new Dimension(230, 34));
        addBtn.addActionListener(e -> dlgAddClient());

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        south.add(addBtn);
        p.add(south, BorderLayout.SOUTH);

        return p;
    }

    // right pane : c lient details n account details

    private JPanel buildRight() {
        JPanel p = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(EldenTheme.PANEL.getRed(), EldenTheme.PANEL.getGreen(),
                        EldenTheme.PANEL.getBlue(), 210));
                ((Graphics2D)g).fillRect(0, 0, getWidth(), getHeight());
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 12));

        clientInfoLbl = new JLabel("  ← Select a soul from the list");
        clientInfoLbl.setFont(EldenTheme.serif(Font.BOLD, 14f));
        clientInfoLbl.setForeground(EldenTheme.GOLD);
        clientInfoLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        p.add(clientInfoLbl, BorderLayout.NORTH);

        /// account table
        String[] cols = { "Account Code", "Type", "Balance", "Bal + Interest", "Overdraft" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        accountTable = new JTable(tableModel);
        accountTable.setBackground(new Color(10, 7, 4));
        accountTable.setForeground(EldenTheme.PARCH);
        accountTable.setGridColor(new Color(55, 42, 14));
        accountTable.setRowHeight(28);
        accountTable.setFont(EldenTheme.serif(Font.PLAIN, 13f));
        accountTable.setSelectionBackground(new Color(50, 38, 10));
        accountTable.setSelectionForeground(EldenTheme.GOLD_HI);
        accountTable.setShowGrid(true);

        JTableHeader th = accountTable.getTableHeader();
        th.setBackground(new Color(16, 12, 5));
        th.setForeground(EldenTheme.GOLD);
        th.setFont(EldenTheme.serif(Font.BOLD, 12f));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, EldenTheme.GOLD_LO));

        /// right-align the numeric columns
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        right.setBackground(new Color(10, 7, 4));
        right.setForeground(EldenTheme.PARCH);
        for (int col : new int[]{ 2, 3, 4 }) {
            accountTable.getColumnModel().getColumn(col).setCellRenderer(right);
        }

        /// alternating row renderer
        accountTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBackground(sel ? new Color(50, 38, 10) : (row % 2 == 0 ? new Color(10, 7, 4) : new Color(14, 10, 6)));
                setForeground(sel ? EldenTheme.GOLD_HI : EldenTheme.PARCH);
                setFont(EldenTheme.serif(Font.PLAIN, 13f));
                if (col >= 2) setHorizontalAlignment(RIGHT);
                else setHorizontalAlignment(LEFT);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        p.add(styledScroll(accountTable), BorderLayout.CENTER);

        // action buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        btns.setOpaque(false);

        openAccBtn  = new EldenTheme.EldenButton("+ OPEN VAULT",      EldenTheme.FOREST);
        overdraftBtn = new EldenTheme.EldenButton("GRANT BOON",        EldenTheme.ASH);
        transferBtn  = new EldenTheme.EldenButton("MOVE RUNES",        EldenTheme.ARCANE);

        for (EldenTheme.EldenButton b : new EldenTheme.EldenButton[]{ openAccBtn, overdraftBtn, transferBtn }) {
            b.setPreferredSize(new Dimension(138, 32));
            b.setEnabled(false);
            btns.add(b);
        }

        openAccBtn.addActionListener(e -> dlgOpenAccount());
        overdraftBtn.addActionListener(e -> dlgOverdraft());
        transferBtn.addActionListener(e -> dlgTransfer());

        p.add(btns, BorderLayout.SOUTH);
        return p;
    }

    // panel of logging

    private JPanel buildLog() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(6, 4, 2, 230));
                g2.fillRect(0, 0, getWidth(), getHeight());
                EldenTheme.paintSep(g2, 14, 0, getWidth() - 28);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(970, 128));
        p.setBorder(BorderFactory.createEmptyBorder(4, 10, 6, 10));

        JLabel logTitle = new JLabel("  CHRONICLES  ⬥");
        logTitle.setFont(EldenTheme.serif(Font.BOLD, 11f));
        logTitle.setForeground(EldenTheme.GOLD_LO);
        p.add(logTitle, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setOpaque(true);
        logArea.setBackground(new Color(9, 6, 3));
        logArea.setForeground(EldenTheme.PARCH_D);
        logArea.setFont(EldenTheme.serif(Font.PLAIN, 11f));
        logArea.setCaretColor(EldenTheme.GOLD);
        logArea.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JScrollPane sc = styledScroll(logArea);
        sc.setBorder(BorderFactory.createLineBorder(EldenTheme.GOLD_LO, 1));
        p.add(sc, BorderLayout.CENTER);
        return p;
    }

    // dataloading

    private void refreshList() {
        String sel = clientList.getSelectedValue();
        listModel.clear();
        for (Client c : store.getClients()) listModel.addElement(c.getName());
        if (sel != null) clientList.setSelectedValue(sel, true);
    }

    private void loadClient(String name) {
        selectedClient = store.findClient(name);
        if (selectedClient == null) return;

        clientInfoLbl.setText("  ⬥  " + selectedClient.getName()
                + "   ·   " + selectedClient.getAddress());

        tableModel.setRowCount(0);
        for (Account a : store.getAccountsForClient(name)) {
            double total = a.getAmount() * (1 + a.getInterest());
            double od    = store.getOverdraftLimit(a.getAccountCode());
            tableModel.addRow(new Object[]{
                    a.getAccountCode(),
                    a.getType().name(),
                    String.format("%.2f", a.getAmount()),
                    String.format("%.2f", total),
                    od > 0 ? String.format("%.2f", od) : "—"
            });
        }

        openAccBtn.setEnabled(true);
        overdraftBtn.setEnabled(true);
        transferBtn.setEnabled(true);
    }



    /// add new client - uses Builder pattern under the hood in BankDataStore
    private void dlgAddClient() {
        JTextField nameF = EldenTheme.styledField(20);
        JTextField addrF = EldenTheme.styledField(20);
        JPanel form = form(new String[]{"Full Name:", "Address:"}, new JTextField[]{nameF, addrF});

        if (JOptionPane.showConfirmDialog(this, form, "Summon New Tarnished",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        String name = nameF.getText().trim();
        String addr = addrF.getText().trim();
        if (name.isEmpty()) { warn("A name is required."); return; }
        if (store.clientExists(name)) { warn("A Tarnished by that name already walks the Lands Between."); return; }

        store.addClient(name, addr.isEmpty() ? "Unknown" : addr);
        refreshList();
        log("Summoned:  " + name + "  (" + (addr.isEmpty() ? "no address" : addr) + ")");
    }

    /// open a new account for selected client - uses Factory pattern
    private void dlgOpenAccount() {
        if (selectedClient == null) return;
        JTextField codeF = EldenTheme.styledField(12);
        JComboBox<String> typeC = EldenTheme.styledCombo();
        typeC.addItem("RON"); typeC.addItem("EUR");
        JTextField amtF  = EldenTheme.styledField(10);
        amtF.setText("0.00");

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setOpaque(false);
        for (String[] row : new String[][]{{"Account Code:", null},{"Type:", null},{"Initial Deposit:", null}}) {
            JLabel l = new JLabel(row[0]); l.setForeground(EldenTheme.PARCH); form.add(l);
        }
        /// redo since we have mixed types - just build manually
        JPanel f2 = new JPanel(new GridLayout(3, 2, 8, 8));
        f2.setOpaque(false);
        JLabel la = new JLabel("Account Code:"); la.setForeground(EldenTheme.PARCH); f2.add(la); f2.add(codeF);
        JLabel lb = new JLabel("Type:");          lb.setForeground(EldenTheme.PARCH); f2.add(lb); f2.add(typeC);
        JLabel lc = new JLabel("Initial Deposit:"); lc.setForeground(EldenTheme.PARCH); f2.add(lc); f2.add(amtF);

        if (JOptionPane.showConfirmDialog(this, f2,
                "Open Vault for " + selectedClient.getName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        String code = codeF.getText().trim();
        if (code.isEmpty()) { warn("Account code cannot be empty."); return; }
        if (store.accountCodeExists(code)) { warn("That code is already in use."); return; }

        double amt;
        try { amt = Double.parseDouble(amtF.getText().trim()); }
        catch (NumberFormatException ex) { warn("Invalid deposit amount."); return; }

        String ts = (String) typeC.getSelectedItem();
        Account.TYPE type = "EUR".equals(ts) ? Account.TYPE.EUR : Account.TYPE.RON;
        store.addAccountToClient(selectedClient.getName(), code, type, amt);
        loadClient(selectedClient.getName());
        log("Opened " + ts + " vault [" + code + "] for " + selectedClient.getName() + "  (initial: " + amt + ")");
    }

    /// apply overdraft decorator to a chosen account - Decorator pattern
    private void dlgOverdraft() {
        if (selectedClient == null) return;
        List<Account> accs = store.getAccountsForClient(selectedClient.getName());
        if (accs.isEmpty()) { warn("No vaults found for this Tarnished."); return; }

        JComboBox<String> accC = EldenTheme.styledCombo();
        accs.forEach(a -> accC.addItem(a.getAccountCode() + "  (" + a.getType() + ")"));
        JTextField limF = EldenTheme.styledField(10);
        limF.setText("500");

        JPanel f = new JPanel(new GridLayout(2, 2, 8, 8));
        f.setOpaque(false);
        JLabel la = new JLabel("Account:"); la.setForeground(EldenTheme.PARCH); f.add(la); f.add(accC);
        JLabel lb = new JLabel("Boon Limit:"); lb.setForeground(EldenTheme.PARCH); f.add(lb); f.add(limF);

        if (JOptionPane.showConfirmDialog(this, f,
                "Grant Overdraft Boon — " + selectedClient.getName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        Account acc = accs.get(accC.getSelectedIndex());
        double lim;
        try { lim = Double.parseDouble(limF.getText().trim()); }
        catch (NumberFormatException ex) { warn("Invalid limit."); return; }

        /// BankDataStore wraps the account in an OverdraftDecorator - decorator pattern
        store.applyOverdraft(selectedClient.getName(), acc.getAccountCode(), lim);
        loadClient(selectedClient.getName());
        log("Overdraft boon (limit=" + lim + ") granted to [" + acc.getAccountCode() + "] for " + selectedClient.getName());
    }

    /// move runes between two vaults of the same tarnished
    /// uses Chain of Responsibility to validate + Command to record
    private void dlgTransfer() {
        if (selectedClient == null) return;
        List<Account> accs = store.getAccountsForClient(selectedClient.getName());
        if (accs.size() < 2) { warn("Need at least two vaults to transfer."); return; }

        JComboBox<String> fromC = EldenTheme.styledCombo();
        JComboBox<String> toC   = EldenTheme.styledCombo();
        accs.forEach(a -> { fromC.addItem(a.getAccountCode() + " (" + a.getType() + ")");
            toC.addItem(a.getAccountCode()   + " (" + a.getType() + ")"); });
        toC.setSelectedIndex(1);
        JTextField amtF = EldenTheme.styledField(10);
        amtF.setText("100");

        JPanel f = new JPanel(new GridLayout(3, 2, 8, 8));
        f.setOpaque(false);
        JLabel la = new JLabel("From:"); la.setForeground(EldenTheme.PARCH); f.add(la); f.add(fromC);
        JLabel lb = new JLabel("To:");   lb.setForeground(EldenTheme.PARCH); f.add(lb); f.add(toC);
        JLabel lc = new JLabel("Amount:"); lc.setForeground(EldenTheme.PARCH); f.add(lc); f.add(amtF);

        if (JOptionPane.showConfirmDialog(this, f,
                "Move Runes — " + selectedClient.getName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        int fi = fromC.getSelectedIndex(), ti = toC.getSelectedIndex();
        if (fi == ti) { warn("Source and destination must be different vaults."); return; }

        Account from = accs.get(fi), to = accs.get(ti);
        double amt;
        try { amt = Double.parseDouble(amtF.getText().trim()); }
        catch (NumberFormatException ex) { warn("Invalid amount."); return; }

        if (!chain.handle(from, to, amt)) {       /// Chain of Responsibility
            warn("Transfer blocked by the Golden Order.\n(Balance or daily limit check)");
            return;
        }
        txMgr.executeCommand(new WithdrawCommand(from, amt));  /// Command pattern
        txMgr.executeCommand(new DepositCommand(to,   amt));
        loadClient(selectedClient.getName());
        log("Transfer: " + String.format("%.2f", amt) + "  [" + from.getAccountCode() + "]  →  [" + to.getAccountCode() + "]  for " + selectedClient.getName());
    }


    private JPanel form(String[] labels, JTextField[] fields) {
        JPanel p = new JPanel(new GridLayout(labels.length, 2, 8, 8));
        p.setOpaque(false);
        for (int i = 0; i < labels.length; i++) {
            JLabel l = new JLabel(labels[i]);
            l.setForeground(EldenTheme.PARCH);
            p.add(l);
            p.add(fields[i]);
        }
        return p;
    }

    private JScrollPane styledScroll(Component c) {
        JScrollPane sc = new JScrollPane(c);
        sc.setOpaque(false);
        sc.getViewport().setOpaque(false);
        sc.setBorder(BorderFactory.createLineBorder(EldenTheme.GOLD_LO, 1));
        sc.getVerticalScrollBar().setBackground(new Color(12, 9, 5));
        sc.getHorizontalScrollBar().setBackground(new Color(12, 9, 5));
        return sc;
    }

    private void log(String msg) {
        String ts = LocalTime.now().format(TFMT);
        logArea.append("[" + ts + "]  " + msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "⬥  Warning", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(OfficerGUI::new);
    }
}