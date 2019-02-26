package test.java.com.revolut.moneytransfer.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import main.java.com.revolut.moneytransfer.exception.AccountNotFoundException;
import main.java.com.revolut.moneytransfer.exception.InsufficientBalanceException;
import main.java.com.revolut.moneytransfer.exception.InvalidReceiverException;
import main.java.com.revolut.moneytransfer.exception.UnsupportedCurrencyException;
import main.java.com.revolut.moneytransfer.model.Account;
import main.java.com.revolut.moneytransfer.model.Amount;
import main.java.com.revolut.moneytransfer.service.AccountService;

public class AccountServiceTest {

	private AccountService accountService;

	@Before
	public void setUp() {
		accountService = new AccountService();
	}

	@Test(expected = AccountNotFoundException.class)
	public void searchAccount_NonexistentId_AccountNotFoundException() throws AccountNotFoundException {

		accountService.searchAccount(5000000L);

	}

	@Test(expected = UnsupportedCurrencyException.class)
	public void createAccount_Noccurency_UnsupportedCurrencyException() throws UnsupportedCurrencyException {

		Amount amount = new Amount();
		amount.setValue(new BigDecimal("1000"));
		accountService.createAccount(amount);

	}

	@Test(expected = UnsupportedCurrencyException.class)
	public void createAccount_UnsupportedCurrency_UnsupportedCurrencyException() throws UnsupportedCurrencyException {

		Amount amount = new Amount(new BigDecimal("1000"), "CAD");
		accountService.createAccount(amount);

	}
	
	@Test
	public void createAndSearchAccount_NormalInputs_Ok() throws UnsupportedCurrencyException, AccountNotFoundException {

		Amount amount = new Amount(new BigDecimal("1000"), "GBP");
		Account createdAccount = accountService.createAccount(amount);
		assertNotNull(createdAccount);

		Account searchedAccount = accountService.searchAccount(createdAccount.getId());
		assertNotNull(searchedAccount);

		assertEquals(createdAccount.getAmount().getValue(), searchedAccount.getAmount().getValue());
		assertEquals(createdAccount.getAmount().getCurrency(), searchedAccount.getAmount().getCurrency());

	}

	@Test(expected = UnsupportedCurrencyException.class)
	public void convertCurrency_UnsupportedCurrency_UnsupportedCurrencyException() throws UnsupportedCurrencyException {

		Amount oldAmount = new Amount(new BigDecimal("100"), "GBP");
		accountService.convertCurrency(oldAmount, "CAD");

	}
	
	@Test
	public void converCurrency_NormalInputs_Ok() throws UnsupportedCurrencyException {

		Amount oldAmount = new Amount(new BigDecimal("100"), "GBP");

		// £100 * gbpToEurRate(==1.15) = €115
		Amount eurAmount = accountService.convertCurrency(oldAmount, "EUR");
		assertNotNull(eurAmount);
		assertEquals(eurAmount.getValue().compareTo(new BigDecimal("115")), 0);
		assertEquals(eurAmount.getCurrency(), "EUR");

		// £100 * gbpToUsdRate(==1.31) = $131
		Amount usdAmount = accountService.convertCurrency(oldAmount, "USD");
		assertNotNull(usdAmount);
		assertEquals(usdAmount.getValue().compareTo(new BigDecimal("131")), 0);
		assertEquals(usdAmount.getCurrency(), "USD");

	}
	
	@Test(expected = UnsupportedCurrencyException.class)
	public void calculateAmount_UnsupportedCurrency_UnsupportedCurrencyException()
			throws UnsupportedCurrencyException, InsufficientBalanceException {

		Amount oldAmount = new Amount(new BigDecimal("500"), "GBP");
		Amount changeAmount = new Amount(new BigDecimal("300"), "CAD");
		String operation = "add";

		accountService.calculateAmount(oldAmount, changeAmount, operation);

	}

	@Test(expected = InsufficientBalanceException.class)
	public void calculateAmount_SubtractMoreThanBalance_InsufficientBalanceException()
			throws UnsupportedCurrencyException, InsufficientBalanceException {

		Amount oldAmount = new Amount(new BigDecimal("500"), "GBP");
		Amount changeAmount = new Amount(new BigDecimal("501"), "GBP");
		String operation = "subtract";

		accountService.calculateAmount(oldAmount, changeAmount, operation);

	}

