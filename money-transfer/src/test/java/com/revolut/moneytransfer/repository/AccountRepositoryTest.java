package test.java.com.revolut.moneytransfer.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import main.java.com.revolut.moneytransfer.exception.AccountNotFoundException;
import main.java.com.revolut.moneytransfer.model.Account;
import main.java.com.revolut.moneytransfer.model.Amount;
import main.java.com.revolut.moneytransfer.repository.AccountRepository;

public class AccountRepositoryTest {

	private AccountRepository accountRepository;

	@Before
	public void setUp() {
		accountRepository = new AccountRepository();
	}
	
	@Test (expected = AccountNotFoundException.class)
	public void getById_NonexistentId_AccountNotFoundException() throws AccountNotFoundException {
		
		accountRepository.getById(5000000L);
		
	}

	@Test
	public void addAndGetById_NormalInputs_Ok() throws AccountNotFoundException {

		Amount amount = new Amount(new BigDecimal("1000"), "GBP");

		Account createdAccount = accountRepository.add(new Account(amount));
		assertNotNull(createdAccount);

		Account searchedAccount = accountRepository.getById(createdAccount.getId());
		assertNotNull(searchedAccount);

		assertEquals(createdAccount.getAmount().getValue(), searchedAccount.getAmount().getValue());
		assertEquals(createdAccount.getAmount().getCurrency(), searchedAccount.getAmount().getCurrency());

	}

	@Test
	public void addAndUpdate_NormalInputs_Ok() throws AccountNotFoundException {

		Amount amount = new Amount(new BigDecimal("2000"), "GBP");

		Account account = accountRepository.add(new Account(amount));
		assertNotNull(account);

		Amount newAmount = new Amount(new BigDecimal("3000"), "EUR");
		account.setAmount(newAmount);

		accountRepository.update(account);

		Account updatedAccount = accountRepository.getById(account.getId());
		assertNotNull(updatedAccount);

		assertEquals(updatedAccount.getAmount().getValue(), newAmount.getValue());
		assertEquals(updatedAccount.getAmount().getCurrency(), newAmount.getCurrency());

	}

}
