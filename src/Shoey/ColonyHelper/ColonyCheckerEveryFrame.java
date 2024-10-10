package Shoey.ColonyHelper;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

import java.util.ArrayList;
import java.util.List;

import static Shoey.ColonyHelper.MainPlugin.*;

public class ColonyCheckerEveryFrame implements EveryFrameScript {

    boolean tick = false;

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {

        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != null)
        {
            if (!tick)
            {
                tick = true;
                InitBaseSIDescMap();
                List<MarketAPI> playerMarkets = new ArrayList<>();
                for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy())
                {
                    if (m.getFaction() == Global.getSector().getPlayerFaction())
                        playerMarkets.add(m);
                }
                GenDesc(Global.getSector().getEconomy().getMarketsCopy());
            }
        } else {

            if (tick)
            {
                tick = false;
                resetSIDescs();
            }

        }

    }
}
