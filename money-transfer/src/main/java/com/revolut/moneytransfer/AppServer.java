package main.java.com.revolut.moneytransfer;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import main.java.com.revolut.moneytransfer.api.RequestHandler;
import main.java.com.revolut.moneytransfer.service.AccountService;

public class AppServer {

	public static void main(String[] args) throws IOException {

		final RequestHandler requestHandler = new RequestHandler(new AccountService());

		int port = 8080;
		HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
		HttpContext context = server.createContext("/");
		context.setHandler(arg0 -> requestHandler.handle(arg0));
		server.start();
		
		System.out.println("Server started on port " + port + "...");

	}

}