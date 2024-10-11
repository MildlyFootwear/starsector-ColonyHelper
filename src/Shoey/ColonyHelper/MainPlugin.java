package Shoey.ColonyHelper;
import Shoey.ColonyHelper.Util.SizeSort;
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

import java.util.*;


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
            if (s.contains("Useful for colon"))
            {
                log.error(item.getId()+": "+item.getName()+" has already been modified, this is likely an error.");
            } else {
//                log.debug("Putting in map; "+item.getName()+": "+item.getDesc());
                DefaultDescriptions.put(item, s);
            }

        }
    }

    public static void GenDesc(List<MarketAPI> validMarkets)
    {
        log.debug("Generating descriptions");
//        for (MarketAPI m : validMarkets)
//        {
//            if (m == null)
//                continue;
//            log.debug("Checking market "+m.getName());
//            for (Industry i : m.getIndustries())
//            {
//                if (i.getSpecialItem() == null || i.getSpecialItem().getId() == null)
//                {
//                    log.debug("SpecItem for "+m.getName()+i.getCurrentName()+ " not found");
//                } else {
//                    log.debug("SpecItem for "+m.getName()+i.getCurrentName()+ ": "+i.getSpecialItem().getId());
//                }
//            }
//        }

        for (SpecialItemSpecAPI item : DefaultDescriptions.keySet())
        {
            //log.debug("Generating for "+item.getName());

            String industriesforitem = item.getParams();
            Collections.sort(validMarkets, new SizeSort());
            List<String> marketsUsable = new ArrayList<>();
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
                                marketsUsable.add(m.getName() + " ("+m.getSize()+")");
                                needBreak = true;
                                break;
                            }
                        }
                        if (needBreak)
                            break;
                    }
                }
            }
            String s = item.getDesc();
            if (!marketsUsable.isEmpty() && !s.contains("Useful for colonies "))
            {
                s += "\n\n";
                if (marketsUsable.size() == 1)
                {
                    s += "Useful for colony "+marketsUsable.get(0)+".";
                } else {
                    s += "Useful for colonies ";
                    for (int i = 0; i < marketsUsable.size() && i < 5; i++)
                    {
                        s += marketsUsable.get(i)+", ";
                    }
                    s = s.substring(0, s.length() - 2)+".";
                }
                item.setDesc(s);
            } else if (s.contains("Useful for colonies "))
                log.error("Description for "+item.getName()+" already modified");

        }
    }

    public static void resetSIDescs()
    {
        if (DefaultDescriptions.isEmpty()) {
            InitBaseSIDescMap();
            return;
        }
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
        Global.getSector().getListenerManager().addListener(new CoreUIListener(), true);
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
