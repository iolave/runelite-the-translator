package com.translator;

import com.google.gson.*;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class TranslatorAPI {
	enum TranslationFileType {
		dialogue,
		npc,
		items,
		item, // will replace 'items'
		object,
		menu_entry_option
	}

	/**
	 * An HTTP Client to access the Google Translate API.
	 */
	@Inject
	private OkHttpClient client;
	private final HashSet<String> collectedGameText = new HashSet<>();
	private final String baseURL = "https://runelite-translator-api-production.up.railway.app";
	// private final String baseURL = "http://localhost:8080";

	/**
	 * Send collected dialogues to the runelite-translator-api.
	 *
	 * @param arr array of dialogues
	 */
	void sendCollectedGameText() throws Exception {
		if (collectedGameText.isEmpty()) {
			return;
		}

		JsonArray body = new JsonArray();
		for (String gt : collectedGameText) {
			String[] gtSplitted = gt.split(";");
			if (gtSplitted.length < 2) {
				continue;
			}
			JsonPrimitive type = new JsonPrimitive(gtSplitted[0]);
			JsonElement text = new JsonPrimitive(gt.replace(String.format("%s;",gtSplitted[0]), ""));

			JsonObject obj = new JsonObject();
			obj.add("type", type);
			obj.add("text", text);

			body.add(obj);
		}

		MediaType JSON = MediaType.parse("application/json; charset=utf-8");
		RequestBody rb = RequestBody.create(JSON, body.toString());

		Request req = new Request.Builder()
			.header("Content-Type", "application/json")
			.method("POST", rb)
			.url(String.format("%s/api/v1/data-collection", baseURL))
			.build();

		Response response = client.newCall(req).execute();
		if (response.code() != 200) {
			response.close();
			throw new Exception("failed to send collected game text, got status " + response.code());
		}
		response.close();

		collectedGameText.clear();
	}

	void collectGameTextData(
		TranslationFileType type,
		String text
	) {
		collectedGameText.add(String.format("%s;%s", type, text));
	}

	HashMap<String, String> getTranslationMap(
		TranslationFileType type,
		TranslatorConfig.Language lang
	) throws Exception {
		InputStream in;
		String url = buildTranslationFileURL(type, lang);

		try {
			in = URI.create(url).toURL().openStream();
		} catch (IOException e) {
			String msg = String.format("failed to download %s translation for %s", lang, type);
			throw new Exception(msg, e);
		}

		return parseCSV(in, ";");
	}

	private HashMap<String, String> parseCSV(InputStream in, String delimiter) throws Exception {
		HashMap<String, String> words = new HashMap<>();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = br.readLine()) != null) {
			String[] temp = line.split(delimiter);
			if (temp.length > 1) {
				words.put(temp[0], temp[1]);
			}
		}

		return words;
	}

	private String buildTranslationFileURL(
		TranslationFileType type,
		TranslatorConfig.Language lang
	) {
		return String.format(
			"https://raw.githubusercontent.com/iolave/osrs-translations/refs/heads/master/%s/%s.txt",
			lang,
			type
		);
	}
}