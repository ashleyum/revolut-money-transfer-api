package main.java.com.revolut.moneytransfer.exception;

public class InvalidReceiverException extends Exception {

	private static final long serialVersionUID = -1458559382090664007L;

	public InvalidReceiverException(String message) {
		super(message);
	}

}
