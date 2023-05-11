package com.github.gavvydizzle.rentableregions.shop.invite;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class InviteManager {

    private static final int INVITE_LENGTH_SECONDS = 15;

    private final ShopManager shopManager;
    private final ArrayList<Invite> invites;

    public InviteManager(ShopManager shopManager) {
        this.shopManager = shopManager;
        invites = new ArrayList<>();
    }

    /**
     * Clears all invites by making them all expire.
     * This is to be called when all shops are reloaded
     */
    public void clear() {
        ArrayList<Invite> arr = new ArrayList<>(invites);
        for (Invite invite : arr) {
            inviteExpired(invite);
        }

        // Clear invites just in case
        invites.clear();
    }

    /**
     * Initiates an island invite for the given players.
     * @param creator The creator of the invite
     * @param invitedPlayer The player who the creator invited
     * @param shop The shop
     */
    public void sendInvite(Player creator, Player invitedPlayer, Shop shop) {
        Invite invite = new Invite(creator, invitedPlayer, shop);

        creator.sendMessage(Messages.inviteSent.replace("{name}", invitedPlayer.getName()));

        TextComponent component = new TextComponent(TextComponent.fromLegacyText(Messages.inviteReceived
                .replace("{name}", creator.getName()).replace("{id}", shop.getId())));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + RentableRegions.getInstance().getPlayerCommandManager().getCommandDisplayName() + " join"));
        invitedPlayer.spigot().sendMessage(component);

        int taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(RentableRegions.getInstance(), () -> inviteExpired(invite), INVITE_LENGTH_SECONDS * 20L);
        invite.setTaskID(taskID);

        invites.add(invite);
    }

    /**
     * Handles when a player accepts their invite
     * @param invitedPlayer The player who was invited
     */
    public void acceptInvite(Player invitedPlayer) {
        Invite invite = null;
        for (Invite i : invites) {
            if (i.getInvitedPlayer().getUniqueId().equals(invitedPlayer.getUniqueId())) {
                invite = i;
                break;
            }
        }

        if (invite == null) {
            return;
        }

        if (!invite.getShop().isOwnerOrMember(invite.getCreator().getUniqueId())) {
            invitedPlayer.sendMessage(ChatColor.RED + "The invite became invalid because " + invite.getCreator().getName() + " is no longer a member of shop " + invite.getShop().getId() + "!");
            inviteExpired(invite);
            return;
        }

        if (invite.getShop().isAtCapacity()) {
            invitedPlayer.sendMessage(Messages.maxMembersReached);
            inviteExpired(invite);
            return;
        }

        if (invite.getCreator().isOnline()) {
            invite.getCreator().sendMessage(Messages.sentInviteAccepted);
        }
        invitedPlayer.sendMessage(Messages.receivedInviteAccepted.replace("{id}", invite.getShop().getId()));

        invites.remove(invite);
        Bukkit.getScheduler().cancelTask(invite.getTaskID());

        shopManager.onPlayerJoinShop(invitedPlayer, invite.getShop(), false);
    }

    /**
     * Handles when the creator of an invitation cancels it before it is expired or accepted
     * @param creator The creator of the invite
     */
    public void cancelInvite(Player creator) {
        Invite invite = null;
        for (Invite i : invites) {
            if (i.getCreator().getUniqueId().equals(creator.getUniqueId())) {
                invite = i;
                break;
            }
        }

        if (invite == null) {
            return;
        }

        if (creator.isOnline()) {
            creator.sendMessage(Messages.sentInviteCancelled.replace("{name}", invite.getInvitedPlayer().getName()));
        }
        if (invite.getInvitedPlayer().isOnline()) {
            invite.getInvitedPlayer().sendMessage(Messages.receivedInviteCancelled.replace("{name}", creator.getName()));
        }

        invites.remove(invite);
        Bukkit.getScheduler().cancelTask(invite.getTaskID());
    }

    /**
     * Handles when an invitation expires
     * @param invite the invite
     */
    private void inviteExpired(Invite invite) {
        if (!invites.contains(invite)) return;

        Bukkit.getScheduler().cancelTask(invite.getTaskID());

        if (invite.getCreator().isOnline()) {
            invite.getCreator().sendMessage(Messages.sentInviteExpired.replace("{name}", invite.getInvitedPlayer().getName()));
        }
        if (invite.getInvitedPlayer().isOnline()) {
            invite.getInvitedPlayer().sendMessage(Messages.receivedInviteExpired.replace("{name}", invite.getCreator().getName()));
        }

        invites.remove(invite);
    }


    /**
     * Determines if this player has an outstanding invite.
     * @param player The player to check
     * @return True if the player is the creator or invitedPlayer of any current outstanding invite, false otherwise.
     */
    public boolean doesPlayerHaveOutstandingInvite(Player player) {
        for (Invite invite : invites) {
            if (player.getUniqueId().equals(invite.getCreator().getUniqueId()) || player.getUniqueId().equals(invite.getInvitedPlayer().getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the player is the creator of an outstanding invite.
     * @param player The player to check
     * @return True if the player is the creator of any current outstanding invite, false otherwise.
     */
    public boolean isPlayerCreatorOfInvite(Player player) {
        for (Invite invite : invites) {
            if (player.getUniqueId().equals(invite.getCreator().getUniqueId())) {
                return true;
            }
        }
        return false;
    }
}
