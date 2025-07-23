package namelessju.scathapro.miscellaneous;

import namelessju.scathapro.miscellaneous.enums.Rarity;

public class PetDrop
{
    public final Rarity rarity;
    public final long dropTime;
    
    public PetDrop(Rarity rarity, long dropTime)
    {
        this.rarity = rarity;
        this.dropTime = dropTime;
    }
}
