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

public class DeleteCommand extends SubCommand {

    private final ShopManager shopManager;
    private final ArrayList<String> args2;

    public DeleteCommand(AdminCommandManager adminCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("delete");
        setDescription("Delete a shop");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " delete <id> [deleteRegions]");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());

        args2 = new ArrayList<>();
        args2.add("deleteRegions");
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        Shop shop = shopManager.getShopByID(args[1]);
        if (shop == null) {
            sender.sendMessage(Messages.invalidShopID.replace("{id}", args[1].toLowerCase()));
            return;
        }

        if (args.length > 2 && args[2].equalsIgnoreCase("deleteRegions")) {
            ArrayList<String> arr = shop.getRegionNames();
            shopManager.deleteShop(shop, true);
            sender.sendMessage(ChatColor.YELLOW + "Successfully deleted shop " + shop.getId() + " and all of its regions");
            sender.sendMessage(ChatColor.YELLOW + "Regions: " + arr.toString());
        }
        else {
            shopManager.deleteShop(shop, false);
            sender.sendMessage(ChatColor.YELLOW + "Successfully deleted shop " + shop.getId());
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], shopManager.getCurrentShopOrAllShops(sender), list);
        }
        else if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], args2, list);
        }

        return list;
    }
}