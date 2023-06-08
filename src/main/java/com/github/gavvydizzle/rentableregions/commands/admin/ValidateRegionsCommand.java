package com.github.gavvydizzle.rentableregions.commands.admin;

import com.github.gavvydizzle.rentableregions.commands.AdminCommandManager;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValidateRegionsCommand extends SubCommand {

    private final ShopManager shopManager;
    private final ArrayList<String> args2 = new ArrayList<>(Collections.singletonList("true"));

    public ValidateRegionsCommand(AdminCommandManager adminCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("validateRegions");
        setDescription("Checks all shop regions for owner and member mismatches");
        setSyntax("/" + adminCommandManager.getCommandDisplayName() + " validateRegions [update]");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(adminCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        shopManager.validateShopRegions(sender, args.length >= 2 && args[1].equalsIgnoreCase("true"));
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> list = new ArrayList<>();
        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], args2, list);
        }
        return list;
    }
}