package main.java.com.revolut.moneytransfer.exception;

public class AccountNotFoundException extends Exception {

	private static final long serialVersionUID = 1527022626320247512L;

	public AccountNotFoundException(String message) {
		super(message);
	}

}
