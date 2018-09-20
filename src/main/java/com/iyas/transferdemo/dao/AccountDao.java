package com.iyas.transferdemo.dao;

import com.iyas.transferdemo.common.CustomException;
import com.iyas.transferdemo.common.Utils;
import com.iyas.transferdemo.domain.Account;
import com.iyas.transferdemo.domain.AccountTransaction;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountDao {

    private static Logger log = Logger.getLogger(AccountDao.class);
    private final static String SQL_LOCK_ACC_BY_ID = "SELECT * FROM Account WHERE AccountId = ? FOR UPDATE";
    private final static String SQL_CREATE_ACC = "INSERT INTO Account (UserName, Balance, CurrencyCode) VALUES (?, ?, ?)";
    private final static String SQL_UPDATE_ACC_BALANCE = "UPDATE Account SET Balance = ? WHERE AccountId = ? ";
    private final static String SQL_GET_ALL_ACC = "SELECT * FROM Account";

    /**
     * Get all accounts.
     */
    public List<Account> getAllAccounts() throws CustomException {
        Connection conn = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<Account> allAccounts = new ArrayList<>();
        try {
            conn = DaoFactory.getConnection();
            prepStmt = conn.prepareStatement(SQL_GET_ALL_ACC);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                Account acc = new Account(rs.getLong("AccountId"), rs.getString("UserName"),
                        rs.getBigDecimal("Balance"), rs.getString("CurrencyCode"));
                allAccounts.add(acc);
            }
            return allAccounts;
        } catch (SQLException e) {
            throw new CustomException("getAccountById: Error reading account data", e);
        } finally {
            DbUtils.closeQuietly(conn, prepStmt, rs);
        }
    }

    /**
     * Get account by ID
     */
    public Account getAccountById(long accountId) throws CustomException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Account acc = null;
        try {
            conn = DaoFactory.getConnection();
            stmt = conn.prepareStatement("SELECT * FROM Account WHERE AccountId = ? ");
            stmt.setLong(1, accountId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                acc = new Account(rs.getLong("AccountId"), rs.getString("UserName"),
                        rs.getBigDecimal("Balance"), rs.getString("CurrencyCode"));
            }
            return acc;
        } catch (SQLException e) {
            throw new CustomException("getAccountById: Error reading account data", e);
        } finally {
            DbUtils.closeQuietly(conn, stmt, rs);
        }

    }

    /**
     * Create account
     */
    public long createAccount(Account account) throws CustomException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        try {
            conn = DaoFactory.getConnection();
            stmt = conn.prepareStatement(SQL_CREATE_ACC);
            stmt.setString(1, account.getUserName());
            stmt.setBigDecimal(2, account.getBalance());
            stmt.setString(3, account.getCurrencyCode());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                log.error("createAccount: Creating account failed, no rows affected.");
                throw new CustomException("Account Cannot be created");
            }
            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            } else {
                log.error("Creating account failed, no ID obtained.");
                throw new CustomException("Account Cannot be created");
            }
        } catch (SQLException e) {
            log.error("Error Inserting Account " + account);
            throw new CustomException("createAccount: Error creating user account " + account, e);
        } finally {
            DbUtils.closeQuietly(conn, stmt, generatedKeys);
        }
    }

    /**
     * Update account balance
     */
    public int updateAccountBalance(long accountId, BigDecimal deltaAmount) throws CustomException {
        Connection conn = null;
        PreparedStatement lockStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        Account targetAccount = null;
        int updateCount = -1;
        try {
            conn = DaoFactory.getConnection();
            conn.setAutoCommit(false);

            // lock account for writing:
            lockStmt = conn.prepareStatement(SQL_LOCK_ACC_BY_ID);
            lockStmt.setLong(1, accountId);
            rs = lockStmt.executeQuery();
            if (rs.next()) {
                targetAccount = new Account(rs.getLong("AccountId"), rs.getString("UserName"),
                        rs.getBigDecimal("Balance"), rs.getString("CurrencyCode"));
            }

            if (targetAccount == null) {
                throw new CustomException("updateAccountBalance: fail to lock account : " + accountId);
            }

            // update account upon success locking
            BigDecimal balance = targetAccount.getBalance().add(deltaAmount);
            if (balance.compareTo(Utils.zeroAmount) < 0) {
                throw new CustomException("Not sufficient Fund for account: " + accountId);
            }

            updateStmt = conn.prepareStatement(SQL_UPDATE_ACC_BALANCE);
            updateStmt.setBigDecimal(1, balance);
            updateStmt.setLong(2, accountId);
            updateCount = updateStmt.executeUpdate();
            conn.commit();

            return updateCount;

        } catch (SQLException se) {
            // rollback transaction if exception occurs
            log.error("updateAccountBalance: Account Transaction Failed, rollback initiated for: " + accountId, se);
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException re) {
                throw new CustomException("Fail to rollback transaction", re);
            }
        } finally {
            DbUtils.closeQuietly(conn);
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(lockStmt);
            DbUtils.closeQuietly(updateStmt);
        }
        return updateCount;
    }

    /**
     * Transfer balance between two accounts.
     */
    public int transferAccountBalance(AccountTransaction accountTransaction) throws CustomException {
        int result = -1;
        Connection conn = null;
        PreparedStatement lockStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        Account fromAccount = null;
        Account toAccount = null;

        try {
            conn = DaoFactory.getConnection();
            conn.setAutoCommit(false);
            // lock the credit and debit account for writing:
            lockStmt = conn.prepareStatement(SQL_LOCK_ACC_BY_ID);
            lockStmt.setLong(1, accountTransaction.getFromAccountId());
            rs = lockStmt.executeQuery();
            if (rs.next()) {
                fromAccount = new Account(rs.getLong("AccountId"), rs.getString("UserName"),
                        rs.getBigDecimal("Balance"), rs.getString("CurrencyCode"));
            }
            lockStmt = conn.prepareStatement(SQL_LOCK_ACC_BY_ID);
            lockStmt.setLong(1, accountTransaction.getToAccountId());
            rs = lockStmt.executeQuery();
            if (rs.next()) {
                toAccount = new Account(rs.getLong("AccountId"), rs.getString("UserName"), rs.getBigDecimal("Balance"),
                        rs.getString("CurrencyCode"));
            }

            // check locking status
            if (fromAccount == null || toAccount == null) {
                throw new CustomException("Fail to lock both accounts for write");
            }

            // check transaction currency
            if (!fromAccount.getCurrencyCode().equals(accountTransaction.getCurrencyCode())) {
                throw new CustomException(
                        "Fail to transfer Fund, transaction ccy are different from source/destination");
            }

            // check ccy is the same for both accounts
            if (!fromAccount.getCurrencyCode().equals(toAccount.getCurrencyCode())) {
                throw new CustomException(
                        "Fail to transfer Fund, the source and destination account are in different currency");
            }

            // check enough fund in source account
            BigDecimal fromAccountLeftOver = fromAccount.getBalance().subtract(accountTransaction.getAmount());
            if (fromAccountLeftOver.compareTo(Utils.zeroAmount) < 0)
                throw new CustomException("Not enough Fund from source Account ");

            // proceed with update
            updateStmt = conn.prepareStatement(SQL_UPDATE_ACC_BALANCE);
            updateStmt.setBigDecimal(1, fromAccountLeftOver);
            updateStmt.setLong(2, accountTransaction.getFromAccountId());
            updateStmt.addBatch();
            updateStmt.setBigDecimal(1, toAccount.getBalance().add(accountTransaction.getAmount()));
            updateStmt.setLong(2, accountTransaction.getToAccountId());
            updateStmt.addBatch();
            int[] rowsUpdated = updateStmt.executeBatch();
            result = rowsUpdated[0] + rowsUpdated[1];
            // If there is no error, commit the transaction
            conn.commit();
        } catch (SQLException se) {
            // rollback transaction if exception occurs
            log.error("transferAccountBalance: Account Transaction Failed, rollback initiated for: " + accountTransaction,
                    se);
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException re) {
                throw new CustomException("Fail to rollback transaction", re);
            }
        } finally {
            DbUtils.closeQuietly(conn);
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(lockStmt);
            DbUtils.closeQuietly(updateStmt);
        }
        return result;
    }

}
