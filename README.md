# Animal Capture by Acronical

## Info
This is a plugin for Minecraft Servers running Spigot/Paper that was originally made for [Yrrah](https://linktr.ee/Yrrah) and the [Outcasters](https://www.beacons.ai/outcasts) on the Outcasts SMP. If you use this plugin in your own content, please credit me by linking this repository and [my website](https://acronical.is-a.dev/projects).

Initialising the plugin creates the red and blue teams, and assigns players randomly to each team so the server is split 50/50. Anyone on a team called `admin` are excluded from the random assignment. Players can then capture animals by right-clicking them, then they can bring them back to their base to score points. All entities are worth 1 point except for the frog called <b>Clyde</b>, who is worth 10 points.

When the player is carrying an entity back to their team's base, they get the slowness 2 effect. Players can be hit to make them drop the entity they are carrying, and entities can be picked up by other players. Players will get stunned with slowness 255 and blindness for 2 seconds if they are hit while carrying an entity.

When a point is scored, the entity is removed from the player and will respawn on 2 minute intervals, <b>Clyde</b> respawns every 5 minutes. A scoreboard will display the number of mobs scored by a team and either of the `/captureannounce` or `/captureprogress` (see below) commands can be used to display the current team scores.
## Commands
- `/capturehelp` - Displays a list of commands.
- `/captureinit` - Initialises the game, creating the teams and assigning players to them.
- `/capturestart` - Starts the game, teleporting players to the arena.
- `/capturestop` - Stops the game, teleporting players back to the spawn.
- `/captureannounce` - Announces the winning team to all players.
- `/capturereset` - Resets the game, clearing the teams and scores.
- `/captureprogress` - Displays the current scores of the game to you.

## Permissions
- `animalcapture.*` - Grants access to all Animal Capture commands.
- `animalcapture.capturehelp` - Grants access to the `/capturehelp` command.
- `animalcapture.captureinit` - Grants access to the `/captureinit` command.
- `animalcapture.capturestart` - Grants access to the `/capturestart` command.
- `animalcapture.capturestop` - Grants access to the `/capturestop` command.
- `animalcapture.captureannounce` - Grants access to the `/captureannounce` command.
- `animalcapture.capturereset` - Grants access to the `/capturereset` command.
- `animalcapture.captureprogress` - Grants access to the `/captureprogress` command.

## Installation
1. Download the world file from the [releases page](https://github.com/OutcastsEvents/AnimalCapture/releases/stable).
2. Replace the world file in your server's `world` folder with the world downloaded.
3. Download the latest release from the [releases page](https://github.com/OutcastsEvents/AnimalCapture/releases/stable).
4. Place the JAR file in your server's `plugins` folder.
5. Start your server.

<h3 align="left">Support Me:</h3>
<p><a href="https://ko-fi.com/acronical"> <img align="left" src="https://cdn.ko-fi.com/cdn/kofi3.png?v=3" height="50" width="210" alt="acronical" /></a></p><br><br>
