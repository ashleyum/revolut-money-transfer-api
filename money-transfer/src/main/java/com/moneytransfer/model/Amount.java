package main.java.com.moneytransfer.model;

import java.math.BigDecimal;

public class Amount {
	
	private BigDecimal value;
	private String currency;
	
	public Amount() {
		super();
	}

	public Amount(BigDecimal value, String currency) {
		super();
		this.value = value;
		this.currency = currency;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	@Override
	public String toString() {
		return this.value + " (" + this.currency + ")";
	}

}
