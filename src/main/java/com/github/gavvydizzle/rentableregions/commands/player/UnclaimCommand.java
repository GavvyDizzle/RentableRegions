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

public class UnclaimCommand extends SubCommand {

    private final ShopManager shopManager;

    public UnclaimCommand(PlayerCommandManager playerCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("unclaim");
        setDescription("Unclaim your shop");
        setSyntax("/" + playerCommandManager.getCommandDisplayName() + " unclaim");
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

        if (!shop.isOwner(player.getUniqueId())) {
            sender.sendMessage(Messages.isNotOwnerOfShop);
            return;
        }

        shop.unclaim(true);
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}