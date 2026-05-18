package src.ro.uvt.fi.dp;

public class Test {

	public static void main(String[] args) {
		/**
		 * Create BCR bank with 2 clients
		 */
		Bank bcr = new Bank("BCR Bank");
		// Client Ionescu has an EUR and a RON account
		/// swapped this to use the builder otherwise it throws errors cus the constructor is private now
		Client cl1 = new Client.ClientBuilder("Ionescu Ion")
				.address("Timisoara")
				.account(Account.TYPE.EUR, "EUR124", 200.9)
				.build();
		bcr.addClient(cl1);

		/// using the factory here instead of raw new keyword
		cl1.addAccount(AccountFactory.createAccount(Account.TYPE.RON, "RON1234", 400));

		// Client Marinescu has a RON account
		/// builder stuff again for marinescu
		Client cl2 = new Client.ClientBuilder("Marinescu Marin")
				.address("Timisoara")
				.account(Account.TYPE.RON, "RON126", 100)
				.build();
		bcr.addClient(cl2);
		System.out.println(bcr);

		/**
		 * Create bank CEC with one client
		 */
		Bank cec = new Bank("CEC Bank");

		/// building vasilescu with builder too
		Client clientCEC = new Client.ClientBuilder("Vasilescu Vasile")
				.address("Brasov")
				.account(Account.TYPE.EUR, "EUR128", 700)
				.build();
		cec.addClient(clientCEC);
		System.out.println(cec);

		/**
		 * Perform operations on client accounts
		 */
		// depose in account RON126 of client Marinescu
		InterfCLient cl = bcr.getClient("Marinescu Marin");
		if (cl != null) {
			cl.getAccount("RON126").depose(400);
			System.out.println(cl);
		}

		// retrieve from account RON126 of Marinescu client
		if (cl != null) {
			cl.getAccount("RON126").retrieve(67);
			System.out.println(cl);
		}

		// transfer between accounts RON126 and RON1234
		Account a1 = cl.getAccount("RON126");
		Account a2 = bcr.getClient("Ionescu Ion").getAccount("RON1234");
		a1.transfer(a2, 40);
		System.out.println(bcr);

	}
}