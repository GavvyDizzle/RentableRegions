package com.github.gavvydizzle.rentableregions.commands.admin;

import com.github.gavvydizzle.rentableregions.commands.AdminCommandManager;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.mittenmc.serverutils.SubCommand;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class CreateCommand extends SubCommand {

    private final ShopManager shopManager;

    public CreateCommand(AdminCommandManager adminCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("create");
        setDescription("Create a new shop");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " create <id> [region]");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        if (shopManager.isNotInValidWorld(sender)) {
            sender.sendMessage(ChatColor.RED + "You are not in a valid shops world");
            return;
        }

        String newID = args[1];
        ProtectedRegion region;

        if (shopManager.getShopByID(newID) != null) {
            sender.sendMessage(ChatColor.RED + "The shop id '" + newID + "' is already in use");
            return;
        }

        Shop shop = new Shop(newID);
        shopManager.registerShop(shop);
        sender.sendMessage(ChatColor.GREEN + "Successfully created shop " + shop.getId());

        if (args.length > 2) {
            RegionManager regionManager = shopManager.getRegionManager();

            if (regionManager.hasRegion(args[2])) {
                region = regionManager.getRegion(args[2]);
            }
            else {
                sender.sendMessage(ChatColor.RED + "The region '" + args[2] + "' does not exist");
                return;
            }

            if (region instanceof GlobalProtectedRegion) {
                sender.sendMessage(ChatColor.RED + "Global regions are not allowed to be used in shops");
            }

            if (shopManager.isRegionTaken(region)) {
                sender.sendMessage(ChatColor.RED + "The region '" + args[2] + "' is already in use by another shop");
                return;
            }

            // Should not happen
            if (region == null) {
                sender.sendMessage(ChatColor.RED + "The region is null");
                return;
            }

            shopManager.addShopRegion(shop, region);
            sender.sendMessage(ChatColor.GREEN + "Successfully added region " + region.getId() + " to this shop");
        }
        else {
            sender.sendMessage(ChatColor.YELLOW + "Since no region was specified, you will need to add one later");
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 3) {
            if (sender instanceof Player) {
                StringUtil.copyPartialMatches(args[2], shopManager.getPlayerRegions((Player) sender), list);
            }
        }

        return list;
    }
}