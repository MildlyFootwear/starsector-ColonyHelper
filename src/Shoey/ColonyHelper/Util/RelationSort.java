package Shoey.ColonyHelper.Util;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

public class RelationSort implements java.util.Comparator<FactionAPI> {
    @Override
    public int compare(FactionAPI o1, FactionAPI o2) {
        return o2.getRelToPlayer().getRepInt() - o1.getRelToPlayer().getRepInt();
    }
}
