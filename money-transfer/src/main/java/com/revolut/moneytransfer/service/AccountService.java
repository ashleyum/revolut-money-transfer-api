package main.java.com.revolut.moneytransfer.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import main.java.com.revolut.moneytransfer.exception.AccountNotFoundException;
import main.java.com.revolut.moneytransfer.exception.InsufficientBalanceException;
import main.java.com.revolut.moneytransfer.exception.InvalidReceiverException;
import main.java.com.revolut.moneytransfer.exception.UnsupportedCurrencyException;
import main.java.com.revolut.moneytransfer.model.Account;
import main.java.com.revolut.moneytransfer.model.Amount;
import main.java.com.revolut.moneytransfer.repository.AccountRepository;

public class AccountService {
	
	private final AccountRepository accountRepository = new AccountRepository();
	
	private List<String> suppportedCurrencies = 
			Arrays.asList(new String[] { "GBP", "EUR", "USD" });
	
	// currencyRates : 2d matrix storing currency rate information
	// GBP index = 0, EUR index = 1, USD index = 2
	// Ex) GBP to EUR rate = currencyRates[0][1]
	
	private double[][] currencyRates = { 
			new double[] {1, 1.15, 1.31},
			new double[] {0.87, 1, 1.13}, 
			new double[] {0.77, 0.88, 1} };


	public Account searchAccount(Long accountId) throws AccountNotFoundException {
		return accountRepository.getById(accountId);
	}
	
	public Account createAccount(Amount amount) throws UnsupportedCurrencyException {
		
		if (amount.getCurrency() == null || !suppportedCurrencies.contains(amount.getCurrency().toUpperCase())) {
			throw new UnsupportedCurrencyException("Supported currencies are GBP, EUR, and USD.");
		}

		Account account = new Account();
		account.setAmount(amount);

		return accountRepository.add(account);
	}

	public void transferAmount(Long senderId, Long receiverId, Amount amount)
			throws InvalidReceiverException, AccountNotFoundException, UnsupportedCurrencyException, InsufficientBalanceException {

		if (senderId.equals(receiverId)) {
			throw new InvalidReceiverException("Sender and receiver cannot be the same account."); 
		}
		
		Account senderAccount = accountRepository.getById(senderId);
		Account receiverAccount = accountRepository.getById(receiverId);

		Amount senderAmount = senderAccount.getAmount();
		Amount receiverAmount = receiverAccount.getAmount();

		Amount senderNewAmount = calculateAmount(senderAmount, amount, "subtract");
		Amount receiverNewAmount = calculateAmount(receiverAmount, amount, "add");

		senderAccount.setAmount(senderNewAmount);
		accountRepository.update(senderAccount);

		receiverAccount.setAmount(receiverNewAmount);
		accountRepository.update(receiverAccount);
		
	}

	public Amount calculateAmount(Amount oldAmount, Amount changeAmount, String operation)
			throws UnsupportedCurrencyException, InsufficientBalanceException {

		String oldAmountCurrency = oldAmount.getCurrency();
		String changeAmountCurrency = changeAmount.getCurrency();

		BigDecimal oldAmountValue = oldAmount.getValue();
		BigDecimal changeAmountValue = changeAmount.getValue();

		Amount newAmount = new Amount();
		newAmount.setCurrency(oldAmountCurrency);

		BigDecimal newAmountValue;
		if (oldAmountCurrency.equals(changeAmountCurrency)) {
			newAmountValue = "add".equals(operation) ? 
					oldAmountValue.add(changeAmountValue)
					: oldAmountValue.subtract(changeAmountValue);
		} else {
			Amount changeAmountConverted = convertCurrency(changeAmount, oldAmountCurrency);
			newAmountValue = "add".equals(operation) ? 
					oldAmountValue.add(changeAmountConverted.getValue())
					: oldAmountValue.subtract(changeAmountConverted.getValue());
		}

		if (newAmountValue.compareTo(BigDecimal.ZERO) < 0) {
			throw new InsufficientBalanceException("Insufficient balance. Cannot process the request.");
		}

		newAmount.setValue(newAmountValue);

		return newAmount;
	}
	
	public Amount convertCurrency(Amount oldAmount, String newCurrency) throws UnsupportedCurrencyException {
		
		String oldCurrency = oldAmount.getCurrency();
		BigDecimal oldValue = oldAmount.getValue();
		
		double convertRate = currencyRates[getCurrencyInx(oldCurrency)][getCurrencyInx(newCurrency)];
		BigDecimal newValue = oldValue.multiply(new BigDecimal(String.valueOf(convertRate)));
		
		return new Amount(newValue, newCurrency);
	}
	
	private int getCurrencyInx(String currency) throws UnsupportedCurrencyException {
		if ("GBP".equals(currency)) {
			return 0;
		} else if ("EUR".equals(currency)) {
			return 1;
		} else if ("USD".equals(currency)) {
			return 2;
		} else {
			throw new UnsupportedCurrencyException("Supported currencies are GBP, EUR, and USD.");
		}
	}

}
