package com.github.gavvydizzle.rentableregions.shop.trade;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.mittenmc.serverutils.Numbers;
import org.bukkit.entity.Player;

/**
 * Trade where the recipient is paying the creator for their shop
 */
public class SellTrade extends Trade {

    private final Shop creatorShop;

    public SellTrade(Player creator, Player recipient, Shop creatorShop, double money, String ccm, String rcm) {
        super(creator, recipient, money, ccm, rcm);
        this.creatorShop = creatorShop;
    }

    @Override
    public void recipientConfirmTrade() {
        // Check that the player is still an owner of their shop
        if (!creatorShop.isOwner(getCreator().getUniqueId())) {
            getCreator().sendMessage(Messages.selfNoLongerOwner);
            getRecipient().sendMessage(Messages.noLongerOwner.replace("{name}", getCreator().getName()));
            return;
        }

        // Check that the player still has enough money
        if (RentableRegions.getEconomy().getBalance(getRecipient()) < getMoney()) {
            getCreator().sendMessage(Messages.notEnoughMoney.replace("{name}", getRecipient().getName()));
            getRecipient().sendMessage(Messages.selfNotEnoughMoney);
            return;
        }

        // Check if the player still meets join requirements
        if (creatorShop.doesNotMeetJoinRequirements(getRecipient())) {
            getCreator().sendMessage(Messages.doesNotMeetRequirements.replace("{name}", getRecipient().getName()));
            getRecipient().sendMessage(Messages.selfDoesNotMeetRequirements.replace("{name}", getCreator().getName()));
            return;
        }

        creatorShop.messageAllMembers(Messages.messageMembersOnSuccessfulTrade.replace("{name}", getCreator().getName()).replace("{name2}", getRecipient().getName()));
        creatorShop.unclaim(false);
        creatorShop.setOwner(getRecipient().getUniqueId());

        RentableRegions.getEconomy().depositPlayer(getCreator(), getMoney());
        RentableRegions.getEconomy().withdrawPlayer(getRecipient(), getMoney());

        getCreator().sendMessage(Messages.successfulTradeReceiveMoney.replace("{money}", Numbers.withSuffix((long) getMoney())));
        getRecipient().sendMessage(Messages.successfulTradeGiveMoney.replace("{money}", Numbers.withSuffix((long) getMoney())));

        RentableRegions.getInstance().getShopLogger().writeToLog(getCreator().getName() + " sold shop " + creatorShop.getId() + " to " + getRecipient().getName() + " with " + Numbers.getTimeFormatted(creatorShop.getRentManager().getSecondsRemaining()) + " for $" + Numbers.round(getMoney(), 2));
    }
}
