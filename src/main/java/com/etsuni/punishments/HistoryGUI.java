package com.etsuni.punishments;

import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class HistoryGUI implements CommandExecutor {

    private final Punishments plugin;

    public HistoryGUI(Punishments plugin) {
        this.plugin = plugin;
    }


    public Inventory historyGUI(UUID uuid) {
        Inventory inv = Bukkit.createInventory(null, 54, Bukkit.getOfflinePlayer(uuid).getName() + "'s Punishments");

        Document find = new Document("uuid", uuid.toString());
        FindIterable<Document> finds = plugin.getCollection().find(find);
        if(finds == null) {
            return inv;
        }

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setDisplayName(Bukkit.getOfflinePlayer(uuid).getName());
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        for(Document doc : finds) {
            List<Document> list = doc.getList("punishments", Document.class);
            int j = 0;
            for(int i = list.size() - 1; i >= 0; i--) {
                List<String> lore = new ArrayList<>();
                lore.add("Punishment: " + list.get(i).getString("type"));
                lore.add("Date issued: " + list.get(i).getDate("date_issued").toString());
                lore.add("Reason: " + list.get(i).getString("reason"));
                if(list.get(i).get("expiration") instanceof Date) {
                    lore.add("Duration: " + list.get(i).getString("duration"));
                    String expiration = list.get(i).get("expiration").toString();
                    lore.add("Expiration Date: " + expiration);
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
                inv.addItem(item);
            }
        }



        return inv;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            if(command.getName().equalsIgnoreCase("history")) {
                if(args.length > 0) {
                    Player player = ((Player) sender).getPlayer();
                    OfflinePlayer lookupPlayer = Bukkit.getOfflinePlayer(args[0]);

                    player.openInventory(historyGUI(lookupPlayer.getUniqueId()));
                } else{
                    sender.sendMessage(ChatColor.RED + "Please specify a player's name!");
                }
            }
        }
        return false;
    }
}
