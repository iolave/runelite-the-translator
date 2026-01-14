# Runlite plugin: Translator

A Runelite plugin that translates Old School RuneScape in-game text to other languages.

## Features

*   Translates game text, including:
    *   NPC dialogues
    *   Items
    *   Objects
    *   Menus and options
*   Supports Dutch, Finnish, French, German, Italian, Portuguese, Spanish, and Swedish.
*   Dialogue collection to improve translations (Opt-in).
*   **Dynamic Translation Loading**: Translation files are downloaded dynamically from the [iolave/osrs-translations](https://github.com/iolave/osrs-translations) repo when needed, ensuring you always have the most up-to-date translations without requiring plugin updates.

## Installation

1.  Install [Runelite](https://runelite.net/).
2.  Open the Plugin Hub.
3.  Search for "Translator" and install it.

## Configuration

You can configure the plugin by going to the plugin settings.

*   **Language**: Choose the target language for translation (Dutch, Finnish, French, German, Italian, Portuguese, Spanish, and Swedish).
*   **Translate Dialogues Options**: Enable or disable the translation of dialogue options.
    *   **Warning**: Enabling this option may interfere with the Quest Helper plugin.
*   **Enable text capture**: Opt-in to collect dialogue data to improve translations.

## Known Issues

*   Enabling "Translate Dialogues Options" can cause issues with the Quest Helper plugin.

## Contributing

You can contribute to this project by enabling the "Enable text capture" option in the plugin settings. This will send dialogue data to a public API to improve the quality of the translations.

## Architecture

The plugin uses the [iolave/runelite-translator-api](https://github.com/iolave/runelite-translator-api) in order to collect dialogues and translate them. It also uses the [iolave/osrs-translations](https://github.com/iolave/osrs-translations) repo in order to download the latest translations.

![Architecture](https://raw.githubusercontent.com/iolave/runelite-the-translator/master/resources/architecture.png)

## License

This project is licensed under the [BSD-2-Clause license](https://raw.githubusercontent.com/iolave/runelite-the-translator/master/LICENSE).