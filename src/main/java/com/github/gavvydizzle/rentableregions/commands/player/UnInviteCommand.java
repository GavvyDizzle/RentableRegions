package com.github.gavvydizzle.rentableregions.commands.player;

import com.github.gavvydizzle.rentableregions.commands.PlayerCommandManager;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class UnInviteCommand extends SubCommand {

    private final ShopManager shopManager;

    public UnInviteCommand(PlayerCommandManager playerCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("unInvite");
        setDescription("Cancel a pending invite");
        setSyntax("/" + playerCommandManager.getCommandDisplayName() + " unInvite");
        setColoredSyntax(ChatColor.YELLOW + getSyntax());
        setPermission(playerCommandManager.getPermissionPrefix() + getName().toLowerCase());
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        // If the player is not the creator of an invite
        if (!shopManager.getInviteManager().isPlayerCreatorOfInvite(player)) {
            sender.sendMessage(Messages.otherOutstandingInvite);
            return;
        }
        shopManager.getInviteManager().cancelInvite(player);
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
