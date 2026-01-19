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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

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
		name = "Chat box translations",
		description = "Chat box translation options (dialogues, options and messages)"
	)
	String chatboxTranslations = "chatboxTranslations";

	@ConfigItem(
		keyName = "translateWidgets",
		name = "Translate dialogues",
		description = "Translate chat box dialogues",
		position = 1,
		section = chatboxTranslations
	)
	default boolean translateWidgets() {
		return true;
	}

	@ConfigItem(
		keyName = "translateWidgetsOptions",
		name = "Translate dialogues options",
		description = "Translate chatbox dialogue options (messes with quest helper)",
		position = 2,
		section = chatboxTranslations
	)
	default boolean translateWidgetsOptions() {
		return false;
	}

	@ConfigItem(
		keyName = "translateChatBoxRT",
		name = "Translate messages in real time",
		description = "Translate chat box messages in real time",
		warning = "Enabling this feature submits your IP address to a 3rd-party server not controlled or verified by Runelite developers.",
		position = 3,
		section = chatboxTranslations
	)
	default boolean translateChatBoxRT() {
		return false;
	}

	@ConfigSection(
		position = 3,
		name = "Pop-up's translations",
		description = "In-game pop-up's translations (right click menu, quest guides)"
	)
	String popupsTranslations = "popupsTranslations";

	@ConfigItem(
		keyName = "translateMenuEntries",
		name = "Translate menu entries",
		description = "Translate menu entries",
		position = 1,
		section = popupsTranslations
	)
	default boolean translateMenuEntries() {
		return true;
	}
}