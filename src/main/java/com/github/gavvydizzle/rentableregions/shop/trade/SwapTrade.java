package com.github.gavvydizzle.rentableregions.shop.trade;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.mittenmc.serverutils.Numbers;
import org.bukkit.entity.Player;

/**
 * Trade where the players are swapping shops and money can go either way
 */
public class SwapTrade extends Trade {

    private final Shop creatorShop, recipientShop;
    private final boolean creatorGiveMoney;

    public SwapTrade(Player creator, Player recipient, Shop creatorShop, Shop recipientShop, double money, boolean creatorGiveMoney, String ccm, String rcm) {
        super(creator, recipient, money, ccm, rcm);
        this.creatorShop = creatorShop;
        this.recipientShop = recipientShop;
        this.creatorGiveMoney = creatorGiveMoney;
    }

    @Override
    public void recipientConfirmTrade() {
        // Check that both players are still owners of their shops
        if (!creatorShop.isOwner(getCreator().getUniqueId())) {
            getCreator().sendMessage(Messages.selfNoLongerOwner);
            getRecipient().sendMessage(Messages.noLongerOwner.replace("{name}", getCreator().getName()));
            return;
        }
        else if (!recipientShop.isOwner(getRecipient().getUniqueId())) {
            getCreator().sendMessage(Messages.noLongerOwner.replace("{name}", getRecipient().getName()));
            getRecipient().sendMessage(Messages.selfNoLongerOwner);
            return;
        }

        // Check that the correct player still has enough money
        if (creatorGiveMoney) {
            if (RentableRegions.getEconomy().getBalance(getCreator()) < getMoney()) {
                getCreator().sendMessage(Messages.selfNotEnoughMoney);
                getRecipient().sendMessage(Messages.notEnoughMoney.replace("{name}", getCreator().getName()));
                return;
            }
        }
        else {
            if (RentableRegions.getEconomy().getBalance(getRecipient()) < getMoney()) {
                getCreator().sendMessage(Messages.notEnoughMoney.replace("{name}", getRecipient().getName()));
                getRecipient().sendMessage(Messages.selfNotEnoughMoney);
                return;
            }
        }

        // Check if the players still meet join requirements
        if (recipientShop.doesNotMeetJoinRequirements(getCreator())) {
            getCreator().sendMessage(Messages.selfDoesNotMeetRequirements.replace("{name}", getRecipient().getName()));
            getRecipient().sendMessage(Messages.doesNotMeetRequirements.replace("{name}", getCreator().getName()));
            return;
        }
        else if (creatorShop.doesNotMeetJoinRequirements(getRecipient())) {
            getCreator().sendMessage(Messages.doesNotMeetRequirements.replace("{name}", getRecipient().getName()));
            getRecipient().sendMessage(Messages.selfDoesNotMeetRequirements.replace("{name}", getCreator().getName()));
            return;
        }

        recipientShop.messageAllMembers(Messages.messageMembersOnSuccessfulTrade.replace("{name}", getRecipient().getName()).replace("{name2}", getCreator().getName()));
        recipientShop.unclaim(false);

        creatorShop.messageAllMembers(Messages.messageMembersOnSuccessfulTrade.replace("{name}", getCreator().getName()).replace("{name2}", getRecipient().getName()));
        creatorShop.unclaim(false);

        // Add new owners after both unclaims have completed
        recipientShop.setOwner(getCreator().getUniqueId());
        creatorShop.setOwner(getRecipient().getUniqueId());

        // Handle payment, messages, and logging
        if (creatorGiveMoney) {
            RentableRegions.getEconomy().withdrawPlayer(getCreator(), getMoney());
            RentableRegions.getEconomy().depositPlayer(getRecipient(), getMoney());

            getCreator().sendMessage(Messages.successfulTradeGiveMoney.replace("{money}", Numbers.withSuffix((long) getMoney())));
            getRecipient().sendMessage(Messages.successfulTradeReceiveMoney.replace("{money}", Numbers.withSuffix((long) getMoney())));

            RentableRegions.getInstance().getShopLogger().writeToLog(getCreator().getName() + " (shop " + creatorShop.getId() + ") and " + getRecipient().getName() + " (shop " + recipientShop.getId() + ") traded shops. " + getCreator().getName() + " paid $" + Numbers.round(getMoney(), 2) + " for the transaction.");
        }
        else {
            RentableRegions.getEconomy().depositPlayer(getCreator(), getMoney());
            RentableRegions.getEconomy().withdrawPlayer(getRecipient(), getMoney());

            getCreator().sendMessage(Messages.successfulTradeReceiveMoney.replace("{money}", Numbers.withSuffix((long) getMoney())));
            getRecipient().sendMessage(Messages.successfulTradeGiveMoney.replace("{money}", Numbers.withSuffix((long) getMoney())));

            RentableRegions.getInstance().getShopLogger().writeToLog(getCreator().getName() + " (shop " + creatorShop.getId() + ") and " + getRecipient().getName() + " (shop " + recipientShop.getId() + ") traded shops. " + getRecipient().getName() + " paid $" + Numbers.round(getMoney(), 2) + " for the transaction.");
        }

        RentableRegions.getInstance().getShopLogger().writeToLog(" - "  + getCreator().getName() + " now owns shop " + recipientShop.getId() + " and it has " + Numbers.getTimeFormatted(recipientShop.getRentManager().getSecondsRemaining()));
        RentableRegions.getInstance().getShopLogger().writeToLog(" - "  + getRecipient().getName() + " now owns shop " + creatorShop.getId() + " and it has " + Numbers.getTimeFormatted(creatorShop.getRentManager().getSecondsRemaining()));
    }
}
