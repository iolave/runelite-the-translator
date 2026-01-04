package com.translator;

import com.google.gson.JsonArray;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;

import static java.net.http.HttpClient.newHttpClient;

class TranslatorAPI {
	HttpClient client = newHttpClient();

	/**
	 * Send collected dialogues to the runelite-translator-api.
	 *
	 * @param arr array of dialogues
	 */
	void sendDialogues(HashSet<String> arr) throws Exception {
		JsonArray body = new JsonArray();
		for (String d : arr) {
			body.add(d);
		}

		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create("https://runelite-translator-api-production.up.railway.app/api/v1/dialogues"))
			.setHeader("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(body.toString()))
			.build();

		HttpResponse<Void> response = null;
		response = client.send(
			request,
			HttpResponse.BodyHandlers.discarding());

		if (response.statusCode() == 200) {
			return;
		}

		throw new Exception("Some required files are missing");
	}
}


