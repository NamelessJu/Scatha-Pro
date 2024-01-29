package com.namelessju.scathapro.miscellaneous;

public class PetDrop
{
    public final Rarity rarity;
    public final long dropTime;
    
    public enum Rarity
    {
        UNKNOWN, RARE, EPIC, LEGENDARY;
    }
    
    public PetDrop(Rarity rarity, long dropTime)
    {
        this.rarity = rarity;
        this.dropTime = dropTime;
    }
}
