package com.github.gavvydizzle.rentableregions.shop.trade;

import com.github.gavvydizzle.rentableregions.utils.Messages;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class Trade {

    private final static String TRADE_COMMAND = "/market trade accept";

    private final Player creator;
    private final Player recipient;
    private final String creatorConfirmationMessage;
    private final String recipientConfirmationMessage;
    private final double money;
    private int taskID;

    public Trade(Player creator, Player recipient, double money, String ccm, String rcm) {
        this.creator = creator;
        this.recipient = recipient;
        this.money = money;
        this.creatorConfirmationMessage = ccm;
        this.recipientConfirmationMessage = rcm;
        this.taskID = -1;
    }

    /**
     * Asks the creator to confirm this trade by sending them a chat message
     */
    public void askCreatorForConfirmation() {
        creator.sendMessage(creatorConfirmationMessage);
        TextComponent textComponent = new TextComponent(Messages.creatorConfirmation);
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, TRADE_COMMAND));
        creator.spigot().sendMessage(textComponent);
    }

    /**
     * Called when the creator confirms their trade.
     * Asks the recipient to confirm this trade by sending them a chat message
     */
    public void creatorConfirmTrade() {
        recipient.sendMessage(recipientConfirmationMessage);
        creator.sendMessage(Messages.creatorSentTrade);
        TextComponent textComponent = new TextComponent(Messages.recipientConfirmation);
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, TRADE_COMMAND));
        recipient.spigot().sendMessage(textComponent);
    }

    /**
     * Called after both players have agreed to the trade.
     * Child implementations will push changes to the shop(s)
     */
    public abstract void recipientConfirmTrade();

    public Player getCreator() {
        return creator;
    }

    public Player getRecipient() {
        return recipient;
    }

    public double getMoney() {
        return money;
    }

    public void setTaskID(int taskID) {
        if (taskID != -1) {
            throw new RuntimeException("Tried to override a non-cancelled task");
        }
        this.taskID = taskID;
    }

    public void cancelTask() {
        Bukkit.getScheduler().cancelTask(taskID);
        taskID = -1;
    }
}
