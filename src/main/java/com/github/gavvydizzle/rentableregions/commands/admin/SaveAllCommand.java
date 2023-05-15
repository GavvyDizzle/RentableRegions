package com.github.gavvydizzle.rentableregions.commands.admin;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.commands.AdminCommandManager;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class SaveAllCommand extends SubCommand {

    private final ShopManager shopManager;

    public SaveAllCommand(AdminCommandManager adminCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("saveAll");
        setDescription("[TESTING] Force saves all shops");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " saveAll");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            Bukkit.getScheduler().runTaskAsynchronously(RentableRegions.getInstance(), shopManager::saveAllShops);
            sender.sendMessage(ChatColor.GREEN + "Force saved all shops (async)");
        }
        else {
            shopManager.saveAllShops();
            sender.sendMessage(ChatColor.GREEN + "Force saved all shops (sync)");
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}