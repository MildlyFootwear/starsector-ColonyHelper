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

public class CoreUIListener implements CoreUITabListener {

    @Override
    public void reportAboutToOpenCoreTab(CoreUITabId tab, Object param) {
        if (tab == CoreUITabId.CARGO || tab == CoreUITabId.OUTPOSTS) {
            factionMarketMap.clear();
            resetSIDescs();
            genFacMarketMap();
            GenDesc();
        }
    }
}
