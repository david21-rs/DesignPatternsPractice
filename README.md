# Banking System - Design Patterns Lab

A Java banking application built for a design patterns lab. It has two Swing GUIs (a client portal and an officer dashboard), persistent storage, and implements several GoF design patterns.

## What it does

- Clients can log in, view their accounts, deposit/withdraw, transfer between accounts, and undo the last action
- Officers can add clients, open new accounts, apply overdraft limits, and move funds between accounts
- Data is saved to `bank_save.dat` on close and loaded back on startup
- Demo data is generated on first run so you have something to log in with

## Design Patterns Used

**Singleton** - `BankConfig` and `BankDataStore` both use singleton. `BankConfig` holds interest rate thresholds so they're loaded once and shared globally. `BankDataStore` is the shared data layer between both GUIs so they stay in sync.

**Builder** - `Client.ClientBuilder` builds client objects. The constructor is private so you're forced to use the builder, which means a client is never in an incomplete state.

**Factory Method** - `AccountFactory.createAccount()` handles account instantiation. Adding a new currency type (e.g. USD) only requires adding a new class and one line in the factory, without touching `Client` or `Bank`.

**Decorator** - `OverdraftDecorator` wraps an `Account` and overrides `retrieve()` to allow the balance to go negative up to the overdraft limit. Applied dynamically at runtime via `BankDataStore.applyOverdraft()`.

**Command** - `DepositCommand` and `WithdrawCommand` implement the `Command` interface. `TransactionManager` holds a stack of executed commands and supports `undoLast()`.

**Chain of Responsibility** - Transfers go through a chain before executing. `BalanceCheckHandler` checks for sufficient funds, `LimitCheckHandler` enforces a 6700 RON daily limit. If either fails, the chain breaks and the transfer is blocked.

## SOLID / GRASP Notes

- `Account` is abstract and `transfer()` is abstract, so adding new account types doesn't require modifying the base class (OCP)
- `Bank` depends on `InterfCLient` instead of `Client` directly, so it's not tied to a concrete implementation (DIP)
- `BankConfig` centralizes interest rate values so `Account` doesn't need to change when rates change (SRP)
- Account creation was moved out of `Client` into `AccountFactory` to avoid coupling client logic to account construction (Low Coupling / DIP)

## Running

Requires Java 8+. For the tests you'll need JUnit 4 on the classpath.

To run the client portal:
```
src.ro.uvt.fi.dp.ClientGUI
```

To run the officer dashboard:
```
src.ro.uvt.fi.dp.OfficerGUI
```

Both GUIs share the same `BankDataStore` instance, so you can open both at the same time and changes will reflect in both windows.

## Demo Accounts (first run)

| Client | Account | Type | Balance |
|---|---|---|---|
| Ionescu Ion | EUR124 | EUR | 2400.00 |
| Ionescu Ion | RON1234 | RON | 1500.00 |
| Marinescu Marin | RON126 | RON | 400.00 |
| Vasilescu Vasile | EUR128 | EUR | 5200.00 |

## Project Structure

```
src/ro/uvt/fi/dp/
    Account.java               - abstract base class
    RonAcc.java                - RON account, implements transfer
    EuroAcc.java               - EUR account, transfer not supported
    AccountFactory.java        - Factory Method
    AccountDecorator.java      - abstract decorator base
    OverdraftDecorator.java    - adds overdraft behavior
    Client.java                - has nested ClientBuilder
    InterfCLient.java          - interface Bank depends on
    Bank.java                  - holds clients
    BankConfig.java            - Singleton, interest rate config
    BankDataStore.java         - Singleton, shared data + persistence
    TransactionManager.java    - Command pattern, undo stack
    Command.java               - Command interface
    DepositCommand.java
    WithdrawCommand.java
    TransferHandler.java       - Chain of Responsibility base
    BalanceCheckHandler.java
    LimitCheckHandler.java
    Operations.java            - interface for account operations
    Transfer.java              - interface for transfer
    ClientGUI.java             - client-facing Swing window
    OfficerGUI.java            - officer-facing Swing window
    EldenTheme.java            - shared UI theme and components
    BankingTest.java           - JUnit 4 tests
    Test.java                  - manual console test
```
