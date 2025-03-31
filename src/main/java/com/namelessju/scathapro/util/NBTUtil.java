package com.namelessju.scathapro.util;

import com.namelessju.scathapro.ScathaPro;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StringUtils;

public class NBTUtil
{
    public static NBTTagCompound getSkyblockTagCompound(ItemStack item)
    {
        return getSkyblockTagCompound(item, null);
    }
    
    public static NBTTagCompound getSkyblockTagCompound(ItemStack item, String path)
    {
        if (item != null && item.hasTagCompound())
        {
            NBTTagCompound currentCompound = item.getSubCompound("ExtraAttributes", false);
            if (currentCompound == null) return null;
            
            if (path != null)
            {
                String[] pathSegments = path.split("/");
                for (int i = 0; i < pathSegments.length; i ++)
                {
                    currentCompound = currentCompound.hasKey(pathSegments[i]) ? currentCompound.getCompoundTag(pathSegments[i]) : null;
                    if (i == pathSegments.length - 1) break;
                    else if (currentCompound == null) return null;
                }
            }
            
            return currentCompound;
        }
        
        return null;
    }
    
    public static String getSkyblockItemID(ItemStack item)
    {
        NBTTagCompound skyblockData = getSkyblockTagCompound(item);
        if (skyblockData != null)
        {
            String id = skyblockData.getString("id");
            if (!id.equals("")) return id;
        }
        return null;
    }

    public static boolean isWormSkull(ItemStack stack)
    {
        return isWormSkull(stack, false);
    }
    
    public static boolean isWormSkull(ItemStack stack, boolean headOnly)
    {
        if (stack != null && stack.getItem() == Items.skull && stack.hasTagCompound())
        {
            NBTTagCompound skullOwner = stack.getTagCompound().getCompoundTag("SkullOwner");
            if (skullOwner != null && skullOwner.getBoolean("hypixelPopulated"))
            {
                NBTTagList textureList = skullOwner.getCompoundTag("Properties").getTagList("textures", 10);
                if (textureList.tagCount() > 0)
                {
                    String textureBase64 = textureList.getCompoundTagAt(0).getString("Value");

                    // Head
                    if (textureBase64.equals("ewogICJ0aW1lc3RhbXAiIDogMTYyMDQ0NTc2NDQ1MSwKICAicHJvZmlsZUlkIiA6ICJmNDY0NTcxNDNkMTU0ZmEwOTkxNjBlNGJmNzI3ZGNiOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWxhcGFnbzA1IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RmMDNhZDk2MDkyZjNmNzg5OTAyNDM2NzA5Y2RmNjlkZTZiNzI3YzEyMWIzYzJkYWVmOWZmYTFjY2FlZDE4NmMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="))
                        return true;
                    
                    // Body
                    if (!headOnly && textureBase64.equals("ewogICJ0aW1lc3RhbXAiIDogMTYyNTA3MjMxNDE2OCwKICAicHJvZmlsZUlkIiA6ICIwNWQ0NTNiZWE0N2Y0MThiOWI2ZDUzODg0MWQxMDY2MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJFY2hvcnJhIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzk2MjQxNjBlYjk5YmRjNjUxZGEzOGRiOTljZDdjMDlmMWRhNjY5ZWQ4MmI5Y2JjMjgyODc0NmU2NTBjNzY1ZGEiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="))
                        return true;
                }
            }
        }
        
        return false;
    }
    
    public static int getAnomalousDesireCooldown(ItemStack stack)
    {
        if (stack == null) return -1;
        
        NBTTagCompound displayTag = stack.getSubCompound("display", false);
        if (displayTag != null && displayTag.hasKey("Lore", 9))
        {
            NBTTagList lore = displayTag.getTagList("Lore", 8);
            boolean cooldownLineExpectedNow = false;
            for (int i = 0; i < lore.tagCount(); i ++)
            {
                String content = StringUtils.stripControlCodes(lore.getStringTagAt(i));
                if (content.startsWith("Ability: Anomalous Desire"))
                {
                    if (i + 3 < lore.tagCount())
                    {
                        i += 2;
                        cooldownLineExpectedNow = true;
                    }
                    continue;
                }
                else if (content.startsWith("Cooldown:"))
                {
                    content = content.substring(10); // remove cooldown text in front
                    content = content.substring(0, content.length() - 1); // remove "s" behind number
                    try
                    {
                        return Integer.parseInt(content);
                    }
                    catch (NumberFormatException ignored) {}
                    ScathaPro.getInstance().logError("Failed to parse Anomalous Desire cooldown number from \"" + content + "\"");
                    break;
                }
                else if (cooldownLineExpectedNow)
                {
                    cooldownLineExpectedNow = false;
                    i -= 3;
                    continue;
                }
            }
        }
        
        return -1;
    }
    
    public static NBTTagList getLore(ItemStack itemStack)
    {
        if (itemStack != null && itemStack.getTagCompound() != null)
        {
            NBTTagCompound displayTagCompound = itemStack.getTagCompound().getCompoundTag("display");
            if (displayTagCompound != null)
            {
                return displayTagCompound.getTagList("Lore", 8);
            }
        }
        
        return null;
    }


    private NBTUtil() {}
}
