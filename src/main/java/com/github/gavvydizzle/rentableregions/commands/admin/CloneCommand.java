package com.github.gavvydizzle.rentableregions.commands.admin;

import com.github.gavvydizzle.rentableregions.commands.AdminCommandManager;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.gavvydizzle.rentableregions.utils.Messages;
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

public class CloneCommand extends SubCommand {

    private final ShopManager shopManager;

    public CloneCommand(AdminCommandManager adminCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("clone");
        setDescription("Create a new shop and copy properties from another");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " clone <cloneID> <id> <region>");
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

        String oldID = args[1];
        Shop clone;
        String newID = args[2];
        ProtectedRegion region;

        clone = shopManager.getShopByID(oldID);
        if (clone == null) {
            sender.sendMessage(Messages.invalidShopID.replace("{id}", oldID.toLowerCase()));
            return;
        }

        if (shopManager.getShopByID(newID) != null) {
            sender.sendMessage(ChatColor.RED + "The shop id '" + newID + "' is already in use");
            return;
        }

        RegionManager regionManager = shopManager.getRegionManager();

        if (regionManager.hasRegion(args[3])) {
            region = regionManager.getRegion(args[3]);
        }
        else {
            sender.sendMessage(ChatColor.RED + "The region '" + args[3] + "' does not exist");
            return;
        }

        if (region instanceof GlobalProtectedRegion) {
            sender.sendMessage(ChatColor.RED + "Global regions are not allowed to be used in shops");
            return;
        }

        if (shopManager.isRegionTaken(region)) {
            sender.sendMessage(ChatColor.RED + "The region '" + args[3] + "' is already in use by another shop");
            return;
        }

        // Should not happen
        if (region == null) {
            sender.sendMessage(ChatColor.RED + "The region is null");
            return;
        }

        Shop shop = new Shop(newID, region, clone);
        shopManager.registerShop(shop);
        sender.sendMessage(ChatColor.GREEN + "Created shop " + newID + " with region " + region.getId());
        sender.sendMessage(ChatColor.GREEN + "Copied the settings from " + oldID + " successfully");
        if (clone.numRegions() == 0) sender.sendMessage(ChatColor.YELLOW + "The cloned shop had no regions so region properties could not be copied");
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], shopManager.getCurrentShopOrAllShops(sender), list);
        }
        else if (args.length == 4) {
            if (sender instanceof Player) {
                StringUtil.copyPartialMatches(args[3], shopManager.getPlayerRegions((Player) sender), list);
            }
        }

        return list;
    }
}