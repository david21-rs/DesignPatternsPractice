package src.ro.uvt.fi.dp;


import org.junit.Test;
import static org.junit.Assert.*;
public class BankingTest {
    /// test depose and retrieve operations
    @Test
    public void testDepositAndRetrieve() {
        Account acc = new RonAcc("RON001", 500); ///start with 500

        acc.depose(67);
        assertEquals(567, acc.getAmount(), 0.001); ///should be 567

        acc.retrieve(167);
        assertEquals(400, acc.getAmount(), 0.001); ///should be 400
    }
    /// test : transfer logic
    @Test
    public void testRonToRonTransfer() {
        Account a1 = new RonAcc("RON1", 100);
        Account a2 = new RonAcc("RON2", 50);
        a1.transfer(a2, 40); ///tranfer 40 from a2 to a1
        assertEquals(140, a1.getAmount(), 0.001);
        assertEquals(10, a2.getAmount(), 0.001);
    }

    /// test: euro acc shouldnt transfer cus its not yet implemented
    @Test
    public void testEurTransferIsIgnored() {
        Account eurAcc = new EuroAcc("EUR1", 100);
        Account targetAcc = new RonAcc("RON1", 50);
        eurAcc.transfer(targetAcc, 40); ///this shouldnt do anything
        assertEquals(100, eurAcc.getAmount(), 0.001); /// should stay same
        assertEquals(50, targetAcc.getAmount(), 0.001); /// should stay same
    }

    @Test
    public void testBankAddAndGetClient() {
        Bank bank = new Bank("TestBank");

        /// Using BUILDER to make the code more  readable and cleaner
        Client c = new Client.ClientBuilder("Johhny Silverhand")
                .address("Str 12")
                .account(Account.TYPE.RON, "RON67", 1000)
                .build();

        bank.addClient(c);
        assertNotNull(bank.getClient("Johhny Silverhand"));
    }

    /// test getting a non existent user
    @Test
    public void testGetNonExistentClient() {
        Bank bank = new Bank("TestBank");
        assertNull(bank.getClient("Ghost User"));
    }

    /// singleton test
    @Test
    public void testSingletonPattern() {
        BankConfig config1 = BankConfig.getInstance();
        BankConfig config2 = BankConfig.getInstance();
        /// assertSame checks if they are literally the exact same object in memory
        assertSame(config1, config2);
    }

    /// factory test
    @Test
    public void testFactoryMethodPattern() {
        Account acc = AccountFactory.createAccount(Account.TYPE.EUR, "EUR99", 500);
        /// checks if the factory correctly returned the specific subclass
        assertTrue(acc instanceof EuroAcc);
    }

    /// builder optional attributes test
    @Test
    public void testBuilderPattern() {
        Client c = new Client.ClientBuilder("V")
                .address("Night City")
                .build();

        assertEquals("V", c.getName());
        assertEquals("Night City", c.getAddress());
    }
    ///LAB6
    @Test
    public void testDecoratorPattern() {
        Account normalAcc = new RonAcc("RON99", 100);

        /// wrap in overdraft dec with 500 lim
        Account overdraftAcc = new OverdraftDecorator(normalAcc, 500);

        overdraftAcc.retrieve(200); /// this should fail but dec allows it
        assertEquals(-100, overdraftAcc.getAmount(), 0.001);
    }
    @Test
    public void testCommandPattern() {
        Account acc = new RonAcc("RON100", 500);
        TransactionManager manager = new TransactionManager();

        Command deposit = new DepositCommand(acc, 200);
        manager.executeCommand(deposit);
        assertEquals(700, acc.getAmount(), 0.001);

        manager.undoLast(); /// meant 20 for ex so undo
        assertEquals(500, acc.getAmount(), 0.001); /// back to normal
    }
    @Test
    public void testChainOfResponsibility() {
        Account from = new RonAcc("RON_FROM", 1000);
        Account to = new RonAcc("RON_TO", 0);

        /// snap the chain together
        TransferHandler handler = new BalanceCheckHandler();
        handler.setNext(new LimitCheckHandler());

        /// test1: good transfer
        assertTrue(handler.handle(from, to, 500));

        /// test2: fails balance check
        assertFalse(handler.handle(from, to, 2000));

        /// test3: fails limit check
        from.depose(10000); /// give lots of money
        assertFalse(handler.handle(from, to, 7000)); /// too much money breaks 6700 daily transfer
    }
}