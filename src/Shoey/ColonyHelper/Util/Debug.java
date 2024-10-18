package Shoey.ColonyHelper.Util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

import static Shoey.ColonyHelper.MainPlugin.log;

public class Debug {

    public static void dumpSectorMarketSpecialItemsInstalled()
    {
        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy())
        {
            if (m == null)
                continue;
            log.debug("Checking market "+m.getName());
            for (Industry i : m.getIndustries())
            {
                if (i.getSpecialItem() == null || i.getSpecialItem().getId() == null)
                {
                    log.debug("SpecItem for "+m.getName()+i.getCurrentName()+ " not found");
                } else {
                    log.debug("SpecItem for "+m.getName()+i.getCurrentName()+ ": "+i.getSpecialItem().getId());
                }
            }
        }
    }

}
