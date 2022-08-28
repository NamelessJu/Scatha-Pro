package com.namelessju.scathapro;

import com.namelessju.scathapro.util.Util;

public enum AlertMode { 
    
    NORMAL(null, "Normal"),
    MEME("meme", "Meme"),
    ANIME("anime", "Anime"),
    STAR_WARS("star_wars", "Star Wars");
    
    public final String id;
    public final String name;
    
    AlertMode(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public int getID() {
        return this.ordinal();
    }
    
    public static AlertMode getByID(int id) {
        AlertMode[] modes = AlertMode.values();
        return id > 0 && id < modes.length ? modes[id] : AlertMode.NORMAL;
    }
    
    public static AlertMode getCurrentMode() {
        return getByID(Config.instance.getInt(Config.Key.mode));
    }

    public static boolean playModeSound(String path) {
        AlertMode mode = getCurrentMode();
        
        if (mode == AlertMode.NORMAL) return false;
        
        Util.playModSoundAtPlayer("alert_modes." + mode.id + "." + path);
        return true;
    }
}
