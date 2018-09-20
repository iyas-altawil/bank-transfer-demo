package com.taskforce.moneyapp.services;


import com.iyas.transferdemo.domain.AccountTransaction;
import org.apache.http.HttpStatus;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

public class TestTransactionService extends TestService {

    @Test
    public void testDeposit() {
        given().expect()
                .response().statusCode(HttpStatus.SC_OK)
                .when().put(getUrl("/account/1/deposit/100"));
    }


    @Test
    public void testWithDrawSufficientFund() {
        given().expect()
                .response().statusCode(HttpStatus.SC_OK)
                .when().put(getUrl("/account/2/withdraw/100"));
    }


    @Test
    public void testWithDrawNonSufficientFund() {
        given().expect()
                .response().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .and().body(containsString("Not sufficient Fund"))
                .when().put(getUrl("/account/2/withdraw/1000"));
    }


    @Test
    public void testTransactionEnoughFund() {
        BigDecimal amount = new BigDecimal(10).setScale(4, RoundingMode.HALF_EVEN);
        AccountTransaction transaction = new AccountTransaction("EUR", amount, 3L, 4L);

        given().expect()
                .response().statusCode(HttpStatus.SC_OK)
                .when().post(getUrl("/transaction"), transaction);
    }


    @Test
    public void testTransactionNotEnoughFund() {
        BigDecimal amount = new BigDecimal(100000).setScale(4, RoundingMode.HALF_EVEN);
        AccountTransaction transaction = new AccountTransaction("EUR", amount, 3L, 4L);

        given().expect()
                .response().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .when().post(getUrl("/transaction"), transaction);
    }


    @Test
    public void testTransactionDifferentCcy() {
        BigDecimal amount = new BigDecimal(100).setScale(4, RoundingMode.HALF_EVEN);
        AccountTransaction transaction = new AccountTransaction("USD", amount, 3L, 4L);

        given().expect()
                .response().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .when().post(getUrl("/transaction"), transaction);

    }

}
