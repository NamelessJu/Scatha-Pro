package com.namelessju.scathapro.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class ScathaPetDropEvent extends Event {
    
    public enum Rarity {
        UNKNOWN, RARE, EPIC, LEGENDARY;
    }
    
    public final Rarity rarity;
    
    public ScathaPetDropEvent(int rarity) {
        switch (rarity) {
            case 1:
                this.rarity = Rarity.RARE;
                break;
            case 2:
                this.rarity = Rarity.EPIC;
                break;
            case 3:
                this.rarity = Rarity.LEGENDARY;
                break;
            default:
                this.rarity = Rarity.UNKNOWN;
        }
    }
    
}
