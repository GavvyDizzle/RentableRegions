package com.github.gavvydizzle.rentableregions.commands.admin;

import com.github.gavvydizzle.rentableregions.commands.AdminCommandManager;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EditVisitLocationCommand extends SubCommand {

    private final ShopManager shopManager;
    private final ArrayList<String> args2 = new ArrayList<>(Arrays.asList("exact", "snap"));

    public EditVisitLocationCommand(AdminCommandManager adminCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("visitLocation");
        setDescription("Edit a shop's visit location");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " visitLocation <id> <exact|snap> [x] [y] [z] [pitch] [yaw]");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;

        if (args.length < 3) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        if (shopManager.isNotInValidWorld(sender)) {
            sender.sendMessage(ChatColor.RED + "You are not in a valid shops world");
            return;
        }

        Player player = (Player) sender;

        if (!shopManager.isInWorld(player)) {
            sender.sendMessage(ChatColor.RED + "You must be in the shops world to change the location");
            return;
        }

        Shop shop = shopManager.getShopByID(args[1]);
        if (shop == null) {
            sender.sendMessage(Messages.invalidShopID.replace("{id}", args[1].toLowerCase()));
            return;
        }

        boolean exact;
        if (args[2].equalsIgnoreCase("exact")) {
            exact = true;
        }
        else if (args[2].equalsIgnoreCase("snap")) {
            exact = false;
        }
        else {
            sender.sendMessage(ChatColor.RED + "Invalid argument: " + args[2]);
            return;
        }

        Location location;
        if (args.length == 3) {
            location = player.getLocation();
            if (!exact) snapLocation(location, true);
        }
        else if (args.length == 6) {
            location = player.getLocation();
            try {
                double x = Double.parseDouble(args[3]);
                double y = Double.parseDouble(args[4]);
                double z = Double.parseDouble(args[5]);
                location.setX(x);
                location.setY(y);
                location.setZ(z);
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Invalid x/y/z arguments");
                return;
            }

            snapLocation(location, !exact);
        }
        else if (args.length == 8) {
            location = player.getLocation();
            try {
                double x = Double.parseDouble(args[3]);
                double y = Double.parseDouble(args[4]);
                double z = Double.parseDouble(args[5]);
                float yaw = Float.parseFloat(args[6]);
                float pitch = Float.parseFloat(args[7]);
                location.setX(x);
                location.setY(y);
                location.setZ(z);
                location.setYaw(yaw);
                location.setPitch(pitch);
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Invalid x/y/z/yaw/pitch arguments");
                return;
            }

            if (!exact) {
                snapLocation(location, true);
            }
        }
        else {
            sender.sendMessage(ChatColor.RED + "Invalid location configuration");
            sender.sendMessage(ChatColor.RED + "Provide nothing, x/y/z, or all 5 arguments");
            return;
        }

        shop.setVisitLocation(location);
        sender.sendMessage(ChatColor.GREEN + "Set shop " + shop.getId() + " visit location to: (" +
                location.getX() + ", " + location.getY() + ", " + location.getZ() + ", " + location.getYaw() + ", " + location.getPitch() + ")");
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
        else if (sender instanceof Player) {
            if (args.length == 4) {
                StringUtil.copyPartialMatches(args[3], Collections.singletonList(String.valueOf(((Player) sender).getLocation().getX())), list);
            }
            else if (args.length == 5) {
                StringUtil.copyPartialMatches(args[4], Collections.singletonList(String.valueOf(((Player) sender).getLocation().getY())), list);
            }
            else if (args.length == 6) {
                StringUtil.copyPartialMatches(args[5], Collections.singletonList(String.valueOf(((Player) sender).getLocation().getZ())), list);
            }
            else if (args.length == 7) {
                StringUtil.copyPartialMatches(args[6], Collections.singletonList(String.valueOf(((Player) sender).getLocation().getYaw())), list);
            }
            else if (args.length == 8) {
                StringUtil.copyPartialMatches(args[7], Collections.singletonList(String.valueOf(((Player) sender).getLocation().getPitch())), list);
            }
        }

        return list;
    }

    /**
     * Edits a location's x/z and/or pitch/yaw directly.
     * Calling this will always set the pitch to 0 and snap the yaw to the closest 90 degree value
     *
     * @param location The location
     * @param snapXYZ To make the x and z in the center of the block
     */
    private void snapLocation(Location location, boolean snapXYZ) {
        if (snapXYZ) {
            location.setX(location.getBlockX() + 0.5);
            location.setZ(location.getBlockZ() + 0.5);
        }

        location.setPitch(0);
        float yaw = location.getYaw();

        if (yaw < -135) yaw = 180;
        else if (yaw < -45) yaw = -90;
        else if (yaw < 45) yaw = 0;
        else if (yaw < 135) yaw = 90;
        else yaw = 180;

        location.setYaw(yaw);
    }
}