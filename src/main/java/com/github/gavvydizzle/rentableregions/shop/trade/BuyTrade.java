package com.github.gavvydizzle.rentableregions.shop.trade;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.mittenmc.serverutils.Numbers;
import org.bukkit.entity.Player;

/**
 * Trade where the creator is paying the recipient for their shop
 */
public class BuyTrade extends Trade {

    private final Shop recipientShop;

    public BuyTrade(Player creator, Player recipient, Shop recipientShop, double money, String ccm, String rcm) {
        super(creator, recipient, money, ccm, rcm);
        this.recipientShop = recipientShop;
    }

    @Override
    public void recipientConfirmTrade() {
        // Check that the player is still an owner of their shop
        if (!recipientShop.isOwner(getRecipient().getUniqueId())) {
            getCreator().sendMessage(Messages.noLongerOwner.replace("{name}", getRecipient().getName()));
            getRecipient().sendMessage(Messages.selfNoLongerOwner);
            return;
        }

        // Check that the player still has enough money
        if (RentableRegions.getEconomy().getBalance(getCreator()) < getMoney()) {
            getCreator().sendMessage(Messages.selfNotEnoughMoney);
            getRecipient().sendMessage(Messages.notEnoughMoney.replace("{name}", getCreator().getName()));
            return;
        }

        // Check if the player still meets join requirements
        if (recipientShop.doesNotMeetJoinRequirements(getCreator())) {
            getCreator().sendMessage(Messages.selfDoesNotMeetRequirements.replace("{name}", getRecipient().getName()));
            getRecipient().sendMessage(Messages.doesNotMeetRequirements.replace("{name}", getCreator().getName()));
            return;
        }

        recipientShop.messageAllMembers(Messages.messageMembersOnSuccessfulTrade.replace("{name}", getRecipient().getName()).replace("{name2}", getCreator().getName()));
        recipientShop.unclaim(false);
        recipientShop.setOwner(getCreator().getUniqueId());

        RentableRegions.getEconomy().withdrawPlayer(getCreator(), getMoney());
        RentableRegions.getEconomy().depositPlayer(getRecipient(), getMoney());

        getCreator().sendMessage(Messages.successfulTradeGiveMoney.replace("{money}", Numbers.withSuffix((long) getMoney())));
        getRecipient().sendMessage(Messages.successfulTradeReceiveMoney.replace("{money}", Numbers.withSuffix((long) getMoney())));

        RentableRegions.getInstance().getShopLogger().writeToLog(getCreator().getName() + " purchased shop " + recipientShop.getId() + " from " + getRecipient().getName() + " with " + Numbers.getTimeFormatted(recipientShop.getRentManager().getSecondsRemaining()) + " for $" + Numbers.round(getMoney(), 2));
    }
}
