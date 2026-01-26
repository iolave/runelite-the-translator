/*
 * Copyright (c) 2018, Morgan Lewis <https://github.com/MESLewis>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.translator;

import net.runelite.client.config.*;

@ConfigGroup("translator")
public interface TranslatorConfig extends Config {
	enum Language{
		finnish,
		french,
		german,
		italian,
		portuguese,
		spanish,
		swedish,
		dutch
	}
	@ConfigSection(
		position = 1,
		name = "General configuration",
		description = "General plugin configuration"
	)
	String generalConfigSection = "generalConfig";

	@ConfigItem(
		keyName = "SelectLanguage",
		name = "Language",
		description = "Select target language",
		position = 1,
		section = generalConfigSection
	)
	default Language selectLanguage() {
		return Language.spanish;
	}

	@ConfigItem(
		keyName = "enableOSRSTextCollection",
		name = "Enable text capture",
		description = "If enabled, OSRS game texts will be captured in order to improve and have a more complete translation system (dialogues, guides, npc names).\n"+
			"We highly suggest to enable this option so you can contribute to the plugin.",
		warning = "Enabling this feature submits your IP address to a 3rd-party server not controlled or verified by Runelite developers.",
		position = 2,
		section = generalConfigSection
	)
	default boolean enableTextCapture() {
		return false;
	}

	@ConfigSection(
		position = 2,
		name = "Static translations",
		description = "Translate using files from the github.com/iolave/osrs-translations repository"
	)
	String staticTranslations = "staticTranslations";

	@ConfigItem(
		keyName = "translateWidgets",
		name = "Chatbox: Translate dialogues",
		description = "Translate chat box dialogues",
		position = 1,
		section = staticTranslations
	)
	default boolean translateWidgets() {
		return true;
	}

	@ConfigItem(
		keyName = "translateWidgetsOptions",
		name = "Chatbox: Translate dialogues options",
		description = "Translate chatbox dialogue options (messes with quest helper)",
		position = 2,
		section = staticTranslations
	)
	default boolean translateWidgetsOptions() {
		return true;
	}

	@ConfigItem(
		keyName = "translateMenuEntries",
		name = "Menu: Translate entries",
		description = "Translate menu entries like (Walk here, Use, Rub, etc)",
		position = 3,
		section = staticTranslations
	)
	default boolean translateMenuEntries() {
		return true;
	}

	@ConfigSection(
		position = 4,
		name = "Real-time translations",
		description = "Translate texts within OSRS in real-time"
	)
	String realtimeTranslations = "realtimeTranslations";

	@ConfigItem(
		keyName = "enableRealtimeTranslations",
		name = "Enable real-time translations",
		description = "Enable this plugin to do translations in real-time",
		warning = "Enabling this feature submits your IP address to a 3rd-party server not controlled or verified by Runelite developers.",
		position = 1,
		section = realtimeTranslations
	)
	default boolean enableRealtimeTranslations() {
		return false;
	}
	@ConfigItem(
		keyName = "translateQuestGuide",
		name = "Journals: Translate quest guides",
		description = "Translate quest guide journals in real time",
		position = 2,
		section = realtimeTranslations
	)
	default boolean translateQuestGuide() {
		return true;
	}
	@ConfigItem(
		keyName = "translateChatBox",
		name = "Chatbox: Translate messages",
		description = "Translate chat box messages in real time",
		position = 3,
		section = realtimeTranslations
	)
	default boolean translateChatBox() {
		return false;
	}
}