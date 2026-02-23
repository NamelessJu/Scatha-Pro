package namelessju.scathapro.gui.menus.screens.settings;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.widgets.sliders.FloatSlider;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import namelessju.scathapro.sounds.instances.VolumePreviewSound;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class SoundSettingsScreen extends ConfigScreen
{
    private CycleButton<Boolean> keepDragonLairSoundsButton;
    private final VolumePreviewSound previewSound;
    
    public SoundSettingsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Sound Settings", parentScreen);
        
        previewSound = new VolumePreviewSound(scathaPro);
    }
    
    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();
        
        GridBuilder gridBuilder = new GridBuilder();
        
        gridBuilder.addFullWidth(floatConfigSlider(
                scathaPro.getModDisplayName() + " Sounds Volume", 0f, 1f, scathaPro.config.sounds.volume,
                value -> {
                    scathaPro.config.sounds.volume.set(value);
                    playVolumePreviewSound();
                }
            ))
            .setStepSize(0.01f)
            .setClickListener(value -> playVolumePreviewSound())
            .setValueComponentSupplier(FloatSlider.PERCENTAGE_COMPONENT_SUPPLIER_WITH_OFF);
        
        gridBuilder.addGap();
        
        gridBuilder.addFullWidth(
            booleanConfigButton("Mute Crystal Hollows Sounds",
                scathaPro.config.sounds.muteCrystalHollowsSounds, null,
                (button, value) -> updateKeepDragonLairSoundsButton())
        );
        gridBuilder.addFullWidth(
            keepDragonLairSoundsButton = booleanConfigButton("Keep Golden Dragon's Lair Sounds",
                scathaPro.config.sounds.keepDragonLairSounds)
        );
        updateKeepDragonLairSoundsButton();
        
        gridBuilder.addToContent(layout);
        
        addDoneButtonFooter();
    }
    
    private void playVolumePreviewSound()
    {
        if (scathaPro.soundManager.isPlaying(previewSound)) return;
        scathaPro.soundManager.play(previewSound);
    }
    
    private void updateKeepDragonLairSoundsButton()
    {
        if (scathaPro.config.sounds.muteCrystalHollowsSounds.get())
        {
            keepDragonLairSoundsButton.active = true;
            keepDragonLairSoundsButton.setTooltip(null);
        }
        else
        {
            keepDragonLairSoundsButton.active = false;
            keepDragonLairSoundsButton.setTooltip(Tooltip.create(
                Component.literal("Applies only when Crystal\nHollows sounds are muted")
                    .withStyle(ChatFormatting.YELLOW)
            ));
        }
    }
    
    @Override
    public void removed()
    {
        super.removed();
        
        scathaPro.soundManager.stop(previewSound);
    }
    
    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}
