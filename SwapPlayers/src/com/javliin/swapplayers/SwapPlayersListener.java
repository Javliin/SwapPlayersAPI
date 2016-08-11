package com.javliin.swapplayers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class SwapPlayersListener implements Listener {
    SwapPlayersListener(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /*
    Use highest priority to assure we can inject our custom NetworkManager first.
    There are a million better ways to perform this, but most involve going very deep into spigot, like using a PlayerList or ServerConnection proxy.
    Since we're only using this to proxy our NetworkManager, I'd rather we use this instead of doing dangerous things like using a ServerConnection proxy.
    */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        try {
            SwapPlayers.getInstance().injectNetworkManager(event.getPlayer());
        }catch(NoSuchFieldException | IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }
}
