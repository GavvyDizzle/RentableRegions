package com.github.gavvydizzle.rentableregions.commands.player;

import com.github.gavvydizzle.rentableregions.commands.PlayerCommandManager;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class TradeCommand extends SubCommand {

    private final ShopManager shopManager;
    private final ArrayList<String> args2;

    public TradeCommand(PlayerCommandManager playerCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("trade");
        setDescription("Create a trade request");
        setSyntax("/" + playerCommandManager.getCommandDisplayName() + " trade <owner> <arg> <money>");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(playerCommandManager.getPermissionPrefix() + getName().toLowerCase());

        args2 = new ArrayList<>(4);
        args2.add("buy");
        args2.add("sell");
        args2.add("swap_give");
        args2.add("swap_request");
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        // Catch trade accept commands and pass them on
        if (args.length == 1 && args[0].equalsIgnoreCase("accept")) {
            if (shopManager.getTradeManager().hasPendingConfirmation(player)) {
                shopManager.getTradeManager().acceptTrade(player);
            }
            else {
                sender.sendMessage(ChatColor.RED + "You have no pending trade to accept");
            }
            return;
        }

        if (args.length < 4) {
            player.sendMessage(getColoredSyntax());
            return;
        }

        // If the sender has an outstanding trade
        if (shopManager.getTradeManager().hasActiveTrade(player)) {
            sender.sendMessage(Messages.selfHasOutstandingTrade);
            return;
        }

        // Check for valid recipient
        Player recipient = Bukkit.getPlayer(args[1]);
        if (recipient == null) {
            sender.sendMessage(Messages.noOnlinePlayerFound.replace("{name}", args[1]));
            return;
        }

        // If the recipient has an outstanding trade
        if (shopManager.getTradeManager().hasActiveTrade(recipient)) {
            sender.sendMessage(Messages.hasOutstandingTrade.replace("{name}", recipient.getName()));
            return;
        }

        // Check for valid money amount
        double money;
        try {
            money = Numbers.round(Double.parseDouble(args[3]), 2);
            if (money < 0) {
                sender.sendMessage(Messages.moneyMustBePositive);
                return;
            }
        }
        catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid money value '" + args[3] + "'");
            return;
        }

        // Determine trade type and pass it on
        if (args[2].equalsIgnoreCase("buy")) {
            Shop recipientShop = shopManager.getShopByOwnerOrMember(recipient);
            if (recipientShop == null) {
                sender.sendMessage(Messages.recipientDoesNotOwnShop.replace("{name}", args[1]));
                return;
            }

            shopManager.getTradeManager().createBuyTrade(player, recipient, recipientShop, money);
        }
        else if (args[2].equalsIgnoreCase("sell")) {
            Shop creatorShop = shopManager.getShopByOwnerOrMember(player);
            if (creatorShop == null) {
                sender.sendMessage(Messages.selfDoesNotBelongToShop);
                return;
            }

            shopManager.getTradeManager().createSellTrade(player, recipient, creatorShop, money);
        }
        else if (args[2].equalsIgnoreCase("swap_give") || args[2].equalsIgnoreCase("swap_request")) {
                        Shop creatorShop = shopManager.getShopByOwnerOrMember(player);
            if (creatorShop == null) {
                sender.sendMessage(Messages.selfDoesNotBelongToShop);
                return;
            }

            Shop recipientShop = shopManager.getShopByOwnerOrMember(recipient);
            if (recipientShop == null) {
                sender.sendMessage(Messages.recipientDoesNotOwnShop.replace("{name}", args[1]));
                return;
            }

            boolean creatorGiveMoney = args[2].equalsIgnoreCase("swap_give");
            shopManager.getTradeManager().createSwapTrade(player, recipient, creatorShop, recipientShop, money, creatorGiveMoney);
        }
        else {
            sender.sendMessage(ChatColor.RED + "Invalid argument: " + args[2]);
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> arr = new ArrayList<>();

        if (args.length == 2) {
            return null;
        }
        else if (args.length == 3) {
            StringUtil.copyPartialMatches(args[1], args2, arr);
        }

        return arr;
    }
}