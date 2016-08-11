# SwapPlayersAPI
Small API to swap players.
## What is it?
API for Spigot / Bukkit that allows you to "swap" players on the server.
This should make the server handle each player as their swap partner once swapped.
## How do I use it?
1. Include the SwapPlayersAPI JAR in your project, it's an API, not a plugin.

2. Create an instance (note: not safe to create multiple instances) with your plugin:
```
SwapPlayers swapPlayers = new SwapPlayers(Plugin myPlugin);
```
Then swap!
```
swapPlayers.swap(Player playerA, Player playerB);
```
Don't worry, the API should handle everything else.
