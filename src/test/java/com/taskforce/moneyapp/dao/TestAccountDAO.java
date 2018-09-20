package com.taskforce.moneyapp.dao;

import com.iyas.transferdemo.common.CustomException;
import com.iyas.transferdemo.dao.DaoFactory;
import com.iyas.transferdemo.domain.Account;

import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static junit.framework.TestCase.*;

public class TestAccountDAO {

	private static final DaoFactory daoFactory = new DaoFactory();

	@BeforeClass
	public static void setup() {
		daoFactory.populateTestData();
	}

	@Test
	public void testGetAllAccounts() throws CustomException {
		List<Account> allAccounts = daoFactory.getAccountDAO().getAllAccounts();
		assertTrue(allAccounts.size() > 1);
	}

	@Test
	public void testGetAccount() throws CustomException {
		Account account = daoFactory.getAccountDAO().getAccountById(1L);
		assertEquals("iyas", account.getUserName());
	}

	@Test
	public void testGetNonExistingAccount() throws CustomException {
		Account account = daoFactory.getAccountDAO().getAccountById(100L);
		assertNull(account);
	}

	@Test
	public void testCreateAccount() throws CustomException {
		BigDecimal balance = new BigDecimal(10).setScale(4, RoundingMode.HALF_EVEN);
		Account a = new Account("test3", balance, "GBP");
		long aid = daoFactory.getAccountDAO().createAccount(a);
		Account afterCreation = daoFactory.getAccountDAO().getAccountById(aid);
		assertEquals("test3", afterCreation.getUserName());
		assertEquals("GBP", afterCreation.getCurrencyCode());
		assertEquals(afterCreation.getBalance(), balance);
	}

	@Test
	public void testUpdateAccountBalanceSufficientFund() throws CustomException {
		BigDecimal deltaDeposit = new BigDecimal(50).setScale(4, RoundingMode.HALF_EVEN);
		BigDecimal afterDeposit = new BigDecimal(150).setScale(4, RoundingMode.HALF_EVEN);
		int rowsUpdated = daoFactory.getAccountDAO().updateAccountBalance(1L, deltaDeposit);
		assertEquals(1, rowsUpdated);
		assertEquals(daoFactory.getAccountDAO().getAccountById(1L).getBalance(), afterDeposit);

		BigDecimal deltaWithdraw = new BigDecimal(-50).setScale(4, RoundingMode.HALF_EVEN);
		BigDecimal afterWithdraw = new BigDecimal(100).setScale(4, RoundingMode.HALF_EVEN);
		int rowsUpdatedW = daoFactory.getAccountDAO().updateAccountBalance(1L, deltaWithdraw);
		assertEquals(1, rowsUpdatedW);
		assertEquals(daoFactory.getAccountDAO().getAccountById(1L).getBalance(), afterWithdraw);

	}

	@Test(expected = CustomException.class)
	public void testUpdateAccountBalanceNotEnoughFund() throws CustomException {
		BigDecimal deltaWithdraw = new BigDecimal(-50000).setScale(4, RoundingMode.HALF_EVEN);
		int rowsUpdatedW = daoFactory.getAccountDAO().updateAccountBalance(1L, deltaWithdraw);
		assertEquals(0, rowsUpdatedW);
	}

}