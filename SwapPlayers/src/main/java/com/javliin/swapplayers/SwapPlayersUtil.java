package main.java.com.javliin.swapplayers;

import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

class SwapPlayersUtil {
    static void copyFields(Object from, Object to) throws NoSuchFieldException, IllegalAccessException {
        Field migrate;
        for(Field field : from.getClass().getDeclaredFields()) {
            migrate = to.getClass().getDeclaredField(field.getName());
            if(!field.isAccessible()) {
                migrate.setAccessible(true);
                migrate.set(to, field.get(from));
                migrate.setAccessible(false);
            }
            
            migrate.set(to, field.get(from));
        }
    }

    static SwapNetworkManagerProxy playerToNMP(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;

        if(!(playerConnection.networkManager instanceof SwapNetworkManagerProxy))
            return null;

        return (SwapNetworkManagerProxy) playerConnection.networkManager;
    }
}
