package Shoey.ColonyHelper;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import kotlin.random.Random;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class MainPlugin extends BaseModPlugin {

    private static Logger log = Global.getLogger(MainPlugin.class);

    public static HashMap<SpecialItemSpecAPI, String> DefaultDescriptions = new HashMap<>();

    public static void InitBaseSIDescMap()
    {
        for (SpecialItemSpecAPI item : Global.getSettings().getAllSpecialItemSpecs())
        {
            if (item == null || item.getId() == null || !item.hasTag("pather4"))
                continue;
            String s = item.getDesc();
            if (s.contains("Useful for colonies "))
            {
                log.error(item.getId()+": "+item.getName()+" has already been modified, this is likely an error.");
            } else {
                log.debug("Putting in map; "+item.getName()+": "+item.getDesc());
                DefaultDescriptions.put(item, s);
            }

        }
    }

    public static String GenDesc(List<MarketAPI> validMarkets)
    {
        log.debug("Generating descriptions");
//        List<MarketAPI> marketsWithItemlesses = new ArrayList<>();
//        for (MarketAPI m : validMarkets)
//        {
//            if (m == null)
//                continue;
//            for (Industry i : m.getIndustries())
//            {
//                if (i.getSpecialItem() == null || i.getSpecialItem().getId() == null)
//                {
//                    marketsWithItemlesses.add(m);
////                    log.debug("SpecItem for "+m.getName()+i.getCurrentName()+ " not found");
//                    break;
////                } else {
////                    log.debug("SpecItem for "+m.getName()+i.getCurrentName()+ ": "+i.getSpecialItem().getId());
//                }
//            }
//
//        }

        for (SpecialItemSpecAPI item : DefaultDescriptions.keySet())
        {
            log.debug("Generating for "+item.getName());

            String industriesforitem = item.getParams();
            String addendum = "Useful for colonies ";
            for (MarketAPI m : validMarkets)
            {
                for (Industry i : m.getIndustries())
                {
                    boolean needBreak = false;
                    if (i == null || i.getSpecialItem() != null && i.getSpecialItem().getId() != null)
                        continue;

                    if (industriesforitem.contains(i.getSpec().getId())) {
                        for (InstallableIndustryItemPlugin ip : i.getInstallableItems()) {
                            if (ip == null || !ip.isMenuItemEnabled())
                                continue;
                            else if (ip.canBeInstalled(new SpecialItemData(item.getId(), item.getParams()))) {
                                addendum += m.getName() + ", ";
                                needBreak = true;
                                break;
                            }
                        }
                        if (needBreak)
                            break;
                    }

//
//                    if (industriesforitem.contains(i.getSpec().getId())) {
//                        addendum += m.getName()+", ";
//                        break;
//                    }
                }
            }
            String s = item.getDesc();
            if (addendum != "Useful for colonies " && !s.contains("Useful for colonies "))
            {
                s += "\n\n" + addendum.substring(0, addendum.length()-2)+".";
                item.setDesc(s);
            } else if (s.contains("Useful for colonies "))
                log.error("Description for "+item.getName()+" already modified");

        }

        return null;
    }

    public static void resetSIDescs()
    {
        log.debug("Resetting item descriptions");
        for (SpecialItemSpecAPI item : DefaultDescriptions.keySet())
        {
            item.setDesc(DefaultDescriptions.get(item));
        }
    }

    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();
        log.setLevel(Level.DEBUG);
    }

    @Override
    public void onGameLoad(boolean b) {
        super.onGameLoad(b);
        Global.getSector().addTransientScript(new ColonyCheckerEveryFrame());
    }

    @Override
    public void beforeGameSave()
    {
        super.beforeGameSave();
    }

    @Override
    public void afterGameSave()
    {
        super.afterGameSave();
    }
}
