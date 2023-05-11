package com.github.gavvydizzle.rentableregions.shop.trade;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.mittenmc.serverutils.Numbers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeManager implements Listener {

    private final static int TRADE_LENGTH_SECONDS = 15;
    private final static int MINIMUM_SECONDS_REMAINING_FOR_TRADE = 1800; // NEVER ALLOW THIS VALUE TO BE LESS THAN THE LOTTERY START TIME

    private final Map<UUID, Trade> playersTrading;
    private final ArrayList<UUID> pendingConfirmations; // Who is waiting to confirm a trade action

    public TradeManager() {
        playersTrading = new HashMap<>();
        pendingConfirmations = new ArrayList<>();
    }

    /**
     * Creates a new BuyTrade request
     * @param creator The creator of the trade
     * @param recipient The other player involved in the trade
     * @param recipientShop The shop of the recipient
     * @param money The amount of money the creator is paying the recipient
     */
    public void createBuyTrade(Player creator, Player recipient, Shop recipientShop, double money) {
        assert money > 0;

        if (recipientShop.getRentManager().getSecondsRemaining() < MINIMUM_SECONDS_REMAINING_FOR_TRADE) {
            creator.sendMessage(Messages.notEnoughTimeRemaining.replace("{name}", recipient.getName()).replace("{time}", Numbers.getTimeFormatted(MINIMUM_SECONDS_REMAINING_FOR_TRADE)));
            return;
        }

        if (RentableRegions.getEconomy().getBalance(creator) < money) {
            creator.sendMessage(Messages.selfNotEnoughMoney);
            return;
        }

        if (recipientShop.doesNotMeetJoinRequirements(creator)) {
            creator.sendMessage(Messages.selfDoesNotMeetRequirements.replace("{name}", recipient.getName()));
            return;
        }

        BuyTrade trade = new BuyTrade(creator, recipient, recipientShop, money,
                Messages.buyTradeCreator.replace("{name}", recipient.getName()).replace("{money}", Numbers.withSuffix((long) money)).replace("{id}", recipientShop.getId()),
                Messages.buyTradeRecipient.replace("{name}", creator.getName()).replace("{money}", Numbers.withSuffix((long) money))
        );
        playersTrading.put(creator.getUniqueId(), trade);
        playersTrading.put(recipient.getUniqueId(), trade);

        int taskID = startCreatorAcceptTimer(creator, recipient);
        trade.setTaskID(taskID);

        trade.askCreatorForConfirmation();
    }

    /**
     * Creates a new SellTrade request
     * @param creator The creator of the trade
     * @param recipient The other player involved in the trade
     * @param creatorShop The shop of the creator
     * @param money The amount of money the recipient is paying the creator
     */
    public void createSellTrade(Player creator, Player recipient, Shop creatorShop, double money) {
        assert money > 0;

        if (creatorShop.getRentManager().getSecondsRemaining() < MINIMUM_SECONDS_REMAINING_FOR_TRADE) {
            creator.sendMessage(Messages.selfNotEnoughTimeRemaining.replace("{time}", Numbers.getTimeFormatted(MINIMUM_SECONDS_REMAINING_FOR_TRADE)));
            return;
        }

        if (RentableRegions.getEconomy().getBalance(recipient) < money) {
            creator.sendMessage(Messages.notEnoughMoney.replace("{name}", recipient.getName()));
            return;
        }

        if (creatorShop.doesNotMeetJoinRequirements(recipient)) {
            creator.sendMessage(Messages.doesNotMeetRequirements.replace("{name}", recipient.getName()));
            return;
        }

        SellTrade trade = new SellTrade(creator, recipient, creatorShop, money,
                Messages.sellTradeCreator.replace("{name}", recipient.getName()).replace("{money}", Numbers.withSuffix((long) money)),
                Messages.sellTradeRecipient.replace("{name}", creator.getName()).replace("{money}", Numbers.withSuffix((long) money)).replace("{id}", creatorShop.getId())
        );
        playersTrading.put(creator.getUniqueId(), trade);
        playersTrading.put(recipient.getUniqueId(), trade);

        int taskID = startCreatorAcceptTimer(creator, recipient);
        trade.setTaskID(taskID);

        trade.askCreatorForConfirmation();
    }

    /**
     *
     * @param creator The creator of the trade
     * @param recipient The other player involved in the trade
     * @param creatorShop The shop of the creator
     * @param recipientShop The shop of the recipient
     * @param money The amount of money
     * @param creatorGiveMoney If true, the creator pays the recipient, else the recipient pays the creator
     */
    public void createSwapTrade(Player creator, Player recipient, Shop creatorShop, Shop recipientShop, double money, boolean creatorGiveMoney) {
        assert money > 0;

        // Check that both shops have enough time remaining
        if (creatorShop.getRentManager().getSecondsRemaining() < MINIMUM_SECONDS_REMAINING_FOR_TRADE) {
            creator.sendMessage(Messages.selfNotEnoughTimeRemaining.replace("{time}", Numbers.getTimeFormatted(MINIMUM_SECONDS_REMAINING_FOR_TRADE)));
            return;
        }
        else if (recipientShop.getRentManager().getSecondsRemaining() < MINIMUM_SECONDS_REMAINING_FOR_TRADE) {
            creator.sendMessage(Messages.notEnoughTimeRemaining.replace("{name}", recipient.getName()).replace("{time}", Numbers.getTimeFormatted(MINIMUM_SECONDS_REMAINING_FOR_TRADE)));
            return;
        }

        // Check that the correct player has enough money
        if (creatorGiveMoney) {
            if (RentableRegions.getEconomy().getBalance(creator) < money) {
                creator.sendMessage(Messages.selfNotEnoughMoney);
                return;
            }
        }
        else {
            if (RentableRegions.getEconomy().getBalance(recipient) < money) {
                creator.sendMessage(Messages.notEnoughMoney.replace("{name}", recipient.getName()));
                return;
            }
        }

        // Check if the players are high enough level to join
        if (recipientShop.doesNotMeetJoinRequirements(creator)) {
            creator.sendMessage(Messages.selfDoesNotMeetRequirements.replace("{name}", recipient.getName()));
            return;
        }
        else if (creatorShop.doesNotMeetJoinRequirements(recipient)) {
            creator.sendMessage(Messages.doesNotMeetRequirements.replace("{name}", recipient.getName()));
            return;
        }

        // Create the correct trade
        SwapTrade trade;
        if (creatorGiveMoney) {
            trade = new SwapTrade(creator, recipient, creatorShop, recipientShop, money, true,
                    Messages.swapTradeCreator_give.replace("{name}", recipient.getName()).replace("{money}", Numbers.withSuffix((long) money)).replace("{id}", recipientShop.getId()),
                    Messages.swapTradeRecipient_give.replace("{name}", creator.getName()).replace("{money}", Numbers.withSuffix((long) money)).replace("{id}", creatorShop.getId())
            );
        }
        else {
            trade = new SwapTrade(creator, recipient, creatorShop, recipientShop, money, false,
                    Messages.swapTradeCreator_request.replace("{name}", recipient.getName()).replace("{money}", Numbers.withSuffix((long) money)).replace("{id}", recipientShop.getId()),
                    Messages.swapTradeRecipient_request.replace("{name}", creator.getName()).replace("{money}", Numbers.withSuffix((long) money)).replace("{id}", creatorShop.getId())
            );
        }
        playersTrading.put(creator.getUniqueId(), trade);
        playersTrading.put(recipient.getUniqueId(), trade);

        int taskID = startCreatorAcceptTimer(creator, recipient);
        trade.setTaskID(taskID);

        trade.askCreatorForConfirmation();
    }

    /**
     * Starts the task that will cancel the trade if the creator does not accept it
     * @param creator The creator
     * @param recipient The recipient
     * @return The task's ID
     */
    private int startCreatorAcceptTimer(Player creator, Player recipient) {
        return Bukkit.getScheduler().scheduleSyncDelayedTask(RentableRegions.getInstance(), () -> {
            pendingConfirmations.remove(creator.getUniqueId());
            pendingConfirmations.remove(recipient.getUniqueId());
            playersTrading.remove(creator.getUniqueId());
            playersTrading.remove(recipient.getUniqueId());

            creator.sendMessage(Messages.tradeConfirmationExpired);
        }, TRADE_LENGTH_SECONDS * 20L);
    }

    /**
     * Starts the task that will end the trade if the recipient foes not accept it
     * @param creator The creator
     * @param recipient The recipient
     * @return The task's ID
     */
    private int startRecipientAcceptTimer(Player creator, Player recipient) {
        return Bukkit.getScheduler().scheduleSyncDelayedTask(RentableRegions.getInstance(), () -> {
            pendingConfirmations.remove(creator.getUniqueId());
            pendingConfirmations.remove(recipient.getUniqueId());
            playersTrading.remove(creator.getUniqueId());
            playersTrading.remove(recipient.getUniqueId());

            creator.sendMessage(Messages.selfTradeExpired.replace("{name}", recipient.getName()));
            recipient.sendMessage(Messages.tradeExpired.replace("{name}", creator.getName()));
        }, TRADE_LENGTH_SECONDS * 20L);
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent e) {
        pendingConfirmations.remove(e.getPlayer().getUniqueId());
        playersTrading.remove(e.getPlayer().getUniqueId());
    }

    /**
     * Handles when the creator confirms a trade or the recipient agrees to a trade.
     * @param player The player who confirmed
     */
    public void acceptTrade(Player player) {
        Trade trade = playersTrading.get(player.getUniqueId());
        if (trade == null) return;

        if (player.equals(trade.getCreator())) { // When the creator confirms the trade
            trade.cancelTask();

            int taskID = startRecipientAcceptTimer(trade.getCreator(), trade.getRecipient());
            trade.setTaskID(taskID);

            trade.creatorConfirmTrade();
        }
        else if (player.equals(trade.getRecipient())) { // When the recipient confirms the trade
            trade.cancelTask();

            pendingConfirmations.remove(trade.getCreator().getUniqueId());
            pendingConfirmations.remove(trade.getRecipient().getUniqueId());
            playersTrading.remove(trade.getCreator().getUniqueId());
            playersTrading.remove(trade.getRecipient().getUniqueId());

            trade.recipientConfirmTrade();
        }
    }

    /**
     * @param player The player
     * @return If the player has an active trade
     */
    public boolean hasActiveTrade(Player player) {
        return playersTrading.containsKey(player.getUniqueId());
    }

    /**
     * @param player The player
     * @return If the player has a pending trade confirmation
     */
    public boolean hasPendingConfirmation(Player player) {
        return pendingConfirmations.contains(player.getUniqueId());
    }
}
