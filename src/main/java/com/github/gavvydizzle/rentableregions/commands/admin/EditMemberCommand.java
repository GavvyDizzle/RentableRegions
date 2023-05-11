package com.github.gavvydizzle.rentableregions.commands.admin;

import com.github.gavvydizzle.rentableregions.commands.AdminCommandManager;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.mittenmc.serverutils.PlayerNameCache;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class EditMemberCommand extends SubCommand {

    private final ShopManager shopManager;
    private final ArrayList<String> args2 = new ArrayList<>(Arrays.asList("add", "clear", "remove"));

    public EditMemberCommand(AdminCommandManager adminCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("member");
        setDescription("Edit a shop's members");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " member <id> <add|clear|remove> [player]");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        Shop shop = shopManager.getShopByID(args[1]);
        if (shop == null) {
            sender.sendMessage(Messages.invalidShopID.replace("{id}", args[1].toLowerCase()));
            return;
        }

        if (!shop.isOccupied()) {
            sender.sendMessage(ChatColor.RED + "You can't edit the members of a shop that has no owner");
            return;
        }

        if (args[2].equalsIgnoreCase("clear")) {
            Collection<String> names = shop.getMemberNames();
            if (names.isEmpty()) {
                sender.sendMessage(ChatColor.YELLOW + "This shop already has no members");
                return;
            }

            shop.removeAllMembers();
            sender.sendMessage(ChatColor.GREEN + "Removed all members from the shop");
            sender.sendMessage(ChatColor.YELLOW + "Players: " + names);
        }
        else {
            if (args.length < 4) {
                sender.sendMessage(ChatColor.RED + "Missing player argument");
                return;
            }

            OfflinePlayer member = Bukkit.getPlayer(args[3]);
            if (member == null) {
                member = Bukkit.getOfflinePlayer(args[3]);
                if (!member.hasPlayedBefore() && !member.isOnline()) {
                    sender.sendMessage(Messages.noPlayerFound.replace("{name}", args[3]));
                    return;
                }
            }

            if (shop.isOwner(member.getUniqueId())) {
                sender.sendMessage(ChatColor.RED + PlayerNameCache.get(member) + " is the owner of this shop");
                return;
            }

            if (args[2].equalsIgnoreCase("add")) {

                if (shop.isMember(member.getUniqueId())) {
                    sender.sendMessage(ChatColor.YELLOW + PlayerNameCache.get(member) + " is already a member of this shop");
                    return;
                }

                boolean wasAtCapacity = shop.isAtCapacity();
                shop.addMember(member.getUniqueId(), true, true);
                sender.sendMessage(ChatColor.GREEN + "Successfully added " + PlayerNameCache.get(member) + " to shop " + shop.getId());

                if (wasAtCapacity && shop.isAtCapacity()) {
                    sender.sendMessage(ChatColor.YELLOW + "Adding this player put the shop over its member cap!");
                }
            }
            else if (args[2].equalsIgnoreCase("remove")) {
                if (!shop.isMember(member.getUniqueId())) {
                    sender.sendMessage(ChatColor.YELLOW + PlayerNameCache.get(member) + " is not a member of this shop");
                    return;
                }

                shop.removeMember(member.getUniqueId(), true);
                sender.sendMessage(ChatColor.GREEN + "Successfully removed " + PlayerNameCache.get(member) + " from shop " + shop.getId());
            }
            else {
                sender.sendMessage(ChatColor.RED + "Invalid argument: " + args[2]);
            }
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
                return null;
            }
            else if (args[2].equalsIgnoreCase("remove")) {
                Shop shop = shopManager.getShopByID(args[1]);
                if (shop == null) {
                    return list;
                }

                StringUtil.copyPartialMatches(args[3], shop.getMemberNames(), list);
            }
        }

        return list;
    }
}