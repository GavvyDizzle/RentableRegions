package com.github.gavvydizzle.rentableregions.commands.player;

import com.github.gavvydizzle.rentableregions.commands.PlayerCommandManager;
import com.github.gavvydizzle.rentableregions.gui.InventoryManager;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class OpenMenuCommand extends SubCommand {

    private final ShopManager shopManager;
    private final InventoryManager inventoryManager;

    public OpenMenuCommand(PlayerCommandManager playerCommandManager, ShopManager shopManager, InventoryManager inventoryManager) {
        this.shopManager = shopManager;
        this.inventoryManager = inventoryManager;

        setName("menu");
        setDescription("Open a shop's menu");
        setSyntax("/" + playerCommandManager.getCommandDisplayName() + " menu [id]");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(playerCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        Shop shop;
        if (args.length == 1) {
            shop = shopManager.getShopByOwnerOrMember(player);
            if (shop == null) {
                sender.sendMessage(Messages.selfDoesNotBelongToShop);
                return;
            }
        }
        else {
            shop = shopManager.getShopByID(args[1].toLowerCase());
            if (shop == null) {
                sender.sendMessage(Messages.invalidShopID.replace("{id}", args[1].toLowerCase()));
                return;
            }
        }

        inventoryManager.openMenu(player, shop.getShopMenu());
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> arr = new ArrayList<>();

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], shopManager.getShopIDs(), arr);
        }

        return arr;
    }
}