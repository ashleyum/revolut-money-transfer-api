package main.java.com.revolut.moneytransfer.model;

public class Account {

	private Long id;
	private Amount amount;
	
	public Account() {
		super();
	}

	public Account(Amount amount) {
		super();
		this.amount = amount;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Amount getAmount() {
		return amount;
	}

	public void setAmount(Amount amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "Account { id=" + this.id + ", amount=" + this.amount + " }";
	}

}
