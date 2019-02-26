package main.java.com.revolut.moneytransfer.exception;

public class InsufficientBalanceException extends Exception {

	private static final long serialVersionUID = -417891453747661077L;

	public InsufficientBalanceException(String message) {
		super(message);
	}

}
