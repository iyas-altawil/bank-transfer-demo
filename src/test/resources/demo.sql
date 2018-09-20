DROP TABLE IF EXISTS Account;

CREATE TABLE Account (AccountId LONG PRIMARY KEY AUTO_INCREMENT NOT NULL,
UserName VARCHAR(30),
Balance DECIMAL(19,4),
CurrencyCode VARCHAR(30)
);

INSERT INTO Account (UserName,Balance,CurrencyCode) VALUES ('iyas',100.0000,'USD');
INSERT INTO Account (UserName,Balance,CurrencyCode) VALUES ('test1',200.0000,'USD');
INSERT INTO Account (UserName,Balance,CurrencyCode) VALUES ('iyas',500.0000,'EUR');
INSERT INTO Account (UserName,Balance,CurrencyCode) VALUES ('test1',500.0000,'EUR');
INSERT INTO Account (UserName,Balance,CurrencyCode) VALUES ('iyas',500.0000,'GBP');
INSERT INTO Account (UserName,Balance,CurrencyCode) VALUES ('test1',500.0000,'GBP');
