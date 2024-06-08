package com.rakilem.teleportpads;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.TextFormat;

import java.util.HashMap;
import java.util.Map;

public class TeleportPads extends PluginBase implements Listener {

    private TeleportPadManager padManager;
    private MessageManager messageManager;
    private Map<String, Long> lastTeleportTime = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messageManager = new MessageManager(this);
        padManager = new TeleportPadManager(this, messageManager);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info(TextFormat.GREEN + "TeleportPads plugin enabled!");
    }

    @Override
    public void onDisable() {
        padManager.saveTeleportPads();
        getLogger().info(TextFormat.RED + "TeleportPads plugin disabled!");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == PlayerInteractEvent.Action.PHYSICAL) {
            Block block = event.getBlock();
            if (padManager.isTeleportPad(block)) {
                String padKey = padManager.getPadKey(block.getLocation());
                Player player = event.getPlayer();
                long currentTime = System.currentTimeMillis();
                if (!lastTeleportTime.containsKey(player.getName()) || currentTime - lastTeleportTime.get(player.getName()) >= 1000) {
                    if (padManager.teleportPlayer(player, padKey)) {
                        lastTeleportTime.put(player.getName(), currentTime);
                    }
                }
                event.setCancelled(true); // Cancel event to avoid multiple triggers
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (padManager.isTeleportPad(block)) {
            padManager.removeTeleportPad(block);
            event.getPlayer().sendMessage(TextFormat.GREEN + messageManager.getMessage("teleport_pad_removed"));
            return;
        }
        // Si el bloque no es una almohadilla de teletransporte, no se enviará ningún mensaje.
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("setteleportpad")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length != 3) {
                    player.sendMessage(TextFormat.RED + messageManager.getMessage("usage"));
                    return false;
                }
                padManager.createTeleportPad(player, args);
                return true;
            } else {
                sender.sendMessage(TextFormat.RED + messageManager.getMessage("only_players"));
            }
        }
        return false;
    }
}
