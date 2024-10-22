package Shoey.ColonyHelper;
import Shoey.ColonyHelper.Util.MarketParsers;
import Shoey.ColonyHelper.Util.SizeSort;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.*;

import static Shoey.ColonyHelper.Util.MarketParsers.findItemMarketsForFaction;


public class MainPlugin extends BaseModPlugin {

    public static Logger log = Global.getLogger(MainPlugin.class);

    public static HashMap<SpecialItemSpecAPI, String> DefaultDescriptions = new HashMap<>();
    public static HashMap<FactionAPI, List<MarketAPI>> factionMarketMap = new HashMap<>();

    public static int maxShown = 5;

    static Level logLevel = Level.INFO;

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
            if (s.contains("\nUseful for"))
            {
                log.error(item.getId()+": "+item.getName()+" has already been modified, this is likely an error.");
            } else {
                DefaultDescriptions.put(item, s);
            }

        }
    }

    public static void setDescWithColonies(SpecialItemSpecAPI item, FactionAPI faction)
    {
        setDescWithColonies(item, faction, true);
    }
    public static void setDescWithColonies(SpecialItemSpecAPI item, FactionAPI faction, boolean recursive)
    {
        setDescWithColonies(item, faction, recursive, null);
    }

    public static void setDescWithColonies(SpecialItemSpecAPI item, FactionAPI faction, boolean recursive, List<FactionAPI> alreadychecked)
    {
        log.info("Looking for markets for "+item.getName()+" in faction "+faction.getDisplayName()+", "+faction.getId());
        List<MarketAPI> marketsUsable = findItemMarketsForFaction(item, faction);

        String s = item.getDesc();
        if (!marketsUsable.isEmpty() && !s.contains("\nUseful for "))
        {
            s += "\n\n";
            if (marketsUsable.size() == 1)
            {
                MarketAPI m = marketsUsable.get(0);
                s += "Useful for "+faction.getDisplayName()+" colony ";
                s += m.getName() + " (" + m.getStarSystem().getBaseName() + ", " + m.getSize()+").";
            } else {
                s += "Useful for "+faction.getDisplayName()+" colonies ";
                for (int i = 0; i < marketsUsable.size() && i < maxShown; i++)
                {
                    MarketAPI m = marketsUsable.get(i);
                    s += m.getName() + " (" + m.getStarSystem().getBaseName() + ", " + m.getSize()+")";
                    if (i != maxShown -1) {
                        s += ", ";
                    } else {
                        if (marketsUsable.size() - maxShown > 0) {
                            s += " and " + (marketsUsable.size() - maxShown) + " more.";
                        } else {
                            s += ".";
                        }
                    }
                }
            }
            item.setDesc(s);
            return;
        } else if (s.contains("Useful for ")) {
            log.error("Description for " + item.getName() + " already modified");
            return;
        } else if (marketsUsable.isEmpty()) {
            log.debug("Found no markets for "+item.getName()+" in faction "+faction.getDisplayName());
        }

        if (!recursive)
            return;

        List<FactionAPI> checked = new ArrayList<>();
        if (alreadychecked != null) {
            checked.addAll(0, alreadychecked);
        }
        checked.add(faction);
        FactionAPI f = null;
        float iRelate = 0.1f;
        for (FactionAPI t : factionMarketMap.keySet())
        {
            float rel = t.getRelToPlayer().getRel();
            if (t.getId().equals("independent"))
                rel /= 2;
            if (!checked.contains(t) && rel > iRelate)
            {
                f = t;
                iRelate = t.getRelToPlayer().getRel();
            }
        }
        if (f != null && !checked.contains(f))
        {
            setDescWithColonies(item, f, true, checked);
        }

    }

    public static void GenDesc()
    {
        log.info("Generating descriptions");

        FactionAPI start = null;

        if (factionMarketMap.containsKey(Global.getSector().getPlayerFaction())) {
            log.info("Starting with player faction.");
            start = Global.getSector().getPlayerFaction();
        } else {
            float iRelate = 0.1f;
            for (FactionAPI t : factionMarketMap.keySet())
            {
                if (t.getRelToPlayer().getRel() > iRelate)
                {
                    start = t;
                    iRelate = t.getRelToPlayer().getRel();
                }
            }
        }

        if (!factionMarketMap.containsKey(start))
            return;

        for (SpecialItemSpecAPI item : DefaultDescriptions.keySet())
        {
            setDescWithColonies(item, start);
        }
    }

    public static void resetSIDescs()
    {
        if (DefaultDescriptions.isEmpty()) {
            InitBaseSIDescMap();
            return;
        }
        log.info("Resetting item descriptions");
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
