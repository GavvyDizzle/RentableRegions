package com.github.gavvydizzle.rentableregions.commands.admin;

import com.github.gavvydizzle.rentableregions.commands.AdminCommandManager;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.mittenmc.serverutils.SubCommand;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditRegionCommand extends SubCommand {

    private final ShopManager shopManager;
    private final ArrayList<String> args2 = new ArrayList<>(Arrays.asList("add", "remove"));

    public EditRegionCommand(AdminCommandManager adminCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("region");
        setDescription("Edit a shop's regions");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " region <id> <add|remove> <region>");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        if (shopManager.isNotInValidWorld(sender)) {
            sender.sendMessage(ChatColor.RED + "You are not in a valid shops world");
            return;
        }

        Shop shop = shopManager.getShopByID(args[1]);
        if (shop == null) {
            sender.sendMessage(Messages.invalidShopID.replace("{id}", args[1].toLowerCase()));
            return;
        }

        ProtectedRegion region = shopManager.getRegionManager().getRegion(args[3]);
        if (region == null) {
            sender.sendMessage(ChatColor.RED + "The region '" + args[3] + "' does not exist");
            return;
        }

        if (region instanceof GlobalProtectedRegion) {
            sender.sendMessage(ChatColor.RED + "Global regions are not allowed to be used in shops");
            return;
        }


        if (args[2].equalsIgnoreCase("add")) {

            if (shop.isRegion(region)) {
                sender.sendMessage(ChatColor.YELLOW + region.getId() + " is already a region of this shop");
                return;
            }

            if (shopManager.isRegionTaken(region)) {
                sender.sendMessage(ChatColor.RED + "The region '" + region.getId() + "' is already in use by another shop");
                return;
            }

            shopManager.addShopRegion(shop, region);
            sender.sendMessage(ChatColor.GREEN + "Successfully added region '" + region.getId() + "' to shop "  + shop.getId());
        }
        else if (args[2].equalsIgnoreCase("remove")) {
            if (!shop.isRegion(region)) {
                sender.sendMessage(ChatColor.YELLOW + region.getId() + " is not a region of this shop");
                return;
            }

            shopManager.removeShopRegion(shop, region);
            sender.sendMessage(ChatColor.GREEN + "Successfully removed region `" + region.getId() + "` from shop " + shop.getId());
        }
        else {
            sender.sendMessage(ChatColor.RED + "Invalid argument: " + args[2]);
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
        else if (args.length == 4) {
            if (args[2].equalsIgnoreCase("add")) {
                if (sender instanceof Player) {
                    StringUtil.copyPartialMatches(args[3], shopManager.getPlayerRegions((Player) sender), list);
                }
            }
            else if (args[2].equalsIgnoreCase("remove")) {
                Shop shop = shopManager.getShopByID(args[1]);
                if (shop == null) {
                    return list;
                }

                StringUtil.copyPartialMatches(args[3], shop.getRegionNames(), list);
            }
        }

        return list;
    }
}