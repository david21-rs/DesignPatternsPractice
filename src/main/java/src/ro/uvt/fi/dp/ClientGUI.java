package src.ro.uvt.fi.dp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/// CLIENT PORTAL - the tarnished-facing window
/// patterns in here: Command (deposit/withdraw via txMgr), Chain of Responsibility (transfer validation),
/// Decorator (overdraft awareness), Singleton (BankDataStore)
public class ClientGUI extends JFrame {

    private static final int W = 520, H = 480;

    private final BankDataStore      store = BankDataStore.getInstance();
    private final TransactionManager txMgr = new TransactionManager();
    private final TransferHandler    chain;

    private CardLayout cards;
    private JPanel     content;

    // login refs
    private JTextField loginField;

    // dashboard refs
    private JLabel welcomeLbl;
    private JComboBox<String> accCombo;
    private EldenTheme.AnimLabel balLbl, totLbl;
    private JLabel odBadge;
    private JTextField amtField;

    // session
    private Client        client;
    private List<Account> accs;

    public ClientGUI() {
        super("BCR Bank – Client Portal");
        TransferHandler b = new BalanceCheckHandler();
        b.setNext(new LimitCheckHandler());
        chain = b;

        EldenTheme.styleDialogs();
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        EldenTheme.styleDialogs();   /// call again after laf because laf resets some keys

        setSize(W, H);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { store.save(); dispose(); }
        });
        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        EldenTheme.EldenBackground root = new EldenTheme.EldenBackground();
        root.setLayout(new BorderLayout());
        setContentPane(root);

        cards   = new CardLayout();
        content = new JPanel(cards);
        content.setOpaque(false);
        root.add(content, BorderLayout.CENTER);

        content.add(buildLoginCard(), "login");
        content.add(buildDashCard(),  "dash");
    }

    //login card

    private JPanel buildLoginCard() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);

        /// pulsing border state - timer drives it, box reads it in paintComponent
        float[] pulse = { 0.4f };
        boolean[] up  = { true  };

        JPanel box = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(14, 10, 6, 235));
                g2.fillRect(0, 0, w, h);
                int alpha = (int)(pulse[0] * 200);
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(new Color(EldenTheme.GOLD.getRed(), EldenTheme.GOLD.getGreen(), EldenTheme.GOLD.getBlue(), alpha));
                g2.drawRect(0, 0, w - 1, h - 1);
                EldenTheme.paintCorners(g2, 0, 0, w - 1, h - 1,
                        new Color(EldenTheme.GOLD_HI.getRed(), EldenTheme.GOLD_HI.getGreen(),
                                EldenTheme.GOLD_HI.getBlue(), (int)(pulse[0] * 240)), 18);
                EldenTheme.paintSep(g2, 24, 85, w - 48);
                EldenTheme.paintSep(g2, 24, h - 90, w - 48);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setPreferredSize(new Dimension(330, 310));

        /// 50ms tick - smooth enough, not wasteful
        new Timer(50, e -> {
            pulse[0] += up[0] ? 0.016f : -0.016f;
            if (pulse[0] >= 1f)   { pulse[0] = 1f;   up[0] = false; }
            if (pulse[0] <= 0.3f) { pulse[0] = 0.3f; up[0] = true;  }
            box.repaint();
        }).start();

        JLabel title = new JLabel("⬥  BCR BANK  ⬥", SwingConstants.CENTER);
        title.setFont(EldenTheme.serif(Font.BOLD, 22f));
        title.setForeground(EldenTheme.GOLD_HI);
        title.setBounds(0, 20, 330, 36);
        box.add(title);

        JLabel sub = new JLabel("TARNISHED, ARISE", SwingConstants.CENTER);
        sub.setFont(EldenTheme.serif(Font.ITALIC, 12f));
        sub.setForeground(EldenTheme.PARCH_D);
        sub.setBounds(0, 55, 330, 22);
        box.add(sub);

        JLabel nameTag = new JLabel("YOUR NAME");
        nameTag.setFont(EldenTheme.serif(Font.BOLD, 10f));
        nameTag.setForeground(EldenTheme.PARCH_D);
        nameTag.setBounds(55, 106, 220, 16);
        box.add(nameTag);

        loginField = EldenTheme.styledField(18);
        loginField.setBounds(55, 124, 220, 36);
        loginField.addActionListener(e -> doLogin());
        box.add(loginField);

        EldenTheme.EldenButton btn = new EldenTheme.EldenButton("ARISE, TARNISHED", EldenTheme.GOLD);
        btn.setBounds(55, 182, 220, 38);
        btn.addActionListener(e -> doLogin());
        box.add(btn);

        JLabel hint = new JLabel("Try: Ionescu Ion  ·  Marinescu Marin", SwingConstants.CENTER);
        hint.setFont(EldenTheme.serif(Font.ITALIC, 10f));
        hint.setForeground(EldenTheme.ASH);
        hint.setBounds(0, 256, 330, 18);
        box.add(hint);

        outer.add(box);
        return outer;
    }

    private void doLogin() {
        String name = loginField.getText().trim();
        if (name.isEmpty()) { warn("Name the Tarnished."); return; }
        client = store.findClient(name);
        if (client == null) { warn("No soul found bearing the name:\n\"" + name + "\""); return; }
        accs = store.getAccountsForClient(name);
        if (accs.isEmpty()) { warn("This Tarnished holds no vaults.\nContact your branch officer."); return; }
        openDash();
    }

    //dashboard card

    private JPanel buildDashCard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        //  header
        JPanel header = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(8, 6, 3, 245));
                g2.fillRect(0, 0, getWidth(), getHeight());
                EldenTheme.paintSep(g2, 12, getHeight() - 1, getWidth() - 24);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(W, 50));

        welcomeLbl = new JLabel("⬥  TARNISHED");
        welcomeLbl.setFont(EldenTheme.serif(Font.BOLD, 14f));
        welcomeLbl.setForeground(EldenTheme.GOLD);
        welcomeLbl.setBounds(16, 13, 340, 24);
        header.add(welcomeLbl);

        EldenTheme.EldenButton restBtn = new EldenTheme.EldenButton("REST AT GRACE", EldenTheme.BLOOD);
        restBtn.setBounds(W - 162, 10, 148, 30);
        restBtn.addActionListener(e -> doLogout());
        header.add(restBtn);

        panel.add(header, BorderLayout.NORTH);

        //center
        JPanel center = new JPanel(null);
        center.setOpaque(false);

        int y = 16;

        JLabel accTag = dimLabel("VAULT");
        accTag.setBounds(20, y + 4, 55, 18);
        center.add(accTag);

        accCombo = EldenTheme.styledCombo();
        accCombo.setBounds(80, y, 200, 27);
        accCombo.addActionListener(e -> refreshTiles());
        center.add(accCombo);

        odBadge = new JLabel("⬥ OVERDRAFT BOON");
        odBadge.setFont(EldenTheme.serif(Font.BOLD, 10f));
        odBadge.setForeground(new Color(205, 135, 48));
        odBadge.setBounds(294, y + 5, 200, 16);
        odBadge.setVisible(false);
        center.add(odBadge);

        y += 40;

        JPanel tiles = buildTiles();
        tiles.setBounds(20, y, W - 50, 88);
        center.add(tiles);
        y += 100;

        JPanel sep = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                EldenTheme.paintSep((Graphics2D) g.create(), 0, getHeight()/2, getWidth());
            }
        };
        sep.setOpaque(false);
        sep.setBounds(20, y, W - 50, 10);
        center.add(sep);
        y += 22;

        JLabel amtTag = dimLabel("RUNES");
        amtTag.setBounds(20, y + 6, 55, 16);
        center.add(amtTag);

        amtField = EldenTheme.styledField(10);
        amtField.setBounds(80, y, 170, 32);
        center.add(amtField);
        y += 50;

        int bx = 20, bw = 108, bh = 36, gap = 11;
        EldenTheme.EldenButton dep  = new EldenTheme.EldenButton("STORE RUNES",  EldenTheme.FOREST);
        EldenTheme.EldenButton wdr  = new EldenTheme.EldenButton("SPEND RUNES",  EldenTheme.BLOOD);
        EldenTheme.EldenButton tfr  = new EldenTheme.EldenButton("SEND RUNES",   EldenTheme.ARCANE);
        EldenTheme.EldenButton undo = new EldenTheme.EldenButton("UNDO GRACE",   EldenTheme.ASH);

        dep.setBounds(bx,              y, bw, bh); center.add(dep);
        wdr.setBounds(bx+(bw+gap),     y, bw, bh); center.add(wdr);
        tfr.setBounds(bx+(bw+gap)*2,   y, bw, bh); center.add(tfr);
        undo.setBounds(bx+(bw+gap)*3,  y, bw, bh); center.add(undo);

        dep.addActionListener(e -> doDeposit());
        wdr.addActionListener(e -> doWithdraw());
        tfr.addActionListener(e -> doTransfer());
        undo.addActionListener(e -> doUndo());

        /// tiny hints below each button so there's no ambiguity
        y += bh + 4;
        String[] hints = { "Deposit", "Withdraw", "Transfer", "Undo last" };
        for (int i = 0; i < 4; i++) {
            JLabel h = new JLabel(hints[i], SwingConstants.CENTER);
            h.setFont(EldenTheme.serif(Font.ITALIC, 9f));
            h.setForeground(EldenTheme.ASH);
            h.setBounds(bx + i*(bw+gap), y, bw, 14);
            center.add(h);
        }

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTiles() {
        JPanel p = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int hw = (getWidth() - 14) / 2;
                EldenTheme.paintGoldPanel(g2, 0, 0, hw, getHeight() - 1);
                EldenTheme.paintGoldPanel(g2, hw + 14, 0, hw, getHeight() - 1);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        int hw = (W - 50 - 14) / 2;

        JLabel capA = tileCaption("CURRENT BALANCE");
        capA.setBounds(0, 8, hw, 16);
        p.add(capA);

        balLbl = new EldenTheme.AnimLabel();
        balLbl.setBounds(0, 26, hw, 50);
        p.add(balLbl);

        JLabel capB = tileCaption("WITH INTEREST");
        capB.setBounds(hw + 14, 8, hw, 16);
        p.add(capB);

        totLbl = new EldenTheme.AnimLabel();
        totLbl.setForeground(EldenTheme.GOLD);
        totLbl.setFont(EldenTheme.serif(Font.BOLD, 22f));
        totLbl.setBounds(hw + 14, 26, hw, 50);
        p.add(totLbl);

        return p;
    }

    private JLabel tileCaption(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(EldenTheme.serif(Font.PLAIN, 10f));
        l.setForeground(EldenTheme.PARCH_D);
        return l;
    }

    private JLabel dimLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(EldenTheme.serif(Font.BOLD, 10f));
        l.setForeground(EldenTheme.PARCH_D);
        return l;
    }

    //session + operators

    private void openDash() {
        welcomeLbl.setText("⬥  " + client.getName().toUpperCase());
        accCombo.removeAllItems();
        for (Account a : accs) accCombo.addItem(a.getAccountCode() + "  (" + a.getType() + ")");
        refreshTiles();
        cards.show(content, "dash");
        loginField.setText("");
    }

    private void doLogout() {
        client = null;
        accs   = null;
        amtField.setText("");
        cards.show(content, "login");
    }

    private Account sel() {
        int i = accCombo.getSelectedIndex();
        return (i < 0 || accs == null || i >= accs.size()) ? null : accs.get(i);
    }

    private void refreshTiles() {
        Account a = sel();
        if (a == null) return;
        String sfx = a.getType().name();
        /// direct computation cus getTotalAmount() breaks for decorators - known issue in AccountDecorator
        double total = a.getAmount() * (1 + a.getInterest());
        balLbl.animateTo(a.getAmount(), sfx);
        totLbl.animateTo(total, sfx);
        odBadge.setVisible(store.getOverdraftLimit(a.getAccountCode()) > 0);
    }

    private void doDeposit() {
        Account a = sel(); if (a == null) return;
        double amt = parseAmt(); if (amt < 0) return;
        txMgr.executeCommand(new DepositCommand(a, amt));   /// Command pattern
        amtField.setText("");
        refreshTiles();
        info(String.format("%.2f %s stored in vault [%s]", amt, a.getType(), a.getAccountCode()));
    }

    private void doWithdraw() {
        Account a = sel(); if (a == null) return;
        double amt = parseAmt(); if (amt < 0) return;
        double avail = a.getAmount() + store.getOverdraftLimit(a.getAccountCode());
        if (avail < amt) {
            warn(String.format("Insufficient runes!\nAvailable: %.2f %s%s",
                    avail, a.getType(),
                    store.getOverdraftLimit(a.getAccountCode()) > 0 ? "  (overdraft incl.)" : ""));
            return;
        }
        txMgr.executeCommand(new WithdrawCommand(a, amt));
        amtField.setText("");
        refreshTiles();
        info(String.format("%.2f %s spent from vault [%s]", amt, a.getType(), a.getAccountCode()));
    }

    private void doTransfer() {
        if (accs == null || accs.size() < 2) { warn("You need at least two vaults to transfer."); return; }
        Account from = sel(); if (from == null) return;
        double amt = parseAmt(); if (amt < 0) return;

        String[] opts = accs.stream()
                .filter(a -> !a.getAccountCode().equals(from.getAccountCode()))
                .map(a -> a.getAccountCode() + "  (" + a.getType() + ")")
                .toArray(String[]::new);

        String chosen = (String) JOptionPane.showInputDialog(this,
                String.format("Send  %.2f %s  from  [%s]  to:", amt, from.getType(), from.getAccountCode()),
                "Send Runes", JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
        if (chosen == null) return;

        String toCode = chosen.split("\\s+")[0];
        Account to = accs.stream()
                .filter(a -> a.getAccountCode().equals(toCode))
                .findFirst().orElse(null);
        if (to == null) return;

        if (!chain.handle(from, to, amt)) {     /// Chain of Responsibility
            warn("Transfer blocked by the Golden Order.\n(Balance or daily limit check failed)");
            return;
        }
        from.transfer(to, amt);
        refreshTiles();
        info(String.format("%.2f sent  [%s]  →  [%s]", amt, from.getAccountCode(), to.getAccountCode()));
    }

    private void doUndo() {
        txMgr.undoLast();
        refreshTiles();
        info("Last action undone by Grace.");
    }

    private double parseAmt() {
        try {
            double v = Double.parseDouble(amtField.getText().trim());
            if (v <= 0) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException ex) {
            warn("Enter a valid positive number of runes.");
            return -1;
        }
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "⬥  The Guidance of Grace", JOptionPane.INFORMATION_MESSAGE);
    }
    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "⬥  Scarlet Warning", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}