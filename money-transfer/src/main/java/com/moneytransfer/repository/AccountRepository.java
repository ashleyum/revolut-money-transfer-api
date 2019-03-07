package main.java.com.moneytransfer.repository;

import java.util.LinkedHashMap;
import java.util.Map;

import main.java.com.moneytransfer.exception.AccountNotFoundException;
import main.java.com.moneytransfer.model.Account;

public class AccountRepository {

	private Map<Long, Account> accounts;
	private Long accountId;

	public AccountRepository() {
		this.accounts = new LinkedHashMap<>();
		this.accountId = 1000000L;
	}

	public Account add(Account account) {
		account.setId(this.accountId++);
		this.accounts.put(account.getId(), account);

		return account;
	}

	public void update(Account account) {
		this.accounts.replace(account.getId(), account);
	}

	public Account getById(Long id) throws AccountNotFoundException {

		if (accounts.get(id) == null) {
			throw new AccountNotFoundException("Account cannot be found - " + id);
		}
		return accounts.get(id);

	}

}
