package com.github.gavvydizzle.rentableregions.commands.player;

import com.github.gavvydizzle.rentableregions.commands.PlayerCommandManager;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LeaveCommand extends SubCommand {

    private final ShopManager shopManager;

    public LeaveCommand(PlayerCommandManager playerCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("leave");
        setDescription("Leave your shop");
        setSyntax("/" + playerCommandManager.getCommandDisplayName() + " leave");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(playerCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        Shop shop = shopManager.getShopByOwnerOrMember(player);
        if (shop == null) {
            sender.sendMessage(Messages.selfDoesNotBelongToShop);
            return;
        }

        if (shop.isOwner(player.getUniqueId())) {
            sender.sendMessage(Messages.ownerTriedToLeave);
            return;
        }

        shop.removeMember(player.getUniqueId(), true);
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}