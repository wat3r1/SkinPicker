# SkinPicker

SkinPicker lets players change the skin shown on a Paper server without logging out. It fetches signed textures from Mojang and applies them to the selected online player.

## Requirements

- Paper 1.21.11 or a compatible version
- Java 21

## Commands

| Command | What it does |
| --- | --- |
| `/skin <name>` | Show that player's skin on yourself. |
| `/skin reset` | Restore your original skin. |
| `/skin random` | Choose a skin from the configured random pool. |
| `/skin <target> <name>` | Set another online player's displayed skin. |
| `/skin reset <target>` | Restore another online player's original skin. |
| `/skin random <target>` | Choose a random skin for another online player. |
| `/shelp` | Show command usage. |

`/skins` is an alias for `/skin`.

Console usage: `/skin <online-player> <name|reset|random>`.

## Permissions

| Permission | Default | Purpose |
| --- | --- | --- |
| `skinpicker.use` | Everyone | Use skin commands. |
| `skinpicker.others` | Operators | Change another player's skin. |
| `skinpicker.bypass-cooldown` | Operators | Skip the skin-change cooldown. |

## Configuration

Edit `plugins/SkinPicker/config.yml`. The default cooldown is 3 seconds; the random pool, cache duration, reset and random commands, saved selections, and permission nodes can also be configured there.

```yaml
skins:
  cooldown-seconds: 3
  cache-minutes: 60
  allow-reset: true
  allow-random: true
  random-pool:
    - "Notch"
    - "jeb_"
  persist-across-joins: true
```

Messages and their color formatting can be customized in the same file.

## Install

1. Download `SkinPicker-1.0.0.jar` from the repository or build it with `mvn package`.
2. Put the JAR in the server's `plugins` folder.
3. Start the server and edit `plugins/SkinPicker/config.yml` if needed.

## Build

```sh
mvn package
```

The built plugin is written to `target/SkinPicker-1.0.0.jar`.
