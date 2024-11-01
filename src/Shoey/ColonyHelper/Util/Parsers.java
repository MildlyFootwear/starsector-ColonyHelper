package Shoey.ColonyHelper.Util;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static Shoey.ColonyHelper.MainPlugin.*;

public class Parsers {

    public static boolean canIndustryUseItem(Industry i, SpecialItemSpecAPI item)
    {
        if (i == null || i.getSpecialItem() != null && i.getSpecialItem().getId() != null)
            return false;
        String temp = item.getParams();
        temp = temp.replaceAll(", ", ",");
        String[] industriesforitem = temp.split(",");
        for (String s : industriesforitem)
        {
            if (i.getId().equals(s))
            {
                for (InstallableIndustryItemPlugin ip : i.getInstallableItems()) {
                    if (ip != null && ip.isMenuItemEnabled() && ip.canBeInstalled(new SpecialItemData(item.getId(), item.getParams()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static List<MarketAPI> findItemMarketsForFaction(SpecialItemSpecAPI item, FactionAPI faction)
    {
        List<MarketAPI> factionMarkets = factionMarketMap.get(faction);
        List<MarketAPI> marketsUsable = new ArrayList<>();
        if (factionMarkets == null || factionMarkets.isEmpty() || item.getParams() == null)
            return marketsUsable;
        Collections.sort(factionMarkets, new SizeSort());
        for (MarketAPI m : factionMarkets)
        {
            if (m.getSize() < 3)
                continue;
            for (Industry i : m.getIndustries())
            {

                if (i == null || i.getSpecialItem() != null && i.getSpecialItem().getId() != null)
                    continue;

                if (canIndustryUseItem(i, item)) {
                    marketsUsable.add(m);
                    break;
                }
            }

        }
        return marketsUsable;
    }
}
