package com.etsuni.punishments;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.Inventory;

import java.util.UUID;


public class Events implements Listener {

    private final Punishments plugin;

    public Events(Punishments plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Punishment punishment = new Punishment(plugin);
        if(punishment.getPunishmentType(uuid.toString()) == null) {
            return;
        }

        if(punishment.getPunishmentType(uuid.toString()).equalsIgnoreCase(PunishmentType.TEMPBAN.toString())
        || punishment.getPunishmentType(uuid.toString()).equalsIgnoreCase(PunishmentType.PERMABAN.toString())) {
            if(!punishment.isPunishmentExpired(uuid.toString())){
                event.disallow(PlayerLoginEvent.Result.KICK_BANNED, "You are banned for: " + punishment.getPunishmentReason(uuid.toString()));
            }
        } else if(punishment.getPunishmentType(uuid.toString()).equalsIgnoreCase(PunishmentType.BLACKLIST.toString())) {
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, "You are blacklisted in this server");
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Punishment punishment = new Punishment(plugin);

        if(!punishment.isPunishmentExpired(player.getUniqueId().toString())) {
            if(punishment.getPunishmentType(player.getUniqueId().toString()).equalsIgnoreCase(PunishmentType.MUTE.toString())
            || punishment.getPunishmentType(player.getUniqueId().toString()).equalsIgnoreCase(PunishmentType.TEMPMUTE.toString())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You are currently muted for " +
                        "\"" + punishment.getPunishmentReason(player.getUniqueId().toString()) + "\"");
            }
        }
    }



    //For history gui
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if(!inventory.getType().equals(InventoryType.CHEST)){
            return;
        }
        if(inventory.getItem(0) == null) {
            return;
        }
        if(!inventory.getItem(0).getType().equals(Material.PLAYER_HEAD) && !inventory.getItem(0).hasItemMeta()) {
            return;
        }
        event.setCancelled(true);
    }

}
