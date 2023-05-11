package com.github.gavvydizzle.rentableregions.commands.admin;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.commands.AdminCommandManager;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.PlayerNameCache;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransferOwnershipCommand extends SubCommand {

    private final ShopManager shopManager;

    public TransferOwnershipCommand(AdminCommandManager adminCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("transfer");
        setDescription("Transfer shop ownership and set the rent time remaining");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " transfer <id> <toPlayer> [time]");
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

        OfflinePlayer destination = Bukkit.getPlayer(args[2]);
        if (destination == null) {
            destination = Bukkit.getOfflinePlayer(args[2]);
            if (!destination.hasPlayedBefore() && !destination.isOnline()) {
                sender.sendMessage(Messages.noPlayerFound.replace("{name}", args[2]));
                return;
            }
        }

        int seconds = shop.getRentManager().getSecondsRemaining();
        if (args.length >= 4) {
            try {
                seconds = Integer.parseInt(args[3]);
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Invalid time: " + args[3]);
                return;
            }
        }

        if (RentableRegions.getInstance().getShopManager().belongToShop(destination)) {
            sender.sendMessage(ChatColor.RED + PlayerNameCache.get(destination) + " already belongs to a shop");
            return;
        }

        // If the shop has an owner then do a transfer, otherwise set
        if (shop.isOccupied()) {
            seconds = Math.max(60, seconds); // Minimum time of 1 minute remaining

            // Handles status messages internally
            shop.transferOwnership(sender, destination, seconds);
        }
        else {
            seconds = Math.max(3, seconds); // Minimum time of 3 seconds remaining
            shop.setOwner(destination.getUniqueId());
            shop.getRentManager().setTimeRemaining(seconds);
            shop.getLotteryManager().setIfLotteryActive();

            Messages.sendMessage(destination.getUniqueId(), Messages.givenShopByTransfer.replace("{id}", shop.getId()));
            sender.sendMessage(ChatColor.GREEN + "Successfully gave shop " + shop.getId() + " to " + PlayerNameCache.get(destination) + " with " + Numbers.getTimeFormatted(seconds));
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], shopManager.getCurrentShopOrAllShops(sender), list);
        }
        else if (args.length == 3) {
            return null;
        }
        else if (args.length == 4) {
            return Collections.singletonList("seconds");
        }

        return list;
    }
}