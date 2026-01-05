package com.translator;

import com.google.gson.JsonArray;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class TranslatorAPI {
	private static final Logger log = LoggerFactory.getLogger(TranslatorAPI.class);
	/**
	 * An HTTP Client to access the Google Translate API.
	 */
	@Inject
	private OkHttpClient client;

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

		MediaType JSON = MediaType.parse("application/json; charset=utf-8");
		RequestBody rb = RequestBody.create(JSON, body.toString());

		Request req = new Request.Builder()
			.header("Content-Type", "application/json")
			.method("POST", rb)
			.url("https://runelite-translator-api-production.up.railway.app/api/v1/dialogues")
			.build();

		Response response = client.newCall(req).execute();
		if (response.code() != 200) {
			response.close();
			throw new Exception("failed to send dialogues, got status " + response.code());
		}
		response.close();
	}
}


