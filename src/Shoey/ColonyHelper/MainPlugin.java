package Shoey.ColonyHelper;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.*;

import static Shoey.ColonyHelper.Util.Parsers.canIndustryUseItem;
import static Shoey.ColonyHelper.Util.Parsers.findItemMarketsForFaction;


public class MainPlugin extends BaseModPlugin {

    public static Logger log = Global.getLogger(MainPlugin.class);

    public static HashMap<SpecialItemSpecAPI, String> DefaultDescriptions = new HashMap<>();
    public static HashMap<FactionAPI, List<MarketAPI>> factionMarketMap = new HashMap<>();

    public static int maxFactionsShown = 5;
    public static int maxMarketsShown = 5;
    public static boolean addSystem = true, addSize = true, addIndustry = true;

    static Level logLevel = Level.INFO;

    public static void setLuna()
    {
        maxFactionsShown = LunaSettings.getInt("ShoeyColonyHelper", "maxFactionsShown");
        maxMarketsShown = LunaSettings.getInt("ShoeyColonyHelper", "maxMarketsShown");
        if (LunaSettings.getBoolean("ShoeyColonyHelper", "Debugging"))
            logLevel = Level.DEBUG;
        else
            logLevel = Level.INFO;
        addSystem = LunaSettings.getBoolean("ShoeyColonyHelper", "addSystem");
        addSize = LunaSettings.getBoolean("ShoeyColonyHelper", "addSize");
        addIndustry = LunaSettings.getBoolean("ShoeyColonyHelper", "addIndustry");
        log.setLevel(logLevel);
    }

    public static void genFacMarketMap()
    {
        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy())
        {
            FactionAPI f  = m.getFaction();
            if (m.isPlayerOwned()) {
                f = Global.getSector().getPlayerFaction();
            }
            if (!factionMarketMap.containsKey(f)) {
                factionMarketMap.put(f, new ArrayList<MarketAPI>());
            }
            factionMarketMap.get(f).add(m);
        }
    }

    public static void InitBaseSIDescMap()
    {
        for (SpecialItemSpecAPI item : Global.getSettings().getAllSpecialItemSpecs())
        {
            if (item == null || item.getId() == null)
                continue;
            if (!item.hasTag("pather4") && !item.hasTag("pather8"))
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
        log.debug("Looking for markets for "+item.getName()+" in faction "+faction.getDisplayName()+", "+faction.getId());
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
                s += m.getName();
                String addendum = " (";
                if (addIndustry)
                {
                    for (Industry i : m.getIndustries())
                    {
                        if (i == null || i.getSpecialItem() != null && i.getSpecialItem().getId() != null)
                            continue;

                        if (canIndustryUseItem(i, item)) {
                            addendum += i.getCurrentName() + ", ";
                        }
                    }
                }
                if (addSize) {
                    addendum += m.getSize() + ", ";
                }
                if (addSystem) {
                    addendum += m.getStarSystem().getBaseName() + ", ";
                }
                if (addendum.equals(" ("))
                    s += ".";
                else
                    s += addendum.substring(0, addendum.lastIndexOf(", ")) +").";
            } else {
                s += marketsUsable.size()+" "+factionString+" colonies: ";
                for (int c = 0; c < marketsUsable.size() && c < maxMarketsShown; c++)
                {
                    MarketAPI m = marketsUsable.get(c);
                    s += m.getName();
                    String addendum = " (";
                    if (addIndustry)
                    {
                        for (Industry i : m.getIndustries())
                        {
                            if (i == null || i.getSpecialItem() != null && i.getSpecialItem().getId() != null)
                                continue;

                            if (canIndustryUseItem(i, item)) {
                                addendum += i.getCurrentName() + ", ";
                            }
                        }
                    }
                    if (addSize) {
                        addendum += m.getSize() + ", ";
                    }
                    if (addSystem) {
                        addendum += m.getStarSystem().getBaseName() + ", ";
                    }
                    if (!addendum.equals(" ("))
                        s += addendum.substring(0, addendum.lastIndexOf(", ")) +")";

                    if (c != maxMarketsShown -1) {
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
        for (FactionAPI factionAPI : factionMarketMap.keySet())
        {
            float rel = factionAPI.getRelToPlayer().getRel();
            if (!checked.contains(factionAPI) && rel > iRelate)
            {
                f = factionAPI;
                iRelate = factionAPI.getRelToPlayer().getRel();
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
            for (FactionAPI factionAPI : factionMarketMap.keySet())
            {
                if (factionAPI.getRelToPlayer().getRel() > iRelate)
                {
                    start = factionAPI;
                    iRelate = factionAPI.getRelToPlayer().getRel();
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
