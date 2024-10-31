package Shoey.ColonyHelper;


import lunalib.lunaSettings.LunaSettingsListener;

import static Shoey.ColonyHelper.MainPlugin.setLuna;

public class LunaListener implements LunaSettingsListener {

    @Override
    public void settingsChanged(String s) {
        if (s.equals("ShoeyColonyHelper"))
        {
            setLuna();
        }
    }
}
