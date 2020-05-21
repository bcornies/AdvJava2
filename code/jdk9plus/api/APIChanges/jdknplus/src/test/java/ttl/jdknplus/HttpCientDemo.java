package ttl.jdknplus;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

public class HttpCientDemo {

	@Test
	public void testSyncCall() throws IOException, InterruptedException {
		HttpClient.Builder clientBuilder = HttpClient.newBuilder();
		clientBuilder.followRedirects(Redirect.NORMAL);
		clientBuilder.connectTimeout(Duration.ofMillis(500));

		HttpClient client = clientBuilder.build();

		HttpRequest request = HttpRequest
				.newBuilder(URI.create("http://google.com"))
				.GET().build();

		HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

		assertEquals(200, response.statusCode());

		System.out.println("status code: " + response.statusCode() + "resonse body: " + response.body());

	}

	@Test
	public void testASyncCall() throws IOException, InterruptedException {
		HttpClient.Builder clientBuilder = HttpClient.newBuilder();
		clientBuilder.followRedirects(Redirect.NORMAL);
		clientBuilder.connectTimeout(Duration.ofMillis(500));

		HttpClient client = clientBuilder.build();

		HttpRequest request = HttpRequest.newBuilder(URI.create("http://google.com")).GET().build();

		CompletableFuture<HttpResponse<String>> cf = client.sendAsync(request, BodyHandlers.ofString());

		cf.thenAccept(response -> {
			assertEquals(200, response.statusCode());
			System.out.println("status code: " + response.statusCode() + "resonse body: " + response.body());
		});
		
		cf.join();

	}
}
