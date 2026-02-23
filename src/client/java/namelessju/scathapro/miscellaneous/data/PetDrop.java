package namelessju.scathapro.miscellaneous.data;

import namelessju.scathapro.miscellaneous.data.enums.Rarity;

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
