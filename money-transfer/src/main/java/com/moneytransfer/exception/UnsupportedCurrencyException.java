package main.java.com.moneytransfer.exception;

public class UnsupportedCurrencyException extends Exception {

	private static final long serialVersionUID = 8659616679727726536L;

	public UnsupportedCurrencyException(String message) {
		super(message);
	}

}
