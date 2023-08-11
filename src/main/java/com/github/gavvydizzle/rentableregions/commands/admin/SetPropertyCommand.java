package com.github.gavvydizzle.rentableregions.commands.admin;

import com.github.gavvydizzle.rentableregions.commands.AdminCommandManager;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Set one of the many properties of a shop
 * Have a sub-help command for this one
 * ID (string -> new string)
 * member (add/remove)
 * region (add/remove)
 * Level (int)
 * Spawn Location (location)
 * Time remaining (seconds)
 * max rent time (seconds)
 * rent price (int)
 * time per rent (seconds)
 */
public class SetPropertyCommand extends SubCommand {

    private final ShopManager shopManager;
    private final ArrayList<String> args2;

    public SetPropertyCommand(AdminCommandManager adminCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("set");
        setDescription("Edit miscellaneous shop properties");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " set <id> <field> <value>");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());

        args2 = new ArrayList<>();
        args2.add("id"); // string
        args2.add("level"); // int
        args2.add("timeRemaining"); // int - seconds
        args2.add("maxRentTime"); // int - seconds
        args2.add("rentPrice"); // int - money
        args2.add("timePerRent"); // int seconds
        args2.add("capacity"); // int members
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        Shop shop = shopManager.getShopByID(args[1]);
        if (shop == null) {
            sender.sendMessage(Messages.invalidShopID.replace("{id}", args[1].toLowerCase()));
            return;
        }

        if (args[3].equalsIgnoreCase("id")) {
            String newID = args[3];
            String oldID = shop.getId();

            if (shopManager.changeShopID(shop, newID)) {
                sender.sendMessage(ChatColor.GREEN + "Successfully updated the shop id to: " + shop.getId() + " (was " + oldID + ")");
            }
            else {
                sender.sendMessage(ChatColor.RED + "The shop id '" + newID + "' is already in use");
            }
            return;
        }

        int value;
        try {
            value = Integer.parseInt(args[3]);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Invalid value: " + args[3] + ". It must be an integer");
            return;
        }
        if (value < 0) {
            sender.sendMessage(ChatColor.RED + "The value must be a positive number");
            return;
        }

        int oldValue;
        if (args[2].equalsIgnoreCase("level")) {
            oldValue = shop.getRentManager().getLevelRequired();
            shop.getRentManager().setLevelRequired(value);
            sender.sendMessage(ChatColor.GREEN + "Successfully updated the shop's level required to " + shop.getRentManager().getLevelRequired() +
                    " (was " + oldValue + ")");
            shop.getShopMenu().updateExtraInfoItem();
        }
        else if (args[2].equalsIgnoreCase("timeRemaining")) {
            if (!shop.isOccupied()) {
                sender.sendMessage(ChatColor.RED + "You cannot edit the time of shop with no owner");
                return;
            }

            oldValue = shop.getRentManager().getSecondsRemaining();
            shop.getRentManager().setTimeRemaining(value);
            sender.sendMessage(ChatColor.GREEN + "Successfully updated the shop's time remaining to " + Numbers.getTimeFormatted(shop.getRentManager().getSecondsRemaining()) +
                    " (was " + Numbers.getTimeFormatted(oldValue) + ")");
        }
        else if (args[2].equalsIgnoreCase("maxRentTime")) {
            oldValue = shop.getRentManager().getMaxRentSeconds();
            shop.getRentManager().setMaxRentSeconds(value);
            sender.sendMessage(ChatColor.GREEN + "Successfully updated the shop's max rent time to " + Numbers.getTimeFormatted(shop.getRentManager().getMaxRentSeconds()) +
                    " (was " + Numbers.getTimeFormatted(oldValue) + ")");
        }
        else if (args[2].equalsIgnoreCase("rentPrice")) {
            oldValue = shop.getRentManager().getRentPrice();
            shop.getRentManager().setRentPrice(value);
            sender.sendMessage(ChatColor.GREEN + "Successfully updated the shop's rent price to $" + Numbers.withSuffix(shop.getRentManager().getRentPrice()) +
                    " (was $" + Numbers.withSuffix(oldValue) + " - exact: " + oldValue + ")");
        }
        else if (args[2].equalsIgnoreCase("timePerRent")) {
            oldValue = shop.getRentManager().getSecondsPerRent();
            shop.getRentManager().setSecondsPerRent(value);
            sender.sendMessage(ChatColor.GREEN + "Successfully updated the shop's time per rent to " + Numbers.getTimeFormatted(shop.getRentManager().getSecondsPerRent()) +
                    " (was " + Numbers.getTimeFormatted(oldValue) + ")");
        }
        else if (args[2].equalsIgnoreCase("capacity")) {
            oldValue = shop.getMemberCapacity();
            shop.setMemberCapacity(value);

            if (oldValue != shop.getMemberCapacity()) {
                sender.sendMessage(ChatColor.GREEN + "Successfully updated the shop's member capacity " + shop.getMemberCapacity() +
                        " (was " + oldValue + ")");
                shop.getShopMenu().updateExtraInfoItem();
                if (shop.isOverfilled())  {
                    sender.sendMessage(ChatColor.YELLOW + "This shop has more members than its capacity allows!");
                }
            }
            else {
                sender.sendMessage(ChatColor.GREEN + "Nothing changed! You set the value equal to the old one (" + oldValue + ")");
            }
        }
        else {
            sender.sendMessage(ChatColor.RED + "Invalid field: " + args[2]);
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