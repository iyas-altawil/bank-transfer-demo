package com.taskforce.moneyapp.services;

import org.apache.http.HttpStatus;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class TestAccountService extends TestService {

    @Test
    public void testGetAccountByUserName() {
        given().expect()
                .response().statusCode(HttpStatus.SC_OK)
                .and().body("userName", is("iyas"))
                .when().get(getUrl("/account/1"));
    }


    @Test
    public void testGetAllAccounts() {
        given().expect()
                .response().statusCode(HttpStatus.SC_OK)
                .and().body("size()", greaterThan(0))
                .when().get(getUrl("/account/all"));
    }


    @Test
    public void testGetAccountBalance() {
        given().expect()
                .response().statusCode(HttpStatus.SC_OK)
                .when().get(getUrl("/account/1/balance"));
    }



    @Test
    public void testCreateAccount() {
        BigDecimal balance = new BigDecimal(10).setScale(4, RoundingMode.HALF_EVEN);

        given().param("userName", "test3")
                .param("balance", balance)
                .param("currencyCode", "GBP")
                .expect()
                    .response().statusCode(HttpStatus.SC_OK)
                    .and().body("userName", is("test3"))
                .when().post(getUrl("/account/create"));
    }


    @Test
    public void testCreateExistingAccount() {
        given().param("userName", "test1")
                .param("balance", new BigDecimal(0))
                .param("currencyCode", "GBP")
                .expect()
                    .response().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .when().post(getUrl("/account/create"));

    }
}
