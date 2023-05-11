package com.github.gavvydizzle.rentableregions.commands.admin;

import com.github.gavvydizzle.rentableregions.commands.AdminCommandManager;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class UpdateSignsCommand extends SubCommand {

    private final ShopManager shopManager;

    public UpdateSignsCommand(AdminCommandManager adminCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("updateSigns");
        setDescription("Reloads the text on all shop signs");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " updateSigns");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        int updated = shopManager.updateAllSigns();
        int numSigns = shopManager.getNumActiveSigns();
        if (updated == numSigns) {
            sender.sendMessage(ChatColor.GREEN + "Successfully updated the signs for all " + updated + " shops");
        }
        else {
            sender.sendMessage(ChatColor.YELLOW + "Successfully updated the signs for " + updated + " shops");
            sender.sendMessage(ChatColor.RED + "Failed to update the signs for " + (numSigns - updated) + " shops. Check the console to see which shops");
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}