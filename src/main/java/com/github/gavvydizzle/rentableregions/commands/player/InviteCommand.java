package com.github.gavvydizzle.rentableregions.commands.player;

import com.github.gavvydizzle.rentableregions.commands.PlayerCommandManager;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class InviteCommand extends SubCommand {

    private final ShopManager shopManager;

    public InviteCommand(PlayerCommandManager playerCommandManager, ShopManager shopManager) {
        this.shopManager = shopManager;

        setName("invite");
        setDescription("Invite a player to your shop");
        setSyntax("/" + playerCommandManager.getCommandDisplayName() + " invite <player>");
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

        Shop shop = shopManager.getShopByOwnerOrMember(player);
        if (shop == null) {
            sender.sendMessage(Messages.selfDoesNotBelongToShop);
            return;
        }

        Player invited = Bukkit.getPlayer(args[1]);
        if (invited == null) {
            sender.sendMessage(Messages.noOnlinePlayerFound.replace("{name}", args[1]));
            return;
        }

        if (player.getUniqueId().equals(invited.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "You can't invite yourself... silly goose");
            return;
        }

        if (shopManager.belongToShop(invited)) {
            sender.sendMessage(Messages.playerBelongsToShop.replace("{name}", args[1]));
            return;
        }

        // Allow the creator to make many messages, but only one to each player
        // Don't allow command to go through if the invited player has an existing invite
        if (shopManager.getInviteManager().doesPlayerHaveOutstandingInvite(invited)) {
            sender.sendMessage(Messages.otherOutstandingInvite);
            return;
        }

        if (shop.doesNotMeetJoinRequirements(invited)) {
            sender.sendMessage(Messages.tooLowLevelToInvite.replace("{name}", invited.getName()));
            return;
        }

        shopManager.getInviteManager().sendInvite(player, invited, shop);
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return null;
        }
        return new ArrayList<>();
    }
}