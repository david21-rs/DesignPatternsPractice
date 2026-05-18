package src.ro.uvt.fi.dp;

import java.util.Arrays;
import java.util.ArrayList;

import src.ro.uvt.fi.dp.Account.TYPE;

public class Client implements InterfCLient {
	private String name;
	private String address;
	private ArrayList<Account> accounts;
	private int accountsNo = 0;
	/// made an account obj here and passed it to addAccount
	/// BUILDER PATTERN: we use it to build the client so that the object gets built when .build() is called so its never in an incomplete state
	public static class ClientBuilder {
		private String name;
		private String address = "IDK"; /// Optional default value
		private ArrayList<Account> accounts = new ArrayList<>();

		public ClientBuilder(String name) {
			this.name = name; /// you must put name
		}

		public ClientBuilder address(String address) {
			this.address = address; /// this is otpional chaining
			return this;
		}

		public ClientBuilder account(TYPE tip, String numarCont, double suma) {
			/// using the FACTORY METHOD here instead of the 'new' keyword directly
			accounts.add(AccountFactory.createAccount(tip, numarCont, suma));
			return this;
		}

		public Client build() {
			return new Client(this);
		}
	}

	/// changed to private to force developers do use builder
	private Client(ClientBuilder builder) {
		this.name = builder.name;
		this.address = builder.address;
		this.accounts = builder.accounts;
		this.accountsNo = builder.accounts.size();
	}

	public void addAccount(Account account) {
			accounts.add(account);
			accountsNo++;

	}
	///CLient class was making the accounts directly in this method with new instead of receiving an ccount object
	///meaning if developers wanted to use stuff like RonAcc or EuroAcc they had to modify Client class too
	///plus creating them in the method was going against DIP from SOLID
	public Account getAccount(String accountCode) {
		for (int i = 0; i < accountsNo; i++) {
			if (accounts.get(i).getAccountCode().equals(accountCode)) {
				return accounts.get(i);
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "\n\tClient [name=" + name + ", address=" + address + ", acounts=" + accounts + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}
}
