# SkinPicker

A lightweight Paper plugin that lets players change their displayed skin without relogging.

## Features

- `/skin <player>` — wear another player's skin
- `/skin random` — get a random skin
- `/skin reset` — restore your own skin
- Skins persist across joins, with per-player cooldowns and a texture cache

## Install

1. Drop `SkinPicker-3.1.7.jar` into your server's `plugins` folder.
2. Restart the server.
3. Tweak `config.yml` to taste (cooldowns, random pool, messages).

## Permissions

- `skinpicker.use` — use `/skin` (default: true)
- `skinpicker.others` — change other players' skins (default: op)
- `skinpicker.bypass-cooldown` — skip the skin cooldown (default: op)

## Requirements

Paper 1.21.x, Java 21.

## Source

`src/` contains the decompiled release sources for reference.
