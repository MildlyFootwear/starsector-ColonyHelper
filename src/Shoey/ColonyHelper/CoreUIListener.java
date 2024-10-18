package Shoey.ColonyHelper;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreInteractionListener;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.CoreUITabListener;

import java.util.ArrayList;
import java.util.List;

import static Shoey.ColonyHelper.MainPlugin.*;

public class CoreUIListener implements CoreInteractionListener, CoreUITabListener {
    @Override
    public void coreUIDismissed() {
        resetSIDescs();
        for (List<MarketAPI> l : factionMarketMap.values())
            l.clear();
    }

    @Override
    public void reportAboutToOpenCoreTab(CoreUITabId tab, Object param) {
        resetSIDescs();

        if (factionMarketMap.isEmpty())
            genFacMarketMap();

        GenDesc(factionMarketMap.get(Global.getSector().getPlayerFaction()));
    }
}
