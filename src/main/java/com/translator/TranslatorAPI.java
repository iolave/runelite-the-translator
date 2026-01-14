package com.translator;

import com.google.gson.JsonArray;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		object
	}

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

		if (type == TranslationFileType.dialogue) {
			return parseDialogue(in);
		}

		return parse(in);
	}

	private HashMap<String, String> parse(InputStream in) throws Exception {
		HashMap<String, String> words = new HashMap<>();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = br.readLine()) != null) {
			String[] temp = line.split(",");
			if (temp.length > 2) {
				words.put(temp[0], temp[2]);
			}
		}
		return words;
	}

	private HashMap<String, String> parseDialogue(InputStream in) throws Exception {
		HashMap<String, String> words = new HashMap<>();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = br.readLine()) != null) {
			String[] temp = line.split(";");
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


