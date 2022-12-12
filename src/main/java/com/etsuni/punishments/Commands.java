package com.etsuni.punishments;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class Commands implements CommandExecutor {

    private final Punishments plugin;

    public Commands(Punishments plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (command.getName().equalsIgnoreCase("ban") || command.getName().equals("mute") || command.getName().equalsIgnoreCase("blacklist")) {
                if (args.length > 0) {

                    Punishment punishment = new Punishment(plugin);
                    OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);

                    if (args.length > 1) {

                        String args1 = args[1];
                        String keyword = "";
                        PunishmentType type = null;

                        if (args1 != null) {
                            if (punishment.isDuration(args1)) {
                                String reason = punishment.isReason(args, false);
                                if (reason.equals("")) {
                                    sender.sendMessage(ChatColor.RED + "Please specify a reason for the punishment!");
                                    return false;
                                }

                                if (command.getName().equalsIgnoreCase("ban")) {
                                    keyword = "banned";
                                    type = PunishmentType.TEMPBAN;
                                } else if (command.getName().equalsIgnoreCase("mute")) {
                                    keyword = "muted";
                                    type = PunishmentType.TEMPMUTE;
                                } else if (command.getName().equalsIgnoreCase("blacklist")) {
                                    keyword = "blacklisted";
                                    type = PunishmentType.BLACKLIST;
                                }

                                punishment.punishPlayer(type, ((Player) sender).getUniqueId().toString(), p.getUniqueId().toString(), args1, reason, keyword);
                            } else {
                                String reason = punishment.isReason(args, true);

                                if (reason.equals("")) {
                                    sender.sendMessage(ChatColor.RED + "Please specify a reason for the punishment!");
                                    return false;
                                }

                                if (command.getName().equalsIgnoreCase("ban")) {
                                    keyword = "banned";
                                    type = PunishmentType.PERMABAN;
                                } else if (command.getName().equalsIgnoreCase("mute")) {
                                    keyword = "muted";
                                    type = PunishmentType.MUTE;
                                } else if (command.getName().equalsIgnoreCase("blacklist")) {
                                    keyword = "blacklisted";
                                    type = PunishmentType.BLACKLIST;
                                }

                                punishment.punishPlayer(type, ((Player) sender).getUniqueId().toString(), p.getUniqueId().toString(), null, reason, keyword);
                            }
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Please specify a duration (don't specify one if perma punishment) ex: 1mo5d2h12m30s and a reason!");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Please specify a player, duration (don't specify one if perma punishment), and a reason!");
                }
            }

            //UNBAN COMMAND
            else if (command.getName().equalsIgnoreCase("unban")) {
                if (args.length > 0) {

                    OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[0]);
                    UUID uuid = targetPlayer.getUniqueId();
                    Punishment punishment = new Punishment(plugin);

                    if (punishment.getPunishmentType(uuid.toString()).equals(PunishmentType.TEMPBAN.toString()) ||
                            punishment.getPunishmentType(uuid.toString()).equals(PunishmentType.PERMABAN.toString())) {

                        if (punishment.unPunish(uuid.toString()) == null) {
                            sender.sendMessage(ChatColor.RED + "Could not find target player or player is already unbanned!");
                        } else if (punishment.unPunish(uuid.toString()).wasAcknowledged()) {
                            sender.sendMessage(ChatColor.GREEN + "You have unbanned " + targetPlayer.getName());
                        }

                    }
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "Please specify a player's name");
                }
            }

            //UNMUTE COMMAND
            else if (command.getName().equalsIgnoreCase("unmute")) {
                if (args.length > 0) {

                    OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[0]);
                    UUID uuid = targetPlayer.getUniqueId();
                    Punishment punishment = new Punishment(plugin);

                    if (punishment.getPunishmentType(uuid.toString()).equals(PunishmentType.TEMPMUTE.toString()) ||
                            punishment.getPunishmentType(uuid.toString()).equals(PunishmentType.MUTE.toString())) {

                        if (punishment.unPunish(uuid.toString()) == null) {
                            sender.sendMessage(ChatColor.RED + "Could not find target player or player is already unmuted!");
                        } else if (punishment.unPunish(uuid.toString()).wasAcknowledged()) {
                            sender.sendMessage(ChatColor.GREEN + "You have unmuted " + targetPlayer.getName());
                        }

                    }
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "Please specify a player's name");
                }
            }

            //KICK COMMAND
            else if (command.getName().equalsIgnoreCase("kick")) {
                if (args.length > 0) {

                    Player targetPlayer = Bukkit.getPlayer(args[0]);
                    UUID uuid = targetPlayer.getUniqueId();
                    Punishment punishment = new Punishment(plugin);

                    if (args.length > 1) {

                        String args1 = args[1];

                        if (args1 != null) {
                            String reason = punishment.isReason(args, true);
                            if (reason.equals("")) {
                                sender.sendMessage(ChatColor.RED + "Please specify a reason for the punishment!");
                                return false;
                            }
                            punishment.punishPlayer(PunishmentType.KICK, ((Player) sender).getUniqueId().toString(), uuid.toString(), args1, reason, "kicked");
                        }
                    } else {
                        sender.sendMessage(ChatColor.DARK_RED + "Please specify a player's name");
                    }
                }
            }
        }
        return false;
    }
}