	@Test
	public void calculateAmount_SameCurrency_Ok()
			throws UnsupportedCurrencyException, InsufficientBalanceException {

		Amount oldAmount = new Amount(new BigDecimal("1000"), "GBP");

		Amount changeAmount = new Amount(new BigDecimal("300"), "GBP");
		String operation = "add";

		// oldAmount(£1000) + changeAmount(£300) = £1300
		Amount addedAmount = accountService.calculateAmount(oldAmount, changeAmount, operation);

		assertNotNull(addedAmount);
		assertEquals(addedAmount.getValue().compareTo(new BigDecimal("1300")), 0);
		assertEquals(addedAmount.getCurrency(), "GBP");

		// oldAmount(£1000) - changeAmount(£200) = £800
		changeAmount = new Amount(new BigDecimal("200"), "GBP");
		operation = "subtract";

		Amount subtractedAmount = accountService.calculateAmount(oldAmount, changeAmount, operation);

		assertNotNull(subtractedAmount);
		assertEquals(subtractedAmount.getValue().compareTo(new BigDecimal("800")), 0);
		assertEquals(subtractedAmount.getCurrency(), "GBP");

	}

	@Test
	public void calculateAmount_DifferentCurrency_Ok()
			throws UnsupportedCurrencyException, InsufficientBalanceException {

		Amount oldAmount = new Amount(new BigDecimal("1000"), "GBP");

		// oldAmount(£1000) + changeAmount(€100 == £87) = £1087
		Amount changeAmount = new Amount(new BigDecimal("100"), "EUR");
		String operation = "add";
		Amount addedAmount = accountService.calculateAmount(oldAmount, changeAmount, operation);

		assertNotNull(addedAmount);
		assertEquals(addedAmount.getValue().compareTo(new BigDecimal("1087")), 0);
		assertEquals(addedAmount.getCurrency(), "GBP");

		// oldAmount(£1000) - changeAmount($100 == £77) = £923
		changeAmount = new Amount(new BigDecimal("100"), "USD");
		operation = "subtract";
		Amount subtractedAmount = accountService.calculateAmount(oldAmount, changeAmount, operation);

		assertNotNull(subtractedAmount);
		assertEquals(subtractedAmount.getValue().compareTo(new BigDecimal("923")), 0);
		assertEquals(subtractedAmount.getCurrency(), "GBP");

	}
	
	@Test(expected = InvalidReceiverException.class)
	public void transferAmount_SameSenderAndReceiver_InvalidReceiverException() throws InvalidReceiverException, AccountNotFoundException,
			UnsupportedCurrencyException, InsufficientBalanceException {

		Amount senderAmount = new Amount(new BigDecimal("300"), "GBP");
		Account senderAccount = accountService.createAccount(senderAmount);
		assertNotNull(senderAccount);

		Long senderId = senderAccount.getId();
		Amount transferAmount = new Amount(new BigDecimal("100"), "GBP");

		accountService.transferAmount(senderId, senderId, transferAmount);

	}

	@Test(expected = AccountNotFoundException.class)
	public void transferAmount_NonexistentId_AccountNotFoundException() throws InvalidReceiverException, AccountNotFoundException,
			UnsupportedCurrencyException, InsufficientBalanceException {

		Amount senderAmount = new Amount(new BigDecimal("300"), "GBP");
		Account senderAccount = accountService.createAccount(senderAmount);
		assertNotNull(senderAccount);

		Long senderId = senderAccount.getId();
		Long receiverId = 5000000L;
		Amount transferAmount = new Amount(new BigDecimal("100"), "GBP");

		accountService.transferAmount(senderId, receiverId, transferAmount);

	}

	@Test(expected = UnsupportedCurrencyException.class)
	public void transferAmount_UnsupportedCurrency_UnsupportedCurrencyException() throws InvalidReceiverException, AccountNotFoundException,
			UnsupportedCurrencyException, InsufficientBalanceException {

		Amount senderAmount = new Amount(new BigDecimal("300"), "GBP");
		Account senderAccount = accountService.createAccount(senderAmount);
		assertNotNull(senderAccount);

		Amount receiverAmount = new Amount(new BigDecimal("200"), "GBP");
		Account receiverAccount = accountService.createAccount(receiverAmount);
		assertNotNull(receiverAccount);

		Long senderId = senderAccount.getId();
		Long receiverId = receiverAccount.getId();
		Amount transferAmount = new Amount(new BigDecimal("100"), "CAD");

		accountService.transferAmount(senderId, receiverId, transferAmount);

	}

