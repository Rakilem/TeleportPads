package com.rakilem.teleportpads;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

import java.io.File;

public class MessageManager {

    private Config messages;

    public MessageManager(PluginBase plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml");
        }
        messages = new Config(file, Config.YAML);
    }

    public String getMessage
            (String key) {
        return messages.getString(key, "Message not found for key: " + key);
    }
}
