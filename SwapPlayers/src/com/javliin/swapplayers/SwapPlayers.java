package com.javliin.swapplayers;

import net.minecraft.server.v1_8_R3.EnumProtocolDirection;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.HashMap;

public class SwapPlayers {

    private static SwapPlayers instance;
    private Plugin plugin;
    private HashMap<SwapNetworkManagerProxy, SwapNetworkManagerProxy> swapMap;

    public SwapPlayers(Plugin plugin) {
        instance = this;
        this.plugin = plugin;
        this.swapMap = new HashMap<>();

        new SwapPlayersListener(plugin);

        PlayerConnection playerConnection;
        for(Player player : Bukkit.getOnlinePlayers()) {
            try {
                playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

                injectNetworkManager(playerConnection, playerConnection.getClass().getDeclaredField("networkManager"), new SwapNetworkManagerProxy(EnumProtocolDirection.SERVERBOUND));
            }catch(NoSuchFieldException | IllegalAccessException exception) {
                exception.printStackTrace();
            }
        }
    }

    // Swap two players.
    public void swap(Player playera, Player playerb) {
        swapMap.put(SwapPlayersUtil.playerToNMP(playera), SwapPlayersUtil.playerToNMP(playerb));
        swapMap.put(SwapPlayersUtil.playerToNMP(playerb), SwapPlayersUtil.playerToNMP(playera));

        try {
            SwapPlayersUtil.playerToNMP(playera).updateMap(SwapPlayersUtil.playerToNMP(playerb));
            SwapPlayersUtil.playerToNMP(playerb).updateMap(SwapPlayersUtil.playerToNMP(playera));
        }catch(NullPointerException exception) {
            plugin.getLogger().severe("[SwapPlayers] Can\'t update map as PlayerConnection has for some reason not been injected!");
            exception.printStackTrace();
        }
    }

    // Unswap all players.
    public void unswapAll() {
        for(SwapNetworkManagerProxy nmp : swapMap.keySet()) {
            swapMap.put(nmp, nmp);

            try {
                nmp.updateMap(nmp);
            }catch(NullPointerException exception) {
                plugin.getLogger().severe("[SwapPlayers] Can\'t update map as PlayerConnection has for some reason not been injected!");
                exception.printStackTrace();
            }
        }
    }

    // Handle player disconnect while in swap.
    void disconnectUnswap(SwapNetworkManagerProxy swapNetworkManagerProxy) {
        swapMap.put(swapMap.get(swapNetworkManagerProxy), null);
        swapMap.get(swapNetworkManagerProxy).updateMap(swapMap.get(swapNetworkManagerProxy));
        swapMap.put(swapNetworkManagerProxy, null);
    }

    // Call main method with provided player's PlayerConnection.
    void injectNetworkManager(Player player) throws NoSuchFieldException, IllegalAccessException {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        injectNetworkManager(playerConnection, playerConnection.getClass().getDeclaredField("networkManager"), new SwapNetworkManagerProxy(EnumProtocolDirection.SERVERBOUND));
    }

    // Check if provided SwapNetworkManagerProxy has a partner.
    Boolean hasSwapPartner(SwapNetworkManagerProxy swapNetworkManagerProxy) {
        return swapMap.get(swapNetworkManagerProxy) != null;
    }

    SwapNetworkManagerProxy getSwapPartner(SwapNetworkManagerProxy swapNetworkManagerProxy) {
        return swapMap.get(swapNetworkManagerProxy);
    }

    // Copy methods of former NetworkManager to our new SwapNetworkManagerProxy, then inject.
    private void injectNetworkManager(PlayerConnection playerConnection, Field networkManager, SwapNetworkManagerProxy proxy) throws NoSuchFieldException, IllegalAccessException {
        SwapPlayersUtil.copyFields(playerConnection.networkManager, proxy);
        networkManager.set(playerConnection, proxy);
        swapMap.put(proxy, null);
    }

    // Get instance for SwapNetworkManagerProxy access.
    static SwapPlayers getInstance() {
        return instance;
    }
}
