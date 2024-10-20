package Shoey.ColonyHelper.Util;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

public class SizeSort implements java.util.Comparator<MarketAPI> {
    @Override
    public int compare(MarketAPI o1, MarketAPI o2) {

        return o2.getSize() - o1.getSize();
    }
}
