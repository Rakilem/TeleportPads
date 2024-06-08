package com.rakilem.teleportpads;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Location;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TeleportPadManager {

    private PluginBase plugin;
    private MessageManager messageManager;
    private Map<String, Location> teleportPads = new HashMap<>();
    private Config config;

    public TeleportPadManager(PluginBase plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        File file = new File(plugin.getDataFolder(), "teleportPads.yml");
        config = new Config(file, Config.YAML);
        loadTeleportPads();
    }

    public boolean isTeleportPad(Block block) {
        return teleportPads.containsKey(getPadKey(block.getLocation()));
    }

    public void createTeleportPad(Player player, String[] args) {
        try {
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            int z = Integer.parseInt(args[2]);
            Location targetLocation = new Location(x, y, z, player.getLevel());
            Location padLocation = player.getLocation();
            teleportPads.put(getPadKey(padLocation), targetLocation);
            player.sendMessage(messageManager.getMessage("teleport_created"));
        } catch (NumberFormatException e) {
            player.sendMessage(messageManager.getMessage("teleport_failed"));
        }
    }

    public boolean teleportPlayer(Player player, String padKey) {
        if (teleportPads.containsKey(padKey)) {
            Location targetLocation = teleportPads.get(padKey);
            player.teleport(targetLocation);
            return true;
        }
        return false;
    }

    public void removeTeleportPad(Player player) {
        Location padLocation = player.getLocation();
        String padKey = getPadKey(padLocation);
        if (teleportPads.remove(padKey) != null) {
            config.remove(padKey);
            config.save();
            player.sendMessage(messageManager.getMessage("teleport_pad_removed"));
        } else {
            player.sendMessage(messageManager.getMessage("not_teleport_pad"));
        }
    }

    public void removeTeleportPad(Block block) {
        String padKey = getPadKey(block.getLocation());
        if (teleportPads.remove(padKey) != null) {
            config.remove(padKey);
            config.save();
        }
    }

    public void saveTeleportPads() {
        for (Map.Entry<String, Location> entry : teleportPads.entrySet()) {
            Location loc = entry.getValue();
            String value = loc.getLevel().getName() + "," + loc.getFloorX() + "," + loc.getFloorY() + "," + loc.getFloorZ();
            config.set(entry.getKey(), value);
        }
        config.save();
    }

    private void loadTeleportPads() {
        for (String key : config.getKeys(false)) {
            String[] parts = config.getString(key).split(",");
            if (parts.length == 4) {
                plugin.getServer().loadLevel(parts[0]);
                teleportPads.put(key, new Location(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), plugin.getServer().getLevelByName(parts[0])));
            }
        }
    }

    public String getPadKey(Location location) {
        return location.getLevel().getName() + ":" + location.getFloorX() + "," + location.getFloorY() + "," + location.getFloorZ();
    }
}
