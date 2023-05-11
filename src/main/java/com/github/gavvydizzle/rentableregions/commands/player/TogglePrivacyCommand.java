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

public class TogglePrivacyCommand extends SubCommand {

    private final ShopManager shopManager;

    public TogglePrivacyCommand(PlayerCommandManager playerCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("privacy");
        setDescription("Toggle your shop's privacy");
        setSyntax("/" + playerCommandManager.getCommandDisplayName() + " privacy");
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

        if (shop.togglePrivacy()) {
            sender.sendMessage(Messages.selfShopPrivate);
            int val = shop.removeVisitors();

            // Send no message if 0 players were removed
            if (val > 0) {
                sender.sendMessage(Messages.visitorsRemoved);
            }
            else if (val == -1) {
                sender.sendMessage(Messages.errorRemovingVisitors);
            }
        }
        else {
            sender.sendMessage(Messages.selfShopPublic);
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}