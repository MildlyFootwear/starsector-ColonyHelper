package Shoey.ColonyHelper;
import Shoey.ColonyHelper.Util.SizeSort;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import kotlin.random.Random;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LogLevel;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class MainPlugin extends BaseModPlugin {

    public static Logger log = Global.getLogger(MainPlugin.class);

    public static HashMap<SpecialItemSpecAPI, String> DefaultDescriptions = new HashMap<>();
    public static HashMap<FactionAPI, List<MarketAPI>> factionMarketMap = new HashMap<>();

    static Level logLevel = Level.DEBUG;

    public static void genFacMarketMap()
    {
        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy())
        {
            FactionAPI f  = m.getFaction();
            if (!factionMarketMap.containsKey(f))
                factionMarketMap.put(f, new ArrayList<MarketAPI>());
            factionMarketMap.get(f).add(m);
        }
        if (logLevel == Level.DEBUG)
        {
            for (FactionAPI f : factionMarketMap.keySet()) {
                log.debug(f.getDisplayName());
                for(MarketAPI m : factionMarketMap.get(f))
                {
                    log.debug(m.getName());
                }
            }
        }
    }

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
                DefaultDescriptions.put(item, s);
            }

        }
    }

    public static void setDesc(SpecialItemSpecAPI item, FactionAPI faction, List<FactionAPI> alreadychecked)
    {
        log.debug("Looking for markets for "+item.getName()+" in faction "+faction.getDisplayName());
        List<MarketAPI> validMarkets = factionMarketMap.get(faction);
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
        if (!marketsUsable.isEmpty() && !s.contains("Useful for "+faction.getDisplayName()+" colon"))
        {
            s += "\n\n";
            if (marketsUsable.size() == 1)
            {
                s += "Useful for "+faction.getDisplayName()+" colony "+marketsUsable.get(0)+".";
            } else {
                s += "Useful for "+faction.getDisplayName()+" colonies ";
                for (int i = 0; i < marketsUsable.size() && i < 5; i++)
                {
                    s += marketsUsable.get(i)+", ";
                }
                s = s.substring(0, s.length() - 2)+".";
            }
            item.setDesc(s);
        } else if (s.contains("Useful for ")) {
            log.error("Description for " + item.getName() + " already modified");
        } else if (marketsUsable.isEmpty()) {
            log.debug("Found no markets for "+item.getName()+" in faction "+faction.getDisplayName());
            List<FactionAPI> checked = new ArrayList<>();
            if (alreadychecked != null) {
                checked.addAll(0, alreadychecked);
            }
            checked.add(faction);
            FactionAPI f = null;
            float iRelate = 0.1f;
            for (FactionAPI t : factionMarketMap.keySet())
            {
                if (t.getRelToPlayer().getRel() > iRelate && !checked.contains(t))
                {
                    f = t;
                    iRelate = t.getRelToPlayer().getRel();
                }
            }
            if (f != null)
            {
                setDesc(item, f, checked);
            }
        }
    }

    public static void setDesc(SpecialItemSpecAPI item, FactionAPI faction)
    {
        setDesc(item, faction, null);
    }

    public static void GenDesc()
    {
        log.debug("Generating descriptions");

        FactionAPI f = Global.getSector().getPlayerFaction();
        if (f == null)
        {
            float iRelate = 0.1f;
            for (FactionAPI t : factionMarketMap.keySet())
            {
                if (t.getRelToPlayer().getRel() > iRelate)
                {
                    f = t;
                    iRelate = t.getRelToPlayer().getRel();
                }
            }
        }
        if (f == null)
            return;
        for (SpecialItemSpecAPI item : DefaultDescriptions.keySet())
        {
            setDesc(item, f);
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
        log.setLevel(logLevel);
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
