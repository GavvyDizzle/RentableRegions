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

public class KickCommand extends SubCommand {

    private final ShopManager shopManager;

    public KickCommand(PlayerCommandManager playerCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("kick");
        setDescription("Kick a member from your shop");
        setSyntax("/" + playerCommandManager.getCommandDisplayName() + " kick <player>");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(playerCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        if (args.length < 2) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        Shop shop = shopManager.getShopByOwner(player);
        if (shop == null) {
            sender.sendMessage(Messages.isNotOwnerOfShop);
            return;
        }

        OfflinePlayer kicked = Bukkit.getPlayer(args[1]);
        if (kicked == null) {
            kicked = Bukkit.getOfflinePlayer(args[1]);
            if (!kicked.hasPlayedBefore() && !kicked.isOnline()) {
                sender.sendMessage(Messages.noPlayerFound.replace("{name}", args[1]));
                return;
            }
        }

        if (shop.isOwner(kicked.getUniqueId())) {
            sender.sendMessage(Messages.ownerKickSelf);
            return;
        }

        if (!shop.isMember(kicked.getUniqueId())) {
            sender.sendMessage(Messages.doesNotBelongToSelfShop.replace("{name}", PlayerNameCache.get(kicked)));
            return;
        }

        shop.removeMember(kicked.getUniqueId(), true);
        sender.sendMessage(Messages.successfulKick.replace("{name}", PlayerNameCache.get(kicked)));
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