	@Test(expected = InsufficientBalanceException.class)
	public void transferAmount_SendMoreThanBalance_InsufficientBalanceException() throws InvalidReceiverException, AccountNotFoundException,
			UnsupportedCurrencyException, InsufficientBalanceException {

		Amount senderAmount = new Amount(new BigDecimal("300"), "GBP");
		Account senderAccount = accountService.createAccount(senderAmount);
		assertNotNull(senderAccount);

		Amount receiverAmount = new Amount(new BigDecimal("500"), "GBP");
		Account receiverAccount = accountService.createAccount(receiverAmount);
		assertNotNull(receiverAccount);

		Long senderId = senderAccount.getId();
		Long receiverId = receiverAccount.getId();
		Amount transferAmount = new Amount(new BigDecimal("350"), "GBP");

		accountService.transferAmount(senderId, receiverId, transferAmount);

	}
	
	public void transferAmount_NormalInputs_Ok() throws InvalidReceiverException, AccountNotFoundException,
			UnsupportedCurrencyException, InsufficientBalanceException {

		Amount senderAmount = new Amount(new BigDecimal("1000"), "GBP");
		Account senderAccount = accountService.createAccount(senderAmount);
		assertNotNull(senderAccount);

		Amount receiverAmount = new Amount(new BigDecimal("500"), "GBP");
		Account receiverAccount = accountService.createAccount(receiverAmount);
		assertNotNull(receiverAccount);

		// (1) transfer 200 GBP
		Long senderId = senderAccount.getId();
		Long receiverId = receiverAccount.getId();
		Amount transferAmount = new Amount(new BigDecimal("200"), "GBP");

		accountService.transferAmount(senderId, receiverId, transferAmount);

		// sender amount before transfer(£1000) - transfer amount(£200) = £800
		Account senderAccountUpdated = accountService.searchAccount(senderId);

		assertEquals(senderAccountUpdated.getAmount().getValue().compareTo(new BigDecimal("800")), 0);
		assertEquals(senderAccountUpdated.getAmount().getCurrency(), "GBP");

		// receiver amount before transfer(£500) + transfer amount(£200) = £700
		Account receiverAccountUpdated = accountService.searchAccount(receiverId);

		assertEquals(receiverAccountUpdated.getAmount().getValue().compareTo(new BigDecimal("700")), 0);
		assertEquals(receiverAccountUpdated.getAmount().getCurrency(), "GBP");

		// (2) transfer 300 EUR (== 261 GBP)
		transferAmount = new Amount(new BigDecimal("300"), "EUR");
		accountService.transferAmount(senderId, receiverId, transferAmount);

		// sender amount before transfer(£800) - transfer amount(€300 == £261) = £539
		senderAccountUpdated = accountService.searchAccount(senderId);

		assertEquals(senderAccountUpdated.getAmount().getValue().compareTo(new BigDecimal("539")), 0);
		assertEquals(senderAccountUpdated.getAmount().getCurrency(), "GBP");

		// receiver amount before transfer(£700) + transfer amount(€300 == £261) = £961
		receiverAccountUpdated = accountService.searchAccount(receiverId);

		assertEquals(receiverAccountUpdated.getAmount().getValue().compareTo(new BigDecimal("961")), 0);
		assertEquals(receiverAccountUpdated.getAmount().getCurrency(), "GBP");

		// (3) transfer 100 USD (== 77 GBP)
		transferAmount = new Amount(new BigDecimal("100"), "USD");
		accountService.transferAmount(senderId, receiverId, transferAmount);

		// sender amount before transfer(£539) - transfer amount($100 == £77) = £461
		senderAccountUpdated = accountService.searchAccount(senderId);

		assertEquals(senderAccountUpdated.getAmount().getValue().compareTo(new BigDecimal("462")), 0);
		assertEquals(senderAccountUpdated.getAmount().getCurrency(), "GBP");

		// receiver amount before transfer(£961) + transfer amount($100 == £77) = £1134
		receiverAccountUpdated = accountService.searchAccount(receiverId);

		assertEquals(receiverAccountUpdated.getAmount().getValue().compareTo(new BigDecimal("1134")), 0);
		assertEquals(receiverAccountUpdated.getAmount().getCurrency(), "GBP");

	}

}
