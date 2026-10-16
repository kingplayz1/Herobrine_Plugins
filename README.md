# HerobrineHaunting
A configurable Minecraft server plugin that brings the Herobrine creepypasta to life.

## Features
- Intelligent haunting system with multiple stages (observation, evidence, manifestation, etc.)
- Configurable world anomalies (structures, tunnels, leaf removal)
- Customizable audio cues and sound profiles
- Player opt-out and administrative controls
- Persistent encounter history and world change journal (SQLite)
- Safety features to prevent griefing and protect player builds
- Compatible with Paper and Purpur servers

## Installation
1. Ensure you are running a Paper or Purpur server for Minecraft 1.20.4-R0.1-SNAPSHOT (or compatible version).
2. Download the latest release of `HerobrineHaunting.jar`.
3. Place the jar in your server's `plugins` directory.
4. Start the server to generate the default configuration.
5. Edit `plugins/HerobrineHaunting/config.yml` to adjust settings as desired.
6. Restart or reload the server.

## Commands
- `/herobrine help` - Show available commands
- `/herobrine status` - View current haunting status
- `/herobrine enable` - Enable haunting events (Admin)
- `/herobrine disable` - Disable haunting events (Admin)
- `/herobrine optout` - Toggle personal haunting encounters (Player)
- `/herobrine spawn <player>` - Force a test encounter (Admin)
- `/herobrine despawn <player|all>` - End active encounters (Admin)
- `/herobrine reload` - Reload configuration from file (Admin)

## Building from Source
This project uses Gradle to build. To build the plugin:

1. Install JDK 21 or higher.
2. Install Gradle (https://gradle.org/install/).
3. Clone this repository.
4. Run `gradle build` in the project root.
5. The compiled jar will be in `build/libs/`.

## Configuration
See `config.yml` for all available options. The configuration is divided into sections:
- `settings`: General plugin settings
- `haunting`: Core haunting behavior and encounter rules
- `appearance`: Herobrine's visual presentation
- `ai`: Artificial intelligence parameters
- `combat`: Combat-related settings (disabled by default)
- `world`: World modification and anomaly settings
- `audio`: Sound and music settings
- `safety`: Server protection and emergency features
- `storage`: Database configuration

## License
This plugin is protected by the HerobrineHaunting Plugin License Agreement. 

**You are allowed to:**
- Use this plugin on your own Minecraft server(s) for personal/non-commercial entertainment
- Run the plugin as-is without modification

**You may NOT:**
- Modify, adapt, translate, or create derivative works based on this plugin
- Decompile, disassemble, or reverse engineer this plugin
- Remove, alter, or obscure any copyright notices or proprietary legends
- Distribute, sell, license, or otherwise transfer this plugin to any third party
- Use this plugin in any commercial service or public server without explicit written permission
- Claim ownership or authorship of this plugin

See the full LICENSE file for complete terms and conditions.

## Warning
This plugin is intended for entertainment purposes only. Herobrine is a fictional entity from Minecraft creepypasta. The plugin does not grant any supernatural powers or affect your computer or account outside of the Minecraft server.