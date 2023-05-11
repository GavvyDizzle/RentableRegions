package com.github.gavvydizzle.rentableregions.commands.admin;

import com.github.gavvydizzle.rentableregions.commands.AdminCommandManager;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Set location to the sign the player is looking at or delete the sign
 */
public class SignCommand extends SubCommand {

    private final ShopManager shopManager;
    private final ArrayList<String> args2;

    public SignCommand(AdminCommandManager adminCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("sign");
        setDescription("Set or delete a shop's linked sign");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " sign <id> <delete|set>");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());

        args2 = new ArrayList<>();
        args2.add("delete");
        args2.add("set");
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

        Location signLocation = shop.getSignLocation();

        if (args[2].equalsIgnoreCase("delete")) {
            if (signLocation == null) {
                sender.sendMessage(ChatColor.YELLOW + "This shop does not have a sign");
                return;
            }

            shopManager.onSignDelete(shop);
            signLocation.getBlock().setType(Material.AIR);
            sender.sendMessage(ChatColor.GREEN + "Successfully deleted the sign for shop " + shop.getId());
        }
        else if (args[2].equalsIgnoreCase("set")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can set a shop sign");
                return;
            }
            Player player = (Player) sender;

            if (signLocation != null) {
                sender.sendMessage(ChatColor.YELLOW + "This shop already has a linked sign. You must delete it to move it");
                return;
            }

            // Raytrace up to 30 blocks to find the sign
            Block block = player.getTargetBlock(null, 30);
            if (!(block.getState() instanceof Sign)) {
                sender.sendMessage(ChatColor.RED + "You must be looking at a sign to set it");
                return;
            }

            Location newLoc = block.getLocation();
            if (shopManager.getShopBySignLocation(newLoc) != null) {
                sender.sendMessage(ChatColor.RED + "This sign is already linked to another shop");
                return;
            }

            shopManager.onSignSet(shop, newLoc);
            sender.sendMessage(ChatColor.GREEN + "Successfully set the sign location for shop " + shop.getId());
        }
        else {
            sender.sendMessage(ChatColor.RED + "Invalid argument: " + args[2]);
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], shopManager.getShopIDs(), list);
        }
        else if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], args2, list);
        }

        return list;
    }
}