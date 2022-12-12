package com.etsuni.punishments;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class Punishment {

    private final Punishments plugin;

    public Punishment(Punishments plugin) {
        this.plugin = plugin;
    }
    public void punishPlayer(PunishmentType type, String issuerUUID, String punisheeUUID, String duration, String reason, String keyword) {
        playerInDB(punisheeUUID);
        Document find = new Document("uuid", punisheeUUID);
        LocalDateTime now = LocalDateTime.now();

        Player sender = Bukkit.getPlayer(UUID.fromString(issuerUUID));
        OfflinePlayer punished = Bukkit.getOfflinePlayer(UUID.fromString(punisheeUUID));

        if(type == PunishmentType.KICK) {
            if(punished.isOnline()) {
                BasicDBObject punishments = new BasicDBObject("punishments", new BasicDBObject("date_issued", now)
                        .append("reason", reason)
                        .append("type", type)
                        .append("duration", "Permanent")
                        .append("expiration", "Permanent")
                        .append("expired", true));
                BasicDBObject update = new BasicDBObject("$push", punishments);
                plugin.getCollection().updateOne(find, update);
                punished.getPlayer().kickPlayer("You have been kicked for " + reason);
            } else{
                sender.sendMessage(ChatColor.RED + "That player is not online!");
            }
            return;
        }

        if(duration == null) {

            BasicDBObject punishments = new BasicDBObject("punishments", new BasicDBObject("date_issued", now)
                    .append("reason", reason)
                    .append("type", type)
                    .append("duration", "Permanent")
                    .append("expiration", "Permanent")
                    .append("expired", false));
            BasicDBObject update = new BasicDBObject("$push", punishments);
            plugin.getCollection().updateOne(find, update);

            sender.sendMessage(ChatColor.GREEN + "You have permanently " + keyword + " " + punished.getName());
            if(type == PunishmentType.PERMABAN || type == PunishmentType.TEMPBAN) {
                if(punished.isOnline()){
                    punished.getPlayer().kickPlayer("You have been permanently "+keyword+". Reason: " + reason);
                }
            }
        } else {
            if(parseDuration(duration) == null) {
                sender.sendMessage(ChatColor.RED + "Invalid duration format. Ex: 1mo15d20h30m25s");
                return;
            }
            BasicDBObject punishments = new BasicDBObject("punishments", new BasicDBObject("date_issued", now)
                    .append("reason", reason)
                    .append("type", type)
                    .append("duration", duration)
                    .append("expiration", parseDuration(duration))
                    .append("expired", false));
            BasicDBObject update = new BasicDBObject("$push", punishments);
            plugin.getCollection().updateOne(find, update);

            sender.sendMessage(ChatColor.GREEN + "You have temporarily " + keyword + " " + punished.getName() + " for " + duration);
            if(type == PunishmentType.PERMABAN || type == PunishmentType.TEMPBAN) {
                if(punished.isOnline()){
                    punished.getPlayer().kickPlayer("You have been temporarily "+keyword+" Reason: " + reason);
                }
            }
        }
    }

    public String getPunishmentReason(String uuid) {
        Document find = new Document("uuid", uuid.toString());
        String str = "";
        FindIterable<Document> finds = plugin.getCollection().find(find);
        if(finds.first() == null) {
            return null;
        }

        for(Document doc : finds) {
            List<Document> list = doc.getList("punishments", Document.class);
            str = list.get(list.size() - 1).getString("reason");
        }

        return str;
    }

    public void playerInDB(String uuid) {
        List<Document> punishmentsList = new ArrayList();
        Document find = new Document("uuid", uuid);
        Document document = new Document("uuid", uuid).append("punishments", punishmentsList);
        if(plugin.getCollection().find(find).first() == null) {
            plugin.getCollection().insertOne(document);
        }
    }

    public LocalDateTime parseDuration(String duration) throws NumberFormatException{

        String splitDuration = duration;
        char[] durationChars = duration.toCharArray();

        //Loop through string and put a '/' after letters: reason why this is here is in the loop below
        for(int i = duration.length() - 1; i >= 1; i--) {
            Character c = durationChars[i];
            if(!isNumeric(c)) {
                if(!isNumeric(durationChars[i - 1])) {
                    splitDuration = addChar(splitDuration, '/', i + 1);
                    i--;
                }
                else {
                    splitDuration = addChar(splitDuration, '/', i + 1);
                }
            }
        }

        String[] strArr = splitDuration.split("/");
        LocalDateTime time = LocalDateTime.now();

        //Loop through our previous loop's string that was set... that is now split into a string[] from the '/' we put in. look for the keywords
        //and parse the int that's in the string and add the correct amount of time.
        for(String str : strArr) {
            if(str.contains("mo")) {
                String temp = str.replace("mo", "");
                time = time.plusDays(Integer.parseInt(temp) + 30);
            }
            else if(str.contains("d")) {
                String temp = str.replace("d", "");
                time = time.plusDays(Integer.parseInt(temp));
            }
            else if(str.contains("h")) {
                String temp = str.replace("h", "");
                time = time.plusHours(Integer.parseInt(temp));
            }
            else if(str.contains("m")) {
                String temp = str.replace("m", "");
                time = time.plusMinutes(Integer.parseInt(temp));
            }
            else if(str.contains("s")) {
                String temp = str.replace("s", "");
                time = time.plusSeconds(Integer.parseInt(temp));
            } else{
                return null;
            }
        }
        return time;
    }

    public Boolean isPunishmentExpired(String uuid) {
        LocalDateTime now = LocalDateTime.now();
        Document find = new Document("uuid", uuid.toString());
        FindIterable<Document> finds = plugin.getCollection().find(find);

        if(finds.first() == null) {
            return true;
        }

        for(Document doc : finds) {
            List<Document> list = doc.getList("punishments", Document.class);
            int index = list.size() - 1;
            Date date = null;

            if(list.get(index).get("expiration") instanceof Date){
                date = list.get(index).getDate("expiration");
                if(list.get(index).getBoolean("expired")) {
                    return true;
                }
            }
            else if(list.get(index).get("expiration") instanceof String) {
                if(list.get(index).getString("expiration").equalsIgnoreCase("Permanent")) {
                    return list.get(index).getBoolean("expired");
                }
            }
            LocalDateTime convert = Objects.requireNonNull(date).toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime();
            if(now.isAfter(convert)) {
                Bson filter = Filters.and(Filters.eq("uuid", uuid), Filters.eq("punishments.expired", false));
                Bson update = Updates.set("punishments.$.expired", true);
                UpdateResult result = plugin.getCollection().updateOne(filter, update);
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    public UpdateResult unPunish(String uuid) {
        Document find = new Document("uuid", uuid);

        FindIterable<Document> finds = plugin.getCollection().find(find);

        if(finds.first() == null) {
            return null;
        }

        for(Document doc : finds) {
            List<Document> list = doc.getList("punishments", Document.class);
            int index = list.size() - 1;
            Bson filter = Filters.and(Filters.eq("uuid", uuid), Filters.eq("punishments.expired", false));
            Bson update = Updates.set("punishments."+index+".expired", true);
            return plugin.getCollection().updateOne(filter, update);
        }
        return null;
    }

    public String getPunishmentType(String uuid) {
        Document find = new Document("uuid", uuid);
        FindIterable<Document> finds = plugin.getCollection().find(find);

        if(finds.first() == null) {
            return null;
        }

        for(Document doc : finds) {
            List<Document> list = doc.getList("punishments", Document.class);
            int index = list.size() - 1;

            return list.get(index).getString("type");
        }
        return "";
    }

    public String isReason(String[] args, boolean perma) {
        int index = 1;
        String reason = "";

        if(!perma) {
            //if this is a temp ban, the start of the "reason" would be at index 2
            if(args.length >=3) {
                index = 2;
            } else{
                return reason;
            }
        }
        if(args[index] == null) {
            return reason;
        }

        for(int i = index ; i < args.length; i++) {
            reason = reason.concat(args[i] + " ");
        }

        return reason;
    }

    public String addChar(String str, char ch, int position) {
        StringBuilder sb = new StringBuilder(str);
        sb.insert(position, ch);
        return sb.toString();
    }

    public boolean isNumeric(Character c) {
        try {
            int i = Integer.parseInt(c.toString());
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public boolean isDuration(String str) {
        return isNumeric(str.charAt(0));
    }
}
