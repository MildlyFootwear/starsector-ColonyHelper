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
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.*;


public class MainPlugin extends BaseModPlugin {

    public static Logger log = Global.getLogger(MainPlugin.class);

    public static HashMap<SpecialItemSpecAPI, String> DefaultDescriptions = new HashMap<>();
    public static HashMap<FactionAPI, List<MarketAPI>> factionMarketMap = new HashMap<>();

    static Level logLevel = Level.OFF;

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
        List<MarketAPI> validMarkets = factionMarketMap.get(faction);
        String industriesforitem = item.getParams();
        List<String> marketsUsable = new ArrayList<>();
        if (validMarkets != null) {
            log.debug("Checking through "+validMarkets.size());
            Collections.sort(validMarkets, new SizeSort());
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
                                marketsUsable.add(m.getName() + " ("+m.getStarSystem().getName().replace(" Star System","")+", "+m.getSize()+")");
                                needBreak = true;
                                break;
                            }
                        }
                        if (needBreak)
                            break;
                    }
                }
            }
        }

        String s = item.getDesc()+"\n";
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
