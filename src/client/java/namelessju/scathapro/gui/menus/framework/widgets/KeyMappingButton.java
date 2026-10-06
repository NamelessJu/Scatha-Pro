package namelessju.scathapro.gui.menus.framework.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.NonNull;

import java.lang.ref.WeakReference;

public class KeyMappingButton extends Button.Plain
{
    private static WeakReference<KeyMappingButton> selectedButton = null;

    public static boolean isAnySelected()
    {
        return selectedButton != null && !selectedButton.refersTo(null);
    }

    public static void clearSelection()
    {
        selectedButton = null;
    }

    private final KeyMapping keyMapping;
    private boolean hasCollision = false;

    public KeyMappingButton(int x, int y, int width, int height, KeyMapping keyMapping)
    {
        super(x, y, width, height, Component.empty(), button -> {
            KeyMappingButton mappingButton = (KeyMappingButton) button;

            KeyMappingButton selectedButtonRef = selectedButton != null ? selectedButton.get() : null;
            if (selectedButtonRef == mappingButton)
            {
                selectedButton = null;
            }
            else
            {
                selectedButton = new WeakReference<>(mappingButton);
                if (selectedButtonRef != null) selectedButtonRef.updateMessage();
            }

            mappingButton.updateMessage();

        }, Button.DEFAULT_NARRATION);
        this.keyMapping = keyMapping;
        updateKey();
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick)
    {
        if (isSelected())
        {
            setKey(InputConstants.Type.MOUSE.getOrCreate(event.button()));
            return true;
        }
        else return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event)
    {
        if (!this.isActive()) return false;

        if (isSelected())
        {
            setKey(event.isEscape() ? InputConstants.UNKNOWN : InputConstants.getKey(event));
            return true;
        }

        if (event.isSelection())
        {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress(event);
            return true;
        }

        return false;
    }

    private void setKey(InputConstants.Key key)
    {
        selectedButton = null;
        keyMapping.setKey(key);
        Minecraft.getInstance().options.save();
        updateKey();
    }

    private void updateKey()
    {
        hasCollision = false;
        MutableComponent tooltip = Component.empty();
        if (!keyMapping.isUnbound())
        {
            for (KeyMapping other : Minecraft.getInstance().options.keyMappings)
            {
                if (other != keyMapping && keyMapping.same(other) && (!other.isDefault() || !keyMapping.isDefault()))
                {
                    if (this.hasCollision)
                    {
                        tooltip.append(", ");
                    }

                    this.hasCollision = true;
                    tooltip.append(Component.translatable(other.getName()));
                }
            }
        }

        setTooltip(hasCollision
            ? Tooltip.create(Component.translatable("controls.keybinds.duplicateKeybinds", tooltip))
            : null
        );

        updateMessage();
    }

    private void updateMessage()
    {
        Component keyComponent = keyMapping.getTranslatedKeyMessage();
        if (hasCollision)
        {
            keyComponent = Component.literal("[ ").append(keyComponent.copy().withColor(TextColor.WHITE)).append(" ]").withColor(TextColor.YELLOW);
        }
        if (isSelected())
        {
            keyComponent = Component.literal("> ").append(keyComponent.copy().setStyle(Style.EMPTY.withColor(TextColor.WHITE).withUnderlined(true))).append(" <").withColor(TextColor.YELLOW);
        }
        setMessage(Component.empty()
            .append(Component.translatable(keyMapping.getName()))
            .append(" Key: ")
            .append(keyComponent)
        );
    }

    private boolean isSelected()
    {
        return selectedButton != null && selectedButton.refersTo(this);
    }
}