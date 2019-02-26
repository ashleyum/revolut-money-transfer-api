package test.java.com.revolut.moneytransfer.api;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.sun.net.httpserver.HttpExchange;

import main.java.com.revolut.moneytransfer.api.RequestHandler;
import main.java.com.revolut.moneytransfer.model.Account;
import main.java.com.revolut.moneytransfer.model.Amount;
import main.java.com.revolut.moneytransfer.service.AccountService;

@RunWith(MockitoJUnitRunner.class)
public class RequestHandlerTest {

	private RequestHandler requestHandler;

	@Mock
	private HttpExchange exchange;

	@Before
	public void setUp() throws Exception {
		requestHandler = new RequestHandler(new AccountService());
	}

	@Test
	public void handle_IncorrectMethod_ResponseCode405() throws IOException {

		given(exchange.getRequestURI()).willReturn(URI.create("/create"));
		given(exchange.getRequestMethod()).willReturn("GET");
		given(exchange.getResponseBody()).willReturn(new ByteArrayOutputStream());

		requestHandler.handle(exchange);

		int expectedCode = 405;
		String expectedMessage = "Incorrect HTTP method. Only POST is allowed for this request.";

		verify(exchange).sendResponseHeaders(expectedCode, expectedMessage.getBytes().length);

	}

	@Test
	public void handlCreate_IncorrectInput_ResponseCode400() throws IOException {

		StringBuilder input = new StringBuilder()
				.append("{")
				.append("\"amount\":\"1000\"")
				.append("}");

		InputStream requestBody = new ByteArrayInputStream(input.toString().getBytes());

		given(exchange.getRequestURI()).willReturn(URI.create("/create"));
		given(exchange.getRequestMethod()).willReturn("POST");
		given(exchange.getRequestBody()).willReturn(requestBody);
		given(exchange.getResponseBody()).willReturn(new ByteArrayOutputStream());

		requestHandler.handle(exchange);

		Account expectedAccount = new Account();
		expectedAccount.setId(1000000L);
		expectedAccount.setAmount(new Amount(new BigDecimal("1000"), "GBP"));

		int expectedCode = 400;
		String expectedMessage = "Incorrect request body format. Please refer to API Document for correct input format.";

		verify(exchange).sendResponseHeaders(expectedCode, expectedMessage.getBytes().length);

	}
	
	@Test
	public void handlCreate_UnsupportedCurrency_ResponseCode400() throws IOException {

		StringBuilder input = new StringBuilder()
				.append("{")
				.append("\"amount\":\"1000\",")
				.append("\"currency\":\"CAD\"")
				.append("}");

		InputStream requestBody = new ByteArrayInputStream(input.toString().getBytes());

		given(exchange.getRequestURI()).willReturn(URI.create("/create"));
		given(exchange.getRequestMethod()).willReturn("POST");
		given(exchange.getRequestBody()).willReturn(requestBody);
		given(exchange.getResponseBody()).willReturn(new ByteArrayOutputStream());

		requestHandler.handle(exchange);

		Account expectedAccount = new Account();
		expectedAccount.setId(1000000L);
		expectedAccount.setAmount(new Amount(new BigDecimal("1000"), "GBP"));

		int expectedCode = 400;
		String expectedMessage = "Supported currencies are GBP, EUR, and USD.";

		verify(exchange).sendResponseHeaders(expectedCode, expectedMessage.getBytes().length);

	}

	@Test
	public void handlCreate_NormalInputs_Ok() throws IOException {

		StringBuilder input = new StringBuilder()
				.append("{")
				.append("\"amount\":\"1000\",")
				.append("\"currency\":\"GBP\"")
				.append("}");

		InputStream requestBody = new ByteArrayInputStream(input.toString().getBytes());

		given(exchange.getRequestURI()).willReturn(URI.create("/create"));
		given(exchange.getRequestMethod()).willReturn("POST");
		given(exchange.getRequestBody()).willReturn(requestBody);
		given(exchange.getResponseBody()).willReturn(new ByteArrayOutputStream());

		requestHandler.handle(exchange);

		Account expectedAccount = new Account();
		expectedAccount.setId(1000000L);
		expectedAccount.setAmount(new Amount(new BigDecimal("1000"), "GBP"));

		int expectedCode = 200;
		String expectedMessage = "Account create success\n" + expectedAccount.toString();

		verify(exchange).sendResponseHeaders(expectedCode, expectedMessage.getBytes().length);

	}

	@Test
	public void handlTransfer_NonexistentId_ResponseCode400() throws IOException {

		StringBuilder transferInput = new StringBuilder()
				.append("{")
				.append("\"sender\":\"5000000\",")
				.append("\"receiver\":\"5000001\",")
				.append("\"amount\":\"200\",")
				.append("\"currency\":\"GBP\"")
				.append("}");

		InputStream transferRequestBody = new ByteArrayInputStream(transferInput.toString().getBytes());

		given(exchange.getRequestURI()).willReturn(URI.create("/transfer"));
		given(exchange.getRequestMethod()).willReturn("POST");
		given(exchange.getRequestBody()).willReturn(transferRequestBody);
		given(exchange.getResponseBody()).willReturn(new ByteArrayOutputStream());

		requestHandler.handle(exchange);

		int expectedCode = 400;
		String expectedMessage = "Account cannot be found - 5000000";
		
		verify(exchange).sendResponseHeaders(expectedCode, expectedMessage.getBytes().length);

	}
	
