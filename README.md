# Bank Account Transfer Demo App

A Java RESTful API for money transfers between bank accounts

### Technologies
- Jetty Server
- H2 database
- JAX-RS API
- REST Assured

Application starts a jetty server on localhost port 8080 with H2 in memory database initialized with some dummy account data

- http://localhost:8080/account/1
- http://localhost:8080/account/2

### Available Services

| HTTP METHOD | PATH | USAGE |
| -----------| ------ | ------ |
| GET | /account/{accountId} | get account by accountId | 
| GET | /account/all | get all accounts | 
| GET | /account/{accountId}/balance | get account balance by accountId | 
| POST | /account/create | create a new account 
| PUT | /account/{accountId}/withdraw/{amount} | withdraw from account | 
| PUT | /account/{accountId}/deposit/{amount} | deposit to account | 
| POST | /transaction | perform transaction between 2 accounts | 

### Sample JSON
##### Account:

```sh
{  
   "userName":"test1",
   "balance":100.0000,
   "currencyCode":"EUR"
} 
```

#### Account Transaction:
```sh
{  
   "currencyCode":"EUR",
   "amount":500.0000,
   "fromAccountId":1,
   "toAccountId":2
}
```
