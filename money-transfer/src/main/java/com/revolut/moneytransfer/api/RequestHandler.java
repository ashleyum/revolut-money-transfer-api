package main.java.com.revolut.moneytransfer.api;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import main.java.com.revolut.moneytransfer.exception.AccountNotFoundException;
import main.java.com.revolut.moneytransfer.exception.IncorrectInputException;
import main.java.com.revolut.moneytransfer.exception.InsufficientBalanceException;
import main.java.com.revolut.moneytransfer.exception.InvalidReceiverException;
import main.java.com.revolut.moneytransfer.exception.UnsupportedCurrencyException;
import main.java.com.revolut.moneytransfer.model.Account;
import main.java.com.revolut.moneytransfer.model.Amount;
import main.java.com.revolut.moneytransfer.service.AccountService;

public class RequestHandler implements HttpHandler {
	
	private final AccountService accountService;

	public RequestHandler(AccountService accountService) {
		this.accountService = accountService;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		URI requestUri = exchange.getRequestURI();
		String requestMethod = exchange.getRequestMethod();
		OutputStream responseBody = exchange.getResponseBody();
		
		System.out.println("---------------------------------------------------");
		System.out.println(">> Request URI : " +  requestUri);
		System.out.println(">> Request Method : " +  requestMethod);

		if (!"POST".equalsIgnoreCase(requestMethod)) {
			String errorMessage = "Incorrect HTTP method. Only POST is allowed for this request.";
			exchange.sendResponseHeaders(405, errorMessage.getBytes().length);
			responseBody.write(errorMessage.getBytes());
			responseBody.close();
			return;
		}

		if ("/create".equals(requestUri.toString())) {
			handleCreate(exchange);
		} else if ("/transfer".equals(requestUri.toString())) {
			handleTransfer(exchange);
		}

	}

	private void handleCreate(HttpExchange exchange) throws IOException {

		OutputStream responseBody = exchange.getResponseBody();
		int responseCode = 0;
		String responseMessage = "";

		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> requestBody = mapper.reader().withType(Map.class).readValue(exchange.getRequestBody());

			if (requestBody == null || requestBody.get("amount") == null || requestBody.get("currency") == null) {
				throw new IncorrectInputException("Incorrect request body format. Please refer to API Document for correct input format.");
			}
			
			String amountValue = requestBody.get("amount").toString();
			String currency = requestBody.get("currency").toString();

			Amount amount = new Amount(new BigDecimal(amountValue), currency);
			Account account = accountService.createAccount(amount);

			responseCode = 200;
			responseMessage = "Account create success\n" + account;
			exchange.sendResponseHeaders(responseCode, responseMessage.getBytes().length);
			responseBody.write(responseMessage.getBytes());
			
		} catch (JsonParseException | IncorrectInputException | UnsupportedCurrencyException e) {
			
			responseCode = 400;
			responseMessage = e.getMessage();
			exchange.sendResponseHeaders(responseCode, responseMessage.getBytes().length);
			responseBody.write(responseMessage.getBytes());

		} finally {
			responseBody.close();
		}
		
		System.out.println("<< Response Code : " +  responseCode);
		System.out.println("<< Response Message : " +  responseMessage);

	}

	private void handleTransfer(HttpExchange exchange) throws IOException {

		OutputStream responseBody = exchange.getResponseBody();
		int responseCode = 0;
		String responseMessage = "";
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> requestBody = mapper.reader().withType(Map.class).readValue(exchange.getRequestBody());

			if (requestBody == null 
					|| requestBody.get("sender") == null || requestBody.get("receiver") == null
					|| requestBody.get("amount") == null || requestBody.get("currency") == null) {
				
				throw new IncorrectInputException("Incorrect request body format. Please refer to API Document for correct input format.");
			}
			
			String sender = requestBody.get("sender").toString();
			String receiver = requestBody.get("receiver").toString();
			String amountValue = requestBody.get("amount").toString();
			String currency = requestBody.get("currency").toString();

			Long senderId = Long.parseLong(sender);
			Long receiverId = Long.parseLong(receiver);
			Amount amount = new Amount(new BigDecimal(amountValue), currency);

			accountService.transferAmount(senderId, receiverId, amount);

			Account senderAccount = accountService.searchAccount(senderId);
			Account receiverAccount = accountService.searchAccount(receiverId);

			responseCode = 200;
			responseMessage = new StringBuilder()
					.append("Transfer success")
					.append("\nSender ").append(senderAccount)
					.append("\nReceiver ").append(receiverAccount)
					.toString();

			exchange.sendResponseHeaders(responseCode, responseMessage.getBytes().length);
			responseBody.write(responseMessage.getBytes());

		} catch (JsonParseException | IncorrectInputException | AccountNotFoundException | 
				UnsupportedCurrencyException | InsufficientBalanceException | InvalidReceiverException e) {

			responseCode = 400;
			responseMessage = e.getMessage();
			exchange.sendResponseHeaders(responseCode, responseMessage.getBytes().length);
			responseBody.write(responseMessage.getBytes());

		} finally {
			responseBody.close();
		}
		
		System.out.println("<< Response Code : " +  responseCode);
		System.out.println("<< Response Message : " +  responseMessage);

	}

}
