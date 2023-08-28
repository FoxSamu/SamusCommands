# Samū's Commands (ModID: `rfx-cmd`)

A small mod for Minecraft 1.20.x that adds several small little gameplay features. The mod is made for Fabric and requires the Fabric API. 

The mod has both client and server features, but operates in such a way that clients don't need to have the mod if the server has it, and vice versa. The mod mostly adds some commands and is most useful on the server, but the client adds some neat little features as well (especially if the server has the mod too).

# Server Features

All commands and gamerules listed here are automatically shared with all clients, so all players have access to this whether they have the mod or not.

## Homes
The mod adds the well known `/home` and `/sethome` commands. You have a main home which you can use by calling the base commands with no extra name. You can add more homes under names by calling the commands with a specific name.
- `/home` (alias `/h`) teleports where you last called `/sethome`
- `/sethome` (alias `/sh`) sets your home to the current location
- `/delhome` deletes your home
- `/gethome` (alias `/gh`) gets your home's coordinates, or just whether you have the home when the gamerule `reducedDebugInfo` is set to `true`
- `/homes` lists your homes, and coordinates if gamerules allow
- `/clearhomes` removes all your named homes, except your main one
- `/clearhomes true` removes all your homes, including your main one

Homes can be controlled by gamerules:
- `/gamerule allowHomeTp true|false` (default `true`) enables or disables all home commands. When `false`, only operators have access to home commands. This gamerule is also not effective to players with cheats enabled in singleplayer worlds.
- `/gamerule maxHomes <nr>` (default `0`) sets the maximum amount of homes a player can have, whether that is a named home or a main home. The value `0` disables the limit. If a player has more homes, they must first remove homes before they can set more homes. If a player has exactly this amount of homes, they can only overwrite existing homes. This command only affects `/sethome`, players that have more homes than this gamerule allows can still teleport to any of those. Admins and players with cheats enabled can make more homes.

Homes work from any distance and through different dimensions. A home set in the Nether allows players in the Overworld to teleport to that home in the Nether instantly.

A useful feature: home names can be clicked in the chat and it will suggest either a `/home` or `/sethome` command based on whether the home existed when it was printed or not.

## Spawn teleportation
The mod adds a simple command to let players teleport to the world spawn point in the Overworld.
- `/spawn` (alias `/s`)

This behaviour can be enabled or disabled with a gamerule:
- `/gamerule allowSpawnTp true|false` (default `true`) enables or disables the `/spawn` command.

## Teleportation Requests
The mod adds the well known `/tpa` system, to send teleportation requests to players. It is controlled using a set of commands:
- `/tpa <player>` sends a teleportation request to `<player>`. If they accept it, you will be teleported to them.
- `/tpc <player>` sends a teleportation call to `<player>`. If they accept it, they will be teleported to you.
- `/tpaccept` accepts the last received teleportation request
- `/tpaccept <player>` accepts a teleportation request from `<player>`
- `/tpaccept *` accepts all teleportation requests and calls, use wisely since this may cause quite some chaos if you received a lot of requests and calls
- `/tpdeny` denies the last received teleportation request
- `/tpdeny <player>` denies a teleportation request from `<player>`
- `/tpdeny *` denies all teleportation requests

You can control who can send you requests using a blacklist and a whitelist. The whitelist is by default disabled but can be enabled. If the whitelist is enabled, nobody can send you a request unless they're whitelisted. Blacklisted players can never send you a request even if they're whitelisted. If the whitelist is disabled, everyone except those in your blacklist can request.
- `/tpblacklist add <player>` (alias `/tpbl add <player>`) adds a player to your TP blacklist
- `/tpblacklist remove <player>` (alias `/tpbl remove <player>`) removes a player from your TP blacklist
- `/tpwhitelist add <player>` (alias `/tpwl add <player>`) adds a player to your TP whitelist
- `/tpwhitelist remove <player>` (alias `/tpwl remove <player>`) removes a player from your TP whitelist
- `/tpwhitelist enable` (alias `/tpwl enable`) enables your whitelist, blocking everyone unless they're whitelisted
- `/tpwhitelist disable` (alias `/tpwl disable`) disables your whitelist, blocking nobody unless they're blacklisted
- `/tpwhitelist toggle` (alias `/tpwl toggle`) toggles your whitelist, enabling it if it is disabled and disabling it if it is enabled

