package com.github.gavvydizzle.rentableregions.commands.admin;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.commands.AdminCommandManager;
import com.github.gavvydizzle.rentableregions.configs.*;
import com.github.gavvydizzle.rentableregions.gui.InventoryManager;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.gavvydizzle.rentableregions.utils.Sounds;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand extends SubCommand {

    private final AdminCommandManager adminCommandManager;
    private final ShopManager shopManager;
    private final InventoryManager inventoryManager;
    private final ArrayList<String> argsList;
    private final String pluginName;

    public ReloadCommand(AdminCommandManager adminCommandManager, ShopManager shopManager, InventoryManager inventoryManager) {
        this.adminCommandManager = adminCommandManager;
        this.shopManager = shopManager;
        this.inventoryManager = inventoryManager;
        pluginName = RentableRegions.getInstance().getName();

        argsList = new ArrayList<>();
        argsList.add("commands");
        argsList.add("gui");
        argsList.add("messages");
        argsList.add("shops");
        argsList.add("sounds");

        setName("reload");
        setDescription("Reloads this plugin or a specified portion");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " reload [arg]");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            switch (args[1].toLowerCase()) {
                case "commands":
                    try {
                        reloadCommands();
                        sender.sendMessage(ChatColor.GREEN + "[" + pluginName + "] " + "Successfully reloaded commands");
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendErrorMessage(sender);
                    }
                    break;
                case "gui":
                    try {
                        reloadGUI();
                        sender.sendMessage(ChatColor.GREEN + "[" + pluginName + "] " + "Successfully reloaded shop GUIs");
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendErrorMessage(sender);
                    }
                    break;
                case "messages":
                    try {
                        reloadMessages();
                        sender.sendMessage(ChatColor.GREEN + "[" + pluginName + "] " + "Successfully reloaded all messages");
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendErrorMessage(sender);
                    }
                    break;
                case "shops":
                    try {
                        reloadShops();
                        sender.sendMessage(ChatColor.GREEN + "[" + pluginName + "] " + "Successfully reloaded all shops");
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendErrorMessage(sender);
                    }
                    break;
                case "sounds":
                    try {
                        reloadSounds();
                        sender.sendMessage(ChatColor.GREEN + "[" + pluginName + "] " + "Successfully reloaded all sounds");
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendErrorMessage(sender);
                    }
                    break;
            }
        }
        else {
            try {
                reloadCommands();
                reloadGUI();
                reloadMessages();
                reloadShops();
                reloadSounds();
                sender.sendMessage(ChatColor.GREEN + "[" + pluginName + "] " + "Successfully reloaded");
            } catch (Exception e) {
                e.printStackTrace();
                sendErrorMessage(sender);
            }
        }
    }

    private void sendErrorMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Encountered an error when reloading. Check the console");
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], argsList, list);
        }

        return list;
    }

    private void reloadCommands() {
        CommandsConfig.reload();
        adminCommandManager.reload();
        RentableRegions.getInstance().getPlayerCommandManager().reload();
    }

    private void reloadGUI() {
        MenuConfig.reload();
        inventoryManager.reload(true);
    }

    private void reloadMessages() {
        MessagesConfig.reload();
        Messages.reloadMessages();
    }

    private void reloadShops() {
        RentableRegions.getInstance().reloadConfig();
        if (shopManager.getRegionManager() != null) { // Reloading the file with a null RegionManager causes NPE error
            ShopsConfig.reload();
        }
        shopManager.reload();
    }

    private void reloadSounds() {
        SoundsConfig.reload();
        Sounds.reload();
    }

}