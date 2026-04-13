# 🎵 SoundCreator

**Play custom audio files on your server through PlasmoVoice — MP3, OGG and WAV with a single command.**

SoundCreator is a Spigot plugin that lets admins broadcast sound files from the server directly to players using [PlasmoVoice](https://modrinth.com/plugin/plasmo-voice)'s audio infrastructure. Drop your audio files in a folder, and stream them to one player, or everyone on the server — no resource pack required.

---

## ✨ Features

- 🎧 **Multi-format support** — Plays **MP3**, **OGG/Vorbis**, and **WAV** files natively (no conversion needed)
- 📡 **PlasmoVoice integration** — Audio is encoded to Opus and streamed through the voice mod's broadcast source system
- 🗃️ **Smart cache** — PCM and Opus frames are cached in memory for instant replay after the first load; all sounds are preloaded on startup
- 🎯 **Flexible targeting** — Play sounds to yourself, a specific player, or all connected players
- 🔢 **Concurrent sound limit** — Optional cap on simultaneous playback sessions to protect server performance
- 🔄 **Hot-reload** — Reload the sound library and config without restarting the server
- 🔒 **Granular permissions** — Individual nodes for play, play-to-others, play-to-all, stop, and reload
- ⌨️ **Tab completion** — Full tab-complete support, including quoted sound names with spaces

---

## 🔧 Commands

| Command | Description |
|---|---|
| `/sc play <sound> [player\|all]` | Play a sound to yourself, a specific player, or everyone |
| `/sc stop [sound\|all]` | Stop one sound (by name) or all active playback sessions |
| `/sc list` | List all available sounds loaded from the sounds folder |
| `/sc reload` | Reload config and sound library, then preload cache |

Alias: `/soundcreator`

---

## 🔑 Permissions

| Permission | Default | Description |
|---|---|---|
| `soundcreator.use` | Everyone | Use `/sc list` |
| `soundcreator.play` | OP | Play sounds to yourself |
| `soundcreator.play.others` | OP | Play sounds to a specific player |
| `soundcreator.play.all` | OP | Broadcast sounds to all players |
| `soundcreator.stop` | OP | Stop playback sessions |
| `soundcreator.reload` | OP | Reload config and sound library |
| `soundcreator.admin` | OP | Grants all of the above |

---

## ⚙️ Configuration (`config.yml`)

```yaml
# Subfolder inside plugins/SoundCreator/ where audio files are scanned
sounds-folder: sounds

# PlasmoVoice source line to use (default line: "proximity")
source-line: proximity

# Default playback volume (0.0 – 1.0)
default-volume: 1.0

# Silently skip players who don't have PlasmoVoice installed
skip-non-voice-players: true

# Maximum simultaneous sounds (0 = unlimited)
max-concurrent-sounds: 10
```

---

## 📦 Installation

1. Install [PlasmoVoice](https://modrinth.com/plugin/plasmo-voice) on your server.
2. Drop `SoundCreator.jar` into your `plugins/` folder.
3. Start the server — the `plugins/SoundCreator/sounds/` folder will be created automatically.
4. Copy your `.mp3`, `.ogg`, or `.wav` files into the `sounds/` folder.
5. Use `/sc reload` (or restart) and you're ready to go!

> **Note:** Players must have the PlasmoVoice mod installed on their client to hear sounds. Players without the mod are skipped silently.

---

## 📋 Requirements

| Requirement | Version |
|---|---|
| Minecraft | 1.21+ |
| Server software | Spigot / Paper |
| Java | 21+ |
| PlasmoVoice | 2.1.8+ |

---

## 🛠️ Technical Details

- Audio files are decoded to raw 16-bit PCM using `mp3spi`, `vorbisspi`, and standard Java Sound SPI
- PCM is resampled to 48 kHz mono and encoded to Opus frames (960 samples/frame) using PlasmoVoice's encoder
- All libraries (javazoom, tritonus) are **shaded and relocated** inside the jar — no external dependencies required on the server beyond PlasmoVoice
- Playback runs **asynchronously** to avoid blocking the main thread

---

*Made by **ELB_GG***