Teleportation requests can be controlled with several gamerules:
- `/gamerule allowTpa true|false` (default `true`) enables or disables all teleportation request commands for everyone, even admins and players with cheats (they have the regular `/tp` command after all, and players could not accept otherwise).
- `/gamerule tpaRequestTimeout 1..3600` (default `300`) defines, in seconds, the amount of time before a teleportation request expires. A request must be accepted within this timeframe, otherwise it will be denied silently (with no message to source or target).
- `/gamerule tpaRequestSameTeam true|false` (default `false`) sets whether teleportation requests may only be sent to players in the same team. This also considers players that are not in a team to be in a single "not in a team"-team so they can still send requests to eachother. If disabled, everyone can send requests to everyone.
- `/gamerule tpaMode tpa|tpc|both` (default `both`) sets in which direction players may teleport eachother. If set to `tpa`, players may only teleport themselves to others (i.e. they can use `/tpa`). If set to `tpc`, players may only teleport others to themselves (i.e. they can use `/tpc`). If set to `both`, players may do both (i.e. they can use `/tpa` and `/tpc`).

## Personal Notes
Players can write small notes for themselves, which are stored per world (on the server's side). Notes are stored a bunch of lines which the player may edit, move, remove and add to freely.
- `/notes add <note>` (alias `/note <note>`) adds a new note with the given text
- `/notes edit <ln> <note>` edits the note at line `<ln>` and replaces the text with the given text
- `/notes remove all` removes all notes
- `/notes remove <ln>` removes one specific note
- `/notes remove <from> <to>` removes a range of notes
- `/notes move <from> <to>` moves a note from one line to another
- `/notes last` (alias `/note`) shows the note at the last line number
- `/notes last <nr>` shows the notes at the last `<nr>` line numbers
- `/notes get all` (alias `/notes`) lists all notes
- `/notes get <ln>` lists one specific note
- `/notes get <from> <to>` lists a range of notes

Notes allow the interpolation of some simple data points such as your coordinates or your dimension, if the gamerule `reducedDebugInfo` is set to `false`. Each of these insert the specific information at the location where they were put in the note. They are meant to make it easier to insert some information from the debug screen in your notes.
- `{xyz}` (alias `{pos}`, `{coords}`) inserts your coordinates in the world
- `{xz}` inserts your X and Z coordinates in the world
- `{x}` inserts your X coordinate in the world
- `{y}` inserts your Y coordinate in the world
- `{z}` inserts your Z coordinate in the world
- `{dimension}` (alias `{dim}`) inserts the dimension ID of the dimension you're in

Since notes are stored on the server side in the player data, a gamerule exists to limit the amount of notes per player in order to limit memory usage per player. Notes have a hardcoded length limit of 1024 characters, but the amount of notes per player is configurable.
- `/gamerule maxNotes 1..4096` (default `1024`) sets the maximum amount of notes a player can have. Changing it will not immediately remove notes from players.

A useful feature: note lines printed in chat can be clicked upon to copy a note line to the clipboard.


# Client Features

Currently, not much client features exist.

## Home Keys
You can bind keys to set and teleport to your main home.


# Planned Features

- A home manager: a visual interface showing your homes where you can manage them.
- Points of interest: shared locations that players can teleport to.
- Lodestone teleportation: allows players to click with a lode compass to teleport to the lodestone it's associated with.
- Death teleportation: allows players to click with a recovery compass to teleport to their last death.

# License

    Copyright (C) 2023 Runefox

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

