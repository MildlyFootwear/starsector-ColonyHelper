package Shoey.ColonyHelper;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.*;

import static Shoey.ColonyHelper.Util.MarketParsers.findItemMarketsForFaction;


public class MainPlugin extends BaseModPlugin {

    public static Logger log = Global.getLogger(MainPlugin.class);

    public static HashMap<SpecialItemSpecAPI, String> DefaultDescriptions = new HashMap<>();
    public static HashMap<FactionAPI, List<MarketAPI>> factionMarketMap = new HashMap<>();

    public static int maxFactionsShown = 5;
    public static int maxMarketsShown = 5;

    static Level logLevel = Level.INFO;

    public static void setLuna()
    {
        maxFactionsShown = LunaSettings.getInt("ShoeyColonyHelper", "maxFactionsShown");
        maxMarketsShown = LunaSettings.getInt("ShoeyColonyHelper", "maxMarketsShown");
        if (LunaSettings.getBoolean("ShoeyColonyHelper", "Debugging"))
            logLevel = Level.DEBUG;
        else
            logLevel = Level.INFO;
        log.setLevel(logLevel);
    }

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
        setDescWithColonies(item, faction, recursive, null, 0);
    }

    public static void setDescWithColonies(SpecialItemSpecAPI item, FactionAPI faction, boolean recursive, List<FactionAPI> alreadyChecked, int numFactionsShown)
    {
        log.info("Looking for markets for "+item.getName()+" in faction "+faction.getDisplayName()+", "+faction.getId());
        List<MarketAPI> marketsUsable = findItemMarketsForFaction(item, faction);

        String s = item.getDesc();
        int newShown = numFactionsShown;
        if (!marketsUsable.isEmpty())
        {
            newShown++;
            if (newShown == 1)
                s += "\n\nUseful for;";
            s += "\n    ";
            String factionString = "";
            if (faction == Global.getSector().getPlayerFaction())
                factionString = "Player faction";
            else
                factionString = faction.getDisplayName();
            if (marketsUsable.size() == 1)
            {
                MarketAPI m = marketsUsable.get(0);
                s += "1 "+factionString+" colony: ";
                s += m.getName() + " (" + m.getStarSystem().getBaseName() + ", " + m.getSize()+").";
            } else {
                s += marketsUsable.size()+" "+factionString+" colonies: ";
                for (int i = 0; i < marketsUsable.size() && i < maxMarketsShown; i++)
                {
                    MarketAPI m = marketsUsable.get(i);
                    s += m.getName() + " (" + m.getStarSystem().getBaseName() + ", " + m.getSize()+")";
                    if (i != maxMarketsShown -1) {
                        s += ", ";
                    } else {
                        if (marketsUsable.size() - maxMarketsShown > 0) {
                            s += " and " + (marketsUsable.size() - maxMarketsShown) + " more.";
                        } else {
                            s += ".";
                        }
                    }
                }
            }
            item.setDesc(s);
//            return;
        } else {
            log.debug("Found no markets for "+item.getName()+" in faction "+faction.getDisplayName());
        }

        if (!recursive)
            return;

        List<FactionAPI> checked = new ArrayList<>();
        if (alreadyChecked != null) {
            checked.addAll(0, alreadyChecked);
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
        if (f != null && !checked.contains(f) && newShown < maxFactionsShown)
        {
            setDescWithColonies(item, f, true, checked, newShown);
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
        log.info("Resetting item descriptions");
        for (SpecialItemSpecAPI item : DefaultDescriptions.keySet())
        {
            item.setDesc(DefaultDescriptions.get(item));
            log.info("Reset description of "+item);
        }
    }

    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();
        setLuna();
        InitBaseSIDescMap();
        LunaSettings.addSettingsListener(new LunaListener());
    }

    @Override
    public void onGameLoad(boolean b) {
        super.onGameLoad(b);
        setLuna();
        Global.getSector().getListenerManager().addListener(new CoreUIListener(), true);
        resetSIDescs();
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
