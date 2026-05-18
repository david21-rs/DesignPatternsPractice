package src.ro.uvt.fi.dp;

import java.util.logging.Logger; ///importing javas built in logging tool


public abstract class Account implements Operations, Transfer {
	protected static final Logger logger = Logger.getLogger(Account.class.getName()); ///I put it on protected so i can use it in RonAcc and EuroAcc
	public static enum TYPE {
		EUR, RON
	};

	private String accountCode = null; //IBAN
	///changed from private to protected so can use in overdraftdecorator
	protected double amount = 0;
	private Account.TYPE type = Account.TYPE.RON; ///declaring them private because they were declared with package visibility
	/// and they could be modified by the other classes without using the depose method for example
	/// thus violating low coupling from GRASP

	protected Account(String accountCode, double amount, Account.TYPE type) {
		this.accountCode = accountCode;
		this.type = type;
		///changed this line cus it was crashing the decorator cus it tried to use the wrappedAccount variable before it was actually created
		this.amount = amount;
	}

	@Override
	public double getTotalAmount() {

		return amount + amount * getInterest();
	}

	@Override
	public void depose(double amount) {

		this.amount += amount;
		logger.info("Deposed " + amount + " to account " + accountCode); // Log it
	}

	@Override
	public void retrieve(double amount) {
		///normal accs cant have negative balance anymore
		if (this.amount >= amount) {
			this.amount -= amount;
			logger.info("Retrieved " + amount + " from account " + accountCode);
		} else {
			logger.warning("Insufficient funds for account " + accountCode + ". Retrieve denied.");
		}
	}

	@Override
	public abstract String toString();


	public String getAccountCode() {
		return accountCode;
	}
	/// we use BankConfig now instead of hardcoding the numbers into the get interest method which technically
	/// gives us one less reason to manually change account.java which is good for SRP --> lab2 changes
	public double getInterest() {
		/// grabbing the Singleton instance, now it doesnt just use the class anymore
		/// grabbing the Singleton instance, now it doesnt just use the class anymore
		BankConfig config = BankConfig.getInstance();

		if (Account.TYPE.RON == this.type) {
			if (amount < config.getRonThreshold())
				return config.getRonInterestBelowThreshold();
			else
				return config.getRonInterestAboveThreshold();
		} else {
			return config.getEurInterest();
		}
	}
	///it used to not be abstract and violated OCP because the developer had to modify
	/// the account class thus i moved the logic to the subclasses
	@Override
	public abstract void transfer(Account c, double s);
	/// added missing getters cus now theyre private
	public double getAmount (){
		return amount;
	}
	public Account.TYPE getType(){
		return type;
	}

}
