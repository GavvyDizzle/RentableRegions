package com.github.gavvydizzle.rentableregions.commands.admin;

import com.github.gavvydizzle.rentableregions.commands.AdminCommandManager;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class CopyPropertiesCommand extends SubCommand {

    private final ShopManager shopManager;

    public CopyPropertiesCommand(AdminCommandManager adminCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("copyProperties");
        setDescription("Copies a shop's properties to another shop");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " copyProperties <fromID> <toID>");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        String oldID = args[1];
        Shop clone;
        String newID = args[2];
        Shop newShop;

        clone = shopManager.getShopByID(oldID);
        if (clone == null) {
            sender.sendMessage(Messages.invalidShopID.replace("{id}", oldID.toLowerCase()));
            return;
        }

        newShop = shopManager.getShopByID(newID);
        if (newShop == null) {
            sender.sendMessage(Messages.invalidShopID.replace("{id}", newID.toLowerCase()));
            return;
        }

        newShop.copyProperties(clone);
        sender.sendMessage(ChatColor.GREEN + "Successfully copied properties from " + ChatColor.YELLOW + clone.getId() + ChatColor.GREEN + " to " + ChatColor.YELLOW + newShop.getId());
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], shopManager.getCurrentShopOrAllShops(sender), list);
        }
        else if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], shopManager.getCurrentShopOrAllShops(sender), list);
        }

        return list;
    }
}