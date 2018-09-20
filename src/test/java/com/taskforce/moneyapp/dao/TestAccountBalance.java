package com.taskforce.moneyapp.dao;

import com.iyas.transferdemo.common.CustomException;
import com.iyas.transferdemo.dao.AccountDao;
import com.iyas.transferdemo.dao.DaoFactory;

import com.iyas.transferdemo.domain.Account;
import com.iyas.transferdemo.domain.AccountTransaction;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

import static junit.framework.TestCase.assertEquals;

public class TestAccountBalance {

	private static Logger log = Logger.getLogger(TestAccountDAO.class);
	private static final DaoFactory daoFactory = new DaoFactory();
	private static final int THREADS_COUNT = 100;

	@BeforeClass
	public static void setup() {
		daoFactory.populateTestData();
	}

	@Test
	public void testAccountSingleThreadSameCcyTransfer() throws CustomException {

		final AccountDao accountDAO = daoFactory.getAccountDAO();

		BigDecimal transferAmount = new BigDecimal(50.01234).setScale(4, RoundingMode.HALF_EVEN);

		AccountTransaction transaction =
				new AccountTransaction("EUR", transferAmount, 3L, 4L);

		accountDAO.transferAccountBalance(transaction);

		Account accountFrom = accountDAO.getAccountById(3);
		Account accountTo = accountDAO.getAccountById(4);

		assertEquals(0,
				accountFrom.getBalance().compareTo(
						new BigDecimal(449.9877).setScale(4, RoundingMode.HALF_EVEN)));
		assertEquals(accountTo.getBalance(), new BigDecimal(550.0123).setScale(4, RoundingMode.HALF_EVEN));

	}

	@Test
	public void testAccountMultiThreadedTransfer() throws InterruptedException, CustomException {
		final AccountDao accountDAO = daoFactory.getAccountDAO();
		// transfer a total of 200USD from 100USD balance in multi-threaded
		// mode, expect half of the transaction fail
		final CountDownLatch latch = new CountDownLatch(THREADS_COUNT);
		for (int i = 0; i < THREADS_COUNT; i++) {
			new Thread(() -> {
				try {
					AccountTransaction transaction = new AccountTransaction("USD",
							new BigDecimal(2).setScale(4, RoundingMode.HALF_EVEN), 1L, 2L);
					accountDAO.transferAccountBalance(transaction);
				} catch (Exception e) {
					log.error("Error occurred during transfer ", e);
				} finally {
					latch.countDown();
				}
			}).start();
		}

		latch.await();

		Account accountFrom = accountDAO.getAccountById(1);
		Account accountTo = accountDAO.getAccountById(2);

		assertEquals(accountFrom.getBalance(), new BigDecimal(0).setScale(4, RoundingMode.HALF_EVEN));
		assertEquals(accountTo.getBalance(), new BigDecimal(300).setScale(4, RoundingMode.HALF_EVEN));

	}

	@Test
	public void testTransferFailOnDBLock() throws CustomException, SQLException {
		final String SQL_LOCK_ACC = "SELECT * FROM Account WHERE AccountId = 5 FOR UPDATE";
		Connection conn = null;
		PreparedStatement lockStmt = null;
		ResultSet rs = null;
		Account fromAccount = null;

		try {
			conn = DaoFactory.getConnection();
			conn.setAutoCommit(false);
			// lock account for writing:
			lockStmt = conn.prepareStatement(SQL_LOCK_ACC);
			rs = lockStmt.executeQuery();
			if (rs.next()) {
				fromAccount = new Account(rs.getLong("AccountId"), rs.getString("UserName"),
						rs.getBigDecimal("Balance"), rs.getString("CurrencyCode"));
			}

			if (fromAccount == null) {
				throw new CustomException("Locking error during test, SQL = " + SQL_LOCK_ACC);
			}
			// after lock account 5, try to transfer from account 6 to 5
			// default h2 timeout for acquire lock is 1sec
			BigDecimal transferAmount = new BigDecimal(50).setScale(4, RoundingMode.HALF_EVEN);

			AccountTransaction transaction = new AccountTransaction("GBP", transferAmount, 6L, 5L);
			daoFactory.getAccountDAO().transferAccountBalance(transaction);
			conn.commit();
		} catch (Exception e) {
			log.error("Exception occurred, initiate a rollback");
			try {
				if (conn != null)
					conn.rollback();
			} catch (SQLException re) {
				log.error("Fail to rollback transaction", re);
			}
		} finally {
			DbUtils.closeQuietly(conn);
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(lockStmt);
		}

		// now inspect account 3 and 4 to verify no transaction occurred
		BigDecimal originalBalance = new BigDecimal(500).setScale(4, RoundingMode.HALF_EVEN);
		assertEquals(daoFactory.getAccountDAO().getAccountById(6).getBalance(), originalBalance);
		assertEquals(daoFactory.getAccountDAO().getAccountById(5).getBalance(), originalBalance);
	}

}
