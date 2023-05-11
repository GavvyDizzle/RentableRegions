package com.github.gavvydizzle.rentableregions.commands.admin;

import com.github.gavvydizzle.rentableregions.commands.AdminCommandManager;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ToggleTimerCommand extends SubCommand {

    private final ShopManager shopManager;

    public ToggleTimerCommand(AdminCommandManager adminCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("toggleTimer");
        setDescription("Toggles the shop timer on/off");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " toggleTimer");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (shopManager.toggleTimer()) {
            sender.sendMessage(ChatColor.GREEN + "The shop timer is now enabled");
        }
        else {
            sender.sendMessage(ChatColor.YELLOW + "The shop timer is now disabled. To enable it, run this command again.");
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}