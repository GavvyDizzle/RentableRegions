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
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class PromoteCommand extends SubCommand {

    private final ShopManager shopManager;

    public PromoteCommand(PlayerCommandManager playerCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("promote");
        setDescription("Promote a member from your shop to owner");
        setSyntax("/" + playerCommandManager.getCommandDisplayName() + " promote <player>");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(playerCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        Shop shop = shopManager.getShopByOwner(player);
        if (shop == null) {
            sender.sendMessage(Messages.isNotOwnerOfShop);
            return;
        }

        OfflinePlayer promoted = Bukkit.getPlayer(args[1]);
        if (promoted == null) {
            promoted = Bukkit.getOfflinePlayer(args[1]);
            if (!promoted.hasPlayedBefore() && !promoted.isOnline()) {
                sender.sendMessage(Messages.noPlayerFound.replace("{name}", args[1]));
                return;
            }
        }

        if (shop.isOwner(promoted.getUniqueId())) {
            sender.sendMessage(Messages.ownerPromoteSelf);
            return;
        }

        if (!shop.isMember(promoted.getUniqueId())) {
            sender.sendMessage(Messages.doesNotBelongToSelfShop.replace("{name}", PlayerNameCache.get(promoted)));
            return;
        }

        shop.swapOwnership(promoted);
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            if (sender instanceof Player) {
                Shop shop = shopManager.getShopByOwner((OfflinePlayer) sender);
                if (shop != null) {
                    StringUtil.copyPartialMatches(args[1], shop.getMemberNames(), list);
                }
            }
        }

        return list;
    }
}