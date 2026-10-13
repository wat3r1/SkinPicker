# SkinPicker

A lightweight skin utility for **Paper / Spigot (1.21+)** on **Java 21**.

Change your displayed skin with Mojang textures, without relogging.

## Commands

| Command | Usage | Description |
| :--- | :--- | :--- |
| `/skin <player>` | `/skin Notch` | Preview another player's skin on yourself |
| `/skin reset` | `/skin reset` | Restore your own skin |
| `/skin random` | `/skin random` | Apply a random skin from the configured pool |
| `/skinpicker` | `/skinpicker` | Plugin info |
| `/shelp` | `/shelp` | Show skin command help |

Aliases: `/skins` works the same as `/skin`.

## Permissions

- `skinpicker.use` (default: true) — use `/skin` for yourself
- `skinpicker.others` (default: op) — change other players' skins
- `skinpicker.bypass-cooldown` (default: op) — bypass the skin change cooldown

## Configuration (`plugins/SkinPicker/config.yml`)

```yaml
cooldown-seconds: 60
allow-reset: true
allow-random: true
random-skins:
  - "Notch"
  - "jeb_"
```

## Installation

1. Copy `target/SkinPicker-1.0.0.jar` into your server's `plugins/` directory.
2. Restart the server.
3. Use `/skin <player>`.
