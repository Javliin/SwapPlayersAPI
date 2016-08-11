package main.java.com.javliin.swapplayers;

import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

class SwapPlayersUtil {
    static void copyFields(Object from, Object to) throws NoSuchFieldException, IllegalAccessException {
        for(Field field : from.getClass().getFields()) {
            to.getClass().getDeclaredField(field.getName()).set(to, field.get(from));
        }
    }

    static SwapNetworkManagerProxy playerToNMP(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;

        if(!(playerConnection.networkManager instanceof SwapNetworkManagerProxy))
            return null;

        return (SwapNetworkManagerProxy) playerConnection.networkManager;
    }
}