	@Test
	public void handlTransfer_SameSenderAndReceiver_ResponseCode400() throws IOException {

		// (1) Create sender account with £500
		
		StringBuilder senderInput = new StringBuilder()
				.append("{")
				.append("\"amount\":\"500\",")
				.append("\"currency\":\"GBP\"")
				.append("}");

		InputStream senderRequestBody = new ByteArrayInputStream(senderInput.toString().getBytes());

		given(exchange.getRequestURI()).willReturn(URI.create("/create"));
		given(exchange.getRequestMethod()).willReturn("POST");
		given(exchange.getRequestBody()).willReturn(senderRequestBody);
		given(exchange.getResponseBody()).willReturn(new ByteArrayOutputStream());

		requestHandler.handle(exchange);

		Account senderAccount = new Account();
		senderAccount.setId(1000000L);
		senderAccount.setAmount(new Amount(new BigDecimal("500"), "GBP"));

		int expectedCode = 200;
		String expectedMessage = "Account create success\n" + senderAccount.toString();

		verify(exchange).sendResponseHeaders(expectedCode, expectedMessage.getBytes().length);

		
		// (2) Transfer £300 from sender to sender
		// -> Bad Request (Invalid Receiver)
		
		StringBuilder transferInput = new StringBuilder()
				.append("{")
				.append("\"sender\":\"1000000\",")
				.append("\"receiver\":\"1000000\",")
				.append("\"amount\":\"300\",")
				.append("\"currency\":\"GBP\"")
				.append("}");

		InputStream transferRequestBody = new ByteArrayInputStream(transferInput.toString().getBytes());

		given(exchange.getRequestURI()).willReturn(URI.create("/transfer"));
		given(exchange.getRequestMethod()).willReturn("POST");
		given(exchange.getRequestBody()).willReturn(transferRequestBody);
		given(exchange.getResponseBody()).willReturn(new ByteArrayOutputStream());

		requestHandler.handle(exchange);

		expectedCode = 400;
		expectedMessage = "Sender and receiver cannot be the same account.";
		
		verify(exchange).sendResponseHeaders(expectedCode, expectedMessage.getBytes().length);

	}
	
	@Test
	public void handlTransfer_NormalInputs_Ok() throws IOException {

		// (1) Create sender account with £1000
		
		StringBuilder senderInput = new StringBuilder()
				.append("{")
				.append("\"amount\":\"1000\",")
				.append("\"currency\":\"GBP\"")
				.append("}");

		InputStream senderRequestBody = new ByteArrayInputStream(senderInput.toString().getBytes());

		given(exchange.getRequestURI()).willReturn(URI.create("/create"));
		given(exchange.getRequestMethod()).willReturn("POST");
		given(exchange.getRequestBody()).willReturn(senderRequestBody);
		given(exchange.getResponseBody()).willReturn(new ByteArrayOutputStream());

		requestHandler.handle(exchange);

		Account senderAccount = new Account();
		senderAccount.setId(1000000L);
		senderAccount.setAmount(new Amount(new BigDecimal("1000"), "GBP"));

		int expectedCode = 200;
		String expectedMessage = "Account create success\n" + senderAccount.toString();

		verify(exchange).sendResponseHeaders(expectedCode, expectedMessage.getBytes().length);

		
		// (2) Create receiver account with £500
		
		StringBuilder receiverInput = new StringBuilder()
				.append("{")
				.append("\"amount\":\"500\",")
				.append("\"currency\":\"GBP\"")
				.append("}");

		InputStream receiverRequestBody = new ByteArrayInputStream(receiverInput.toString().getBytes());

		given(exchange.getRequestURI()).willReturn(URI.create("/create"));
		given(exchange.getRequestMethod()).willReturn("POST");
		given(exchange.getRequestBody()).willReturn(receiverRequestBody);
		given(exchange.getResponseBody()).willReturn(new ByteArrayOutputStream());

		requestHandler.handle(exchange);

		Account receiverAccount = new Account();
		receiverAccount.setId(1000001L);
		receiverAccount.setAmount(new Amount(new BigDecimal("500"), "GBP"));

		expectedCode = 200;
		expectedMessage = "Account create success\n" + receiverAccount.toString();

		verify(exchange).sendResponseHeaders(expectedCode, expectedMessage.getBytes().length);

		
		// (3) Transfer £200 from sender to receiver
		// -> Balance after transfer : Sender £800, Receiver £700
		
		StringBuilder transferInput = new StringBuilder()
				.append("{")
				.append("\"sender\":\"1000000\",")
				.append("\"receiver\":\"1000001\",")
				.append("\"amount\":\"200\",")
				.append("\"currency\":\"GBP\"")
				.append("}");

		InputStream transferRequestBody = new ByteArrayInputStream(transferInput.toString().getBytes());

		given(exchange.getRequestURI()).willReturn(URI.create("/transfer"));
		given(exchange.getRequestMethod()).willReturn("POST");
		given(exchange.getRequestBody()).willReturn(transferRequestBody);
		given(exchange.getResponseBody()).willReturn(new ByteArrayOutputStream());

		requestHandler.handle(exchange);

		senderAccount.setAmount(new Amount(new BigDecimal("800"), "GBP"));
		receiverAccount.setAmount(new Amount(new BigDecimal("700"), "GBP"));

		expectedCode = 200;
		expectedMessage = new StringBuilder()
				.append("Transfer success")
				.append("\nSender ").append(senderAccount)
				.append("\nReceiver ").append(receiverAccount)
				.toString();
		
		verify(exchange).sendResponseHeaders(expectedCode, expectedMessage.getBytes().length);

	}

}
