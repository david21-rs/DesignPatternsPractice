package src.ro.uvt.fi.dp;
/// bank was using client class directly for its array methods which violated DIP from SOLID
///  since a high level module was dependant on a concrete implementation
///  and if the developer wanted to add dif types of clients he had to change bank class ( bad )
/// therefore i made an interface for client that all clients implement and made bank use it
import java.util.Arrays;
import java.util.ArrayList;
import java.util.logging.Logger; ///importing javas built in logging tool

public class Bank {
	private static final Logger logger = Logger.getLogger(Bank.class.getName());///creating the logger instance
	private ArrayList<InterfCLient> clients; ///Change the ArayList from hardcoded number
	private int clientsNumber;
	private String bankCode = null;

	public Bank(String codBanca) {
		this.bankCode = codBanca;
		clients = new ArrayList<>(); ///initialize dynamycally
	}

	public void addClient(InterfCLient c) {
		clients.add(c);
		clientsNumber++;
		/// adding the event to logs:
		logger.info("New client has joined our bank. Welcome our beloved client: " + c.getName());

	}

	
	public InterfCLient getClient(String nume) {
		for (int i = 0; i < clientsNumber; i++) {
			if (clients.get(i).getName().equals(nume)) {
				return clients.get(i);
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "Bank [code=" + bankCode + ", clients=" + clients + "]";
	}

}
