package namelessju.scathapro.util;

import namelessju.scathapro.ScathaPro;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class SkyblockItemUtil
{
    public static final String KEY_ID = "id";
    public static final String KEY_ENCHANTMENTS = "enchantments";
    public static final String KEY_GEMS = "gems";
    public static final String KEY_PETINFO = "petInfo";
    
    public static @Nullable CompoundTag getData(@Nullable ItemStack itemStack)
    {
        if (itemStack == null) return null;
        CustomData data = itemStack.getComponents().get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        return data.copyTag();
    }
    
    public static void getData(@Nullable ItemStack itemStack, @NonNull Consumer<CompoundTag> consumer)
    {
        CompoundTag data = getData(itemStack);
        if (data != null) consumer.accept(data);
    }
    
    public static @Nullable String getItemID(@Nullable ItemStack itemStack)
    {
        CompoundTag data = getData(itemStack);
        if (data == null) return null;
        return data.getString(KEY_ID).orElse(null);
    }
    
    public static int getTunnelVisionCooldown(@Nullable ItemStack stack)
    {
        if (stack == null) return -1;
        
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) return -1;
        
        boolean cooldownLineExpectedNow = false;
        for (int i = 0; i < lore.lines().size();)
        {
            Component component = lore.lines().get(i);
            String content = StringDecomposer.getPlainText(component);
            if (content.startsWith("Ability: Tunnel Vision"))
            {
                if (i + 3 < lore.lines().size())
                {
                    i += 3;
                    cooldownLineExpectedNow = true;
                    continue;
                }
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
                ScathaPro.LOGGER.error("Failed to parse Tunnel Vision cooldown number from \"{}\"", content);
                break;
            }
            else if (cooldownLineExpectedNow)
            {
                cooldownLineExpectedNow = false;
                i -= 2;
                continue;
            }
            
            i ++;
        }
        
        return -1;
    }
    
    
    private SkyblockItemUtil() {}
}
