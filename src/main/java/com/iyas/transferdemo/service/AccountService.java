package com.iyas.transferdemo.service;

import com.iyas.transferdemo.common.CustomException;
import com.iyas.transferdemo.common.Utils;
import com.iyas.transferdemo.dao.DaoFactory;
import com.iyas.transferdemo.domain.Account;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@Path("/account")
@Produces(MediaType.APPLICATION_JSON)
public class AccountService {

    private final DaoFactory daoFactory = new DaoFactory();

    @GET
    @Path("/all")
    public Response getAllAccounts() {
        try {
            List<Account> accounts = daoFactory.getAccountDAO().getAllAccounts();
            return Response.ok(accounts).build();
        }catch (CustomException e){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }


    @GET
    @Path("/{accountId}")
    public Response getAccount(@PathParam("accountId") long accountId) {
        try {
            Account account = daoFactory.getAccountDAO().getAccountById(accountId);
        return Response.ok(account).build();
        } catch (CustomException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }


    @GET
    @Path("/{accountId}/balance")
    public Response getBalance(@PathParam("accountId") long accountId) {
        try {
            Account account = daoFactory.getAccountDAO().getAccountById(accountId);

            if (account == null)
                return Response.status(Response.Status.NOT_FOUND).build();

            return Response.ok(account.getBalance()).build();
        } catch (CustomException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }


    @POST
    @Path("/create")
    public Response createAccount(String userName, BigDecimal balance, String currencyCode) {
        try {
            Account account = new Account(userName, balance, currencyCode);
            long accountId = daoFactory.getAccountDAO().createAccount(account);

            Account accountById = daoFactory.getAccountDAO().getAccountById(accountId);

            return Response.ok(accountById).build();
        } catch (CustomException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }


    @PUT
    @Path("/{accountId}/deposit/{amount}")
    public Response deposit(@PathParam("accountId") long accountId,@PathParam("amount") BigDecimal amount) {

        if (amount.compareTo(Utils.zeroAmount) <= 0)
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid deposit amount").build();

        try {
            daoFactory.getAccountDAO().updateAccountBalance(accountId, amount.setScale(4, RoundingMode.HALF_EVEN));
            Account account = daoFactory.getAccountDAO().getAccountById(accountId);
            return Response.ok(account).build();
        } catch (CustomException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }


    @PUT
    @Path("/{accountId}/withdraw/{amount}")
    public Response withdraw(@PathParam("accountId") long accountId,@PathParam("amount") BigDecimal amount) {

        if (amount.compareTo(Utils.zeroAmount) <= 0)
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid withdraw amount").build();

        BigDecimal delta = amount.negate();
        try {
            daoFactory.getAccountDAO().updateAccountBalance(accountId, delta.setScale(4, RoundingMode.HALF_EVEN));
            Account account = daoFactory.getAccountDAO().getAccountById(accountId);
            return Response.ok(account).build();
        } catch (CustomException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
