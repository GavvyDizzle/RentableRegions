package com.github.gavvydizzle.rentableregions.commands.player;

import com.github.gavvydizzle.rentableregions.commands.PlayerCommandManager;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.mittenmc.serverutils.PlayerNameCache;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class VisitCommand extends SubCommand {

    private final ShopManager shopManager;

    public VisitCommand(PlayerCommandManager playerCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("visit");
        setDescription("Visit a player's shop");
        setSyntax("/" + playerCommandManager.getCommandDisplayName() + " visit <player>");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(playerCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;

        if (args.length < 1) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        OfflinePlayer destination = Bukkit.getPlayer(args[1]);
        if (destination == null) {
            destination = Bukkit.getOfflinePlayer(args[1]);
            if (!destination.hasPlayedBefore() && !destination.isOnline()) {
                sender.sendMessage(Messages.noPlayerFound.replace("{name}", args[1]));
                return;
            }
        }

        Shop shop = shopManager.getShopByOwnerOrMember(destination);
        if (shop == null) {
            sender.sendMessage(Messages.doesNotBelongToShop.replace("{name}", PlayerNameCache.get(destination)));
            return;
        }

        shop.teleportPlayer((Player) sender);
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return null;
        }
        return new ArrayList<>();
    }
}