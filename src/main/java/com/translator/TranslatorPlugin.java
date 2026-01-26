/*
 * Copyright (c) 2017, Aria <aria@ar1as.space>
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

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.game.ItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.runelite.api.gameval.InterfaceID.*;

@PluginDescriptor(
        name = "Translator",
        description = "translate npc, items, objects and dialogue",
        tags = {"actions"}
)
public class TranslatorPlugin extends Plugin {
	private static final Logger log = LoggerFactory.getLogger(TranslatorPlugin.class);
	@Inject
	private TranslatorConfig config;
    @Inject
    private Client client;
    @Inject
    private ItemManager itemManager;
    @Provides
    TranslatorConfig provideConfig(ConfigManager configManager) {return configManager.getConfig(TranslatorConfig.class);}
	@Inject
	private TranslatorAPI api;
	private ScheduledExecutorService executor;
	@Inject
	private ClientThread clientThread;

	private HashMap<TranslatorAPI.TranslationFileType, HashMap<String, String>> maps = new HashMap<>();

    private Widget prevTickWidget;
    private Actor actor;
	private final Long dataCollectionPeriod = 5L;
	private final TimeUnit dataCollectionTU = TimeUnit.MINUTES;

	private void loadTranslation(TranslatorConfig.Language lang) {
		executor.execute(() -> {
				for (TranslatorAPI.TranslationFileType t : TranslatorAPI.TranslationFileType.values()) {
					try {
						maps.put(t, api.getTranslationMap(t, lang));
					} catch (Exception e) {
						log.error("failed to load translation '{}:{}'", t, lang, e);
					}
				}
		});
	}

    @Override
    protected void startUp() {
		executor = Executors.newSingleThreadScheduledExecutor();

		for (TranslatorAPI.TranslationFileType t : TranslatorAPI.TranslationFileType.values()) {
			maps.put(t, new HashMap<>());
		}

		loadTranslation(config.selectLanguage());

		if (config.enableTextCapture()) {
			executor.scheduleAtFixedRate(
				this::collectGameTexts,
				0,
				dataCollectionPeriod,
				dataCollectionTU
			);
		}
    }

	@Override
	protected void shutDown() {
		executor.shutdownNow();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals("translator")) {
			return;
		}

		if (event.getKey().equals("SelectLanguage")) {
			loadTranslation(config.selectLanguage());
		}

		if (event.getKey().equals("enableOSRSTextCollection")) {
			if (config.enableTextCapture()) {
				executor.scheduleAtFixedRate(
					this::collectGameTexts,
					0,
					dataCollectionPeriod,
					dataCollectionTU
				);
			} else {
				executor.shutdownNow();
			}
		}
	}

    private String notedItemsCheck(HashMap<String, String> words, Integer id){
        String translated = null;
        if (words == maps.get(TranslatorAPI.TranslationFileType.item)) {
            final ItemComposition itemComp = itemManager.getItemComposition(id);
            if (itemComp.getNote() == 799) {
                translated = words.get(String.valueOf(itemComp.getLinkedNoteId()));
            }
        }
        return translated;
    }

    private void translateMenuEntrys(HashMap<String, String> words, MenuEntry menuEntry, Integer id){
        String target = menuEntry.getTarget();

        if (target.length() > 0) {
            String translated = words.get(id.toString());
            String notedId = notedItemsCheck(words, id);
            if (notedId != null){
                translated = notedId;
            }
            String[] subStrings = target.split(">");
            int colStart = subStrings[0].length() + 1;
            String colour = target.substring(0, colStart);

            if (subStrings.length > 2) {
                //npc with combat lvl
                int combatStart = target.split("<")[1].length() + 1;
                String combat = target.substring(combatStart);

                if (translated != null) {
                    menuEntry.setTarget(colour + translated + combat);
                }
            } else {
                String[] itemSubStrings = target.split("\\(");
                //amount after item
                if (itemSubStrings.length > 1){
                    String amount = "("+itemSubStrings[itemSubStrings.length-1];
                    if (translated != null) {
                        menuEntry.setTarget(colour + translated + amount);
                    }
                }
                else if (translated != null) {
                    //items
                    menuEntry.setTarget(colour + translated);
                }
            }
        }
    }

    @Subscribe
    public void onInteractingChanged(InteractingChanged event)
    {
        if (event.getTarget() == null || event.getSource() != client.getLocalPlayer()) {
            return;
        }
        actor = event.getTarget();
    }

	void translateWidgetTextRecursively(Widget w) {
		if (w == null) {
			log.error("failed to translate widget text, got null widget");
			return;
		}

		if (w.getType() == WidgetType.TEXT) {
			String text = w.getText();
			if (!text.isEmpty()) {
				new Thread(() -> {
					try {
						String translated = api.translate(config.selectLanguage(), text);
						if (translated == null) {
							return;
						}

						clientThread.invokeLater(() -> {
							w.setText(String.format("%s", translated));
						});
					} catch (Exception e) {
						w.setText(String.format("%s<br>error: failed to translate", text));
						log.error("failed to translate quest text", e);
					}
				}).start();
			}
		}

		Widget[] wc = w.getChildren();
		if (wc != null) {
			for (Widget wcw : wc) {
				translateWidgetTextRecursively(wcw);
			}
		}
		wc = w.getDynamicChildren();
		if (wc != null) {
			for (Widget wcw : wc) {
				translateWidgetTextRecursively(wcw);
			}
		}
		wc = w.getNestedChildren();
		if (wc != null) {
			for (Widget wcw : wc) {
				translateWidgetTextRecursively(wcw);
			}
		}
		wc = w.getStaticChildren();
		if (wc != null) {
			for (Widget wcw : wc) {
				translateWidgetTextRecursively(wcw);
			}
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event) {
		if (event.getScriptId() == ScriptID.QUEST_UPDATE_LINECOUNT) {
			if (config.enableRealtimeTranslations() && config.translateQuestGuide()) {
				translateWidgetTextRecursively(client.getWidget(Questjournal.UNIVERSE));
			}
		}
	}

    @Subscribe
    public void onGameTick(GameTick event) {
        if (actor != null)
        {
            checkWidgetDialogs();
            checkWidgetOptionDialogs();
        }
    }

	private void checkWidgetOptionDialogs() {
		Widget playerOptionsWidget = client.getWidget(ComponentID.DIALOG_OPTION_OPTIONS);
        if (playerOptionsWidget == null) {
            return;
        }

        Widget[] optionWidgets = playerOptionsWidget.getChildren();
		if (optionWidgets == null) {
			return;
		}

		for (Widget widget : optionWidgets) {
            String optionText = widget.getText();
            if (optionText == null) {
                continue;
            }
			optionText = optionText.replace("<br>", " ");
			String translated = maps.get(TranslatorAPI.TranslationFileType.dialogue).get(optionText);
            if (translated == null) {
				api.collectGameTextData(
					TranslatorAPI.TranslationFileType.dialogue,
					optionText
				);
			} else {
				if (!config.translateWidgetsOptions()) {
					return;
				}

				widget.setText(translated);
			}
		}
	}

    private void checkWidgetDialogs() {
        Widget widget;
        if (client.getWidget(ComponentID.DIALOG_NPC_TEXT) != null) {
            widget = client.getWidget(ComponentID.DIALOG_NPC_TEXT);
        } else {
            widget = client.getWidget(ComponentID.DIALOG_PLAYER_TEXT);
        }

        if (widget == null) {
            return;
        }

        String text = widget.getText();
        if (text == null) {
            return;
        }
        text = text.replace("<br>", " ");
		String playerName = client.getLocalPlayer().getName();
		if (playerName != null) {
			text = text.replaceAll(playerName, "[PLAYER_NAME]");
		}
		String slayAmtAndMonster = "";
		if (text.startsWith("Your new task is to kill")) {
			slayAmtAndMonster = text.replace("Your new task is to kill ", "");
			text = "Your new task is to kill [SLAYER_TASK_AMT_AND_MONSTER]";
		}

		String translated = maps.get(TranslatorAPI.TranslationFileType.dialogue).get(text);
		if (translated == null) {
			api.collectGameTextData(
				TranslatorAPI.TranslationFileType.dialogue,
				text
			);

			return;
		}

		if (playerName != null) {
			translated = translated.replace("[PLAYER_NAME]", playerName);
		}
		if (!slayAmtAndMonster.isEmpty()) {
			translated = translated.replace("[SLAYER_TASK_AMT_AND_MONSTER]", slayAmtAndMonster);
		}

		if (!config.translateWidgets()) {
			return;
		}

		widget.setText(translated);
    }

	private void collectGameTexts() {
		try {
			api.sendCollectedGameText();
		} catch (Exception e) {
			log.error("failed to send game text to backend", e);
		}
	}

	private void translateMenuEntry(MenuEntry entry) {
		String opt = entry.getOption();

		// if data collection is enabled then add to the set:
		// - the entry option
		if (config.enableTextCapture()) {
			api.collectGameTextData(
				TranslatorAPI.TranslationFileType.menu_entry_option,
				opt
			);
		}
		// translate menu entry options
		if (opt != null && !opt.isEmpty()) {
			String translated = maps.get(TranslatorAPI.TranslationFileType.menu_entry_option).get(opt);
			if (translated == null) {
				return;
			}

			entry.setOption(translated);
		}

		// OLD TRANSLATION
		//item
		if (entry.getItemId() > 0) {
			translateMenuEntrys(maps.get(TranslatorAPI.TranslationFileType.item), entry, entry.getItemId());
		}
		//equipped item
		if (entry.getWidget() != null && entry.getWidget().getId() <= 25362457 && entry.getWidget().getId() >= 25362447) {
			translateMenuEntrys(maps.get(TranslatorAPI.TranslationFileType.item), entry, entry.getWidget().getChild(1).getItemId());
		}
		//ground items
		else if (entry.getType() == MenuAction.EXAMINE_ITEM_GROUND | entry.getType() == MenuAction.GROUND_ITEM_THIRD_OPTION ) {
			translateMenuEntrys(maps.get(TranslatorAPI.TranslationFileType.item), entry, entry.getIdentifier());
		}
		//not item
		else if (entry.getItemId() == -1) {
			//player
			if (entry.getPlayer() != null) {
			}
			//npc
			else if (entry.getNpc() != null) {
				translateMenuEntrys(maps.get(TranslatorAPI.TranslationFileType.npc), entry, entry.getNpc().getId());
			}
			//object
			else if (entry.getIdentifier() > 0 & entry.getType() != MenuAction.CC_OP & entry.getType() != MenuAction.RUNELITE & entry.getType() != MenuAction.WALK && entry.getType() != MenuAction.CC_OP_LOW_PRIORITY) {
				translateMenuEntrys(maps.get(TranslatorAPI.TranslationFileType.object), entry, entry.getIdentifier());
			}
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event) {
		if (config.translateMenuEntries()) {
			MenuEntry entry = event.getMenuEntry();
			translateMenuEntry(entry);
		}

	}
	@Subscribe
	public void onChatMessage(ChatMessage event) {
		ChatMessageType type = event.getType();
		String msg = event.getMessage();
		String sender = event.getSender();
		String name = event.getName();
		if (msg == null) {
			return;
		}

		if (!config.translateChatBox()) {
			return;
		}
		if (name != null && name.equals("translator")) {
			return;
		}

		if (type == ChatMessageType.DIALOG) {
			return;
		}

		new Thread(() -> {
			try {
				String translated = api.translate(config.selectLanguage(), msg);
				clientThread.invokeLater(() -> {
					if (name == null || name.isEmpty()) {
						client.addChatMessage(
							ChatMessageType.GAMEMESSAGE,
							"translator",
							String.format("[translator] %s", translated),
							sender
						);
					} else {
						client.addChatMessage(
							ChatMessageType.GAMEMESSAGE,
							"translator",
							String.format("[translator] (%s) %s", event.getName(), translated),
							sender
						);
					}

				});
			} catch (Exception e) {
				client.addChatMessage(
					type,
					"translator",
					"error: failed to translate in real time",
					"translator"
				);
				log.error("failed to translate in real time", e);
			}
		}).start();
	}
}