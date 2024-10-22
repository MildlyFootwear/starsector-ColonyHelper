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

public class MarketParsers {

    public static List<MarketAPI> findItemMarketsForFaction(SpecialItemSpecAPI item, FactionAPI faction)
    {
        List<MarketAPI> validMarkets = factionMarketMap.get(faction);
        String industriesforitem = item.getParams();
        List<MarketAPI> marketsUsable = new ArrayList<>();
        if (validMarkets == null || validMarkets.isEmpty() || industriesforitem == null)
            return marketsUsable;
        Collections.sort(validMarkets, new SizeSort());
        for (MarketAPI m : validMarkets)
        {

            for (Industry i : m.getIndustries())
            {

                boolean needBreak = false;

                if (i == null || i.getSpecialItem() != null && i.getSpecialItem().getId() != null)
                    continue;

                if (industriesforitem.contains(i.getSpec().getId())) {
                    for (InstallableIndustryItemPlugin ip : i.getInstallableItems()) {
                        if (ip != null && ip.isMenuItemEnabled() && ip.canBeInstalled(new SpecialItemData(item.getId(), item.getParams()))) {
                            marketsUsable.add(m);
                            needBreak = true;
                            break;
                        }
                    }
                    if (needBreak)
                        break;
                }

            }

        }
        return marketsUsable;
    }
}
