# Money Transfer API
By [Jihyun Um](ashleyum20@gmail.com)


## Task Description
Design and implement a RESTful API (including data model and the backing implementation) for money transfers between accounts.

### Explicit Requirements:
1. You can use Java, Scala or Kotlin.
2. Keep it simple and to the point (e.g. no need to implement any authentication).
3. Assume the API is invoked by multiple systems and services on behalf of end users.
4. You can use frameworks/libraries if you like (except Spring), but don't forget about
requirement #2 – keep it simple and avoid heavy frameworks.
5. The datastore should run in-memory for the sake of this test.
6. The final result should be executable as a standalone program (should not require
a pre-installed container/server).
7. Demonstrate with tests that the API works as expected.

### Implicit Requirements:
1. The code produced by you is expected to be of high quality.
2. There are no detailed requirements, use common sense.


## Discussion
- I used Java 8 and Maven.
- Java HttpServer is used to generate a simple server, making the project a standalone program.
- AccountRepository.java class stores the accounts data in-memory. Account ID begins with 1000000(Long) and is incremented each time an account is created.
- For a simple demonstration, sample supported currencies are GBP, EUR, and USD.
- Unit testing can be demonstrated by running *Test.java classes as JUnit.
- Two APIs are provided to create an account and transfer money between two accounts. In order to test the transfer function, two accounts have to be created prior to the transfer. Please refer to the API Documentation section for instruction.


## Used Libraries
- [JUnit 4.12](https://junit.org/junit4/) - Simple framework for unit testing
- [Mockito 2.10.0](https://static.javadoc.io/org.mockito/mockito-core/2.10.0/org/mockito/Mockito.html) - Mocking framework for unit testing
- [HttpServer](https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpServer.html) - Simple HTTP Server
- [Jackson-Databind 2.1.0](http://fasterxml.github.io/jackson-databind/javadoc/2.1.0/) - Provides functionality for reading and writing JSON


## Installation Instruction (On Eclipse)
1. Import > Existing Projects Into Workspace > Select archive file > Select money-transfer.zip > Finish 
2. Run main.java.com.revolut.moneytransfer.AppServer.java and see if the following is printed in the console.
```
Server started on port 8080...
```
3. If the message is correctly printed in the console, start testing with http://localhost:8080/{API_URI}.
4. If any import error occurs, please follow [this instruction](https://crunchify.com/mavenmvn-clean-install-update-project-and-project-clean-options-in-eclipse-ide-to-fix-any-dependency-issue/) to resolve the Maven issues and repeat #2 and #3.


# API Documentation

## Create Account

### Method : POST
### URI : /create
### Request body parameters :
  - amount : initial amount for the account (String)
  - currency : default currency for the account - GBP, EUR, USD (String)
```
{"amount":"500", "currency":"GBP"}
```
### Response message :
```
Account create success
Account { id=1000000, amount=500 (GBP) }
```

## Transfer Money

### Method : POST
### URI : /transfer
### Request body parameters :
  - sender : sender's account id (String)
  - receiver : receiver's account id (String)
  - amount : amount to transfer (String)
  - currency : currency for transfer - GBP, EUR, USD (String)
```
{ "sender":"1000000", "receiver":"1000001", "amount":"200", "currency":"GBP" }
```
### Response message :
```
Transfer success
Sender Account { id=1000000, amount=300 (GBP) }
Receiver Account { id=1000001, amount=700 (GBP) }
```
