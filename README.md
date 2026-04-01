# Panda Unique Item

Fabric mod for Minecraft 26.1.x that adds configurable lore to items when they are forged or renamed in an anvil. Lore can list the player name and date; format strings in `config/PandaUniqueItem.json` use `%player_name%` and `%date%`, with optional Placeholder API-style tags (for example `<white>` and `<gold>`) when `loreFormat` is set.

Build with Java 25 and Gradle (`./gradlew build`). Place the built jar in your mods folder alongside Fabric Loader and Fabric API.
