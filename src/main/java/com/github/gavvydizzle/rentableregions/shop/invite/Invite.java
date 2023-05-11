package com.github.gavvydizzle.rentableregions.shop.invite;

import com.github.gavvydizzle.rentableregions.shop.Shop;
import org.bukkit.entity.Player;

public class Invite {

    private final Player creator, invitedPlayer;
    private final Shop shop;
    private int taskID;

    public Invite(Player creator, Player invitedPlayer, Shop shop) {
        this.creator = creator;
        this.invitedPlayer = invitedPlayer;
        this.shop = shop;
    }

    public Player getCreator() {
        return creator;
    }

    public Player getInvitedPlayer() {
        return invitedPlayer;
    }

    public Shop getShop() {
        return shop;
    }

    protected int getTaskID() {
        return taskID;
    }

    protected void setTaskID(int taskID) {
        this.taskID = taskID;
    }
}