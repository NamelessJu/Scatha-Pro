package com.namelessju.scathapro.miscellaneous;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.MultiOptionButton;

public enum SecondaryKillCounterType implements MultiOptionButton.IOption<String>
{
    PER_LOBBY("Lobby", "lobby")
    {
        @Override
        public int getRegularWormKills()
        {
            return ScathaPro.getInstance().variables.lobbyRegularWormKills;
        }
        
        @Override
        public int getScathaKills()
        {
            return ScathaPro.getInstance().variables.lobbyScathaKills;
        }
    },
    
    PER_SESSION("Session", "session")
    {
        @Override
        public int getRegularWormKills()
        {
            return ScathaPro.getInstance().variables.sessionRegularWormKills;
        }
        
        @Override
        public int getScathaKills()
        {
            return ScathaPro.getInstance().variables.sessionScathaKills;
        }
    };
    
    
    private String displayString;
    private String id;
    
    SecondaryKillCounterType(String displayString, String id)
    {
        this.displayString = displayString;
        this.id = id;
    }
    
    public abstract int getRegularWormKills();
    
    public abstract int getScathaKills();
    
    @Override
    public String getName()
    {
        return displayString;
    }

    @Override
    public String getValue()
    {
        return id;
    }
}