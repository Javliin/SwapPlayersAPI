package com.javliin.swapplayers;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;

import java.lang.reflect.Field;

public class SwapNetworkManagerProxy extends NetworkManager {

    private Location location;
    private Container container;
    private Integer dimension;

    SwapNetworkManagerProxy(EnumProtocolDirection enumprotocoldirection) {
        super(enumprotocoldirection);
    }

    // Send packets, forward them to our swap partner if it exists.
    @Override
    public void handle(Packet packet) {
        if(SwapPlayers.getInstance().hasSwapPartner(this)) {
            if(packet instanceof PacketPlayOutUpdateHealth)
                try {
                    Field health = PacketPlayOutUpdateHealth.class.getDeclaredField("a");
                    if (health.get(packet) == 0) {
                        SwapPlayers.getInstance().swap(
                            ((PlayerConnection)this.getPacketListener()).getPlayer().getPlayer(),
                            ((PlayerConnection)SwapPlayers.getInstance().getSwapPartner(this).getPacketListener()).getPlayer().getPlayer());
                    }

                    super.handle(packet);
                    return;
                }catch(NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }

            if(packet instanceof PacketPlayInBlockDig) {
                PacketPlayInBlockDig packetPlayInBlockDig = (PacketPlayInBlockDig) packet;
                if(packetPlayInBlockDig.c() == PacketPlayInBlockDig.EnumPlayerDigType.DROP_ITEM || packetPlayInBlockDig.c() == PacketPlayInBlockDig.EnumPlayerDigType.DROP_ALL_ITEMS) {
                    handle(new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"You may not drop items when swapped!\",\"color\":\"red\"}")), this);
                    return;
                }
            }

            SwapPlayers.getInstance().getSwapPartner(this).handle(packet, this);
            return;
        }

        super.handle(packet);
    }

    // Recieve packets, forward them to our swap partner if it exists.
    @Override
    public void a(ChannelHandlerContext channelhandlercontext, Packet packet) throws Exception {
        if(SwapPlayers.getInstance().hasSwapPartner(this)) {
            SwapPlayers.getInstance().getSwapPartner(this).a(channelhandlercontext, packet, this);
            return;
        }

        super.a(channelhandlercontext, packet);
    }

    //
    @Override
    public void l() {
        if (SwapPlayers.getInstance().hasSwapPartner(this))
            SwapPlayers.getInstance().disconnectUnswap(this);
    }

    // New method to handle with source, so we don't ping pong the packets.
    void handle(Packet packet, SwapNetworkManagerProxy source) {
        super.handle(packet);
    }

    // New method to handle with source, so we don't ping pong the packets.
    void a(ChannelHandlerContext channelhandlercontext, Packet packet, SwapNetworkManagerProxy source) throws Exception {
        super.a(channelhandlercontext, packet);
    }

    // Give our player new info of our new swap partner's player.
    void updateMap(SwapNetworkManagerProxy nmpPartner) {
        PlayerConnection playerConnection = (PlayerConnection) this.getPacketListener();
        location = playerConnection.getPlayer().getLocation();
        container = playerConnection.player.activeContainer;
        dimension = playerConnection.player.dimension;

        if(nmpPartner.equals(this)) {
            playerConnection.teleport(location);
            playerConnection.player.updateInventory(container);

            // Reload chunks.
            MinecraftServer.getServer().getWorldServer(playerConnection.player.dimension).removeEntity(playerConnection.player);
            MinecraftServer.getServer().getWorldServer(playerConnection.player.dimension).addEntity(playerConnection.player);
            return;
        }

        PlayerConnection partnerPlayerConnection = (PlayerConnection) nmpPartner.getPacketListener();
        Location partnerLocation = partnerPlayerConnection.getPlayer().getLocation();
        Container partnerContainer = partnerPlayerConnection.player.activeContainer;
        Integer partnerDimension = partnerPlayerConnection.player.dimension;

        if(nmpPartner.getLocation() != null || nmpPartner.getContainer() != null || nmpPartner.getDimension() != null) {
            partnerLocation = nmpPartner.getLocation();
            partnerContainer = nmpPartner.getContainer();
            partnerDimension = nmpPartner.getDimension();
        }

        partnerPlayerConnection.teleport(partnerLocation);
        partnerPlayerConnection.player.updateInventory(partnerContainer);
        MinecraftServer.getServer().getWorldServer(partnerDimension).removeEntity(partnerPlayerConnection.player);
        MinecraftServer.getServer().getWorldServer(partnerDimension).addEntity(partnerPlayerConnection.player);
    }

    // Local storage of original values before swap.
    Location getLocation() { return location; }
    Container getContainer() { return container; }
    Integer getDimension() { return dimension; }
}
