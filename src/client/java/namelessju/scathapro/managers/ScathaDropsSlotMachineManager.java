package namelessju.scathapro.managers;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.miscellaneous.data.ScathaPetDrop;
import namelessju.scathapro.sounds.instances.ScathaProSound;
import namelessju.scathapro.util.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public class ScathaDropsSlotMachineManager
{
    private static final int MAIN_SLOT_COUNT = 20;
    private static final int START_END_EXTRA_SLOT_COUNT = 5;
    private static final int SLOT_SIZE = 70;
    private static final int SLOT_GAP = 10;
    private static final int Y_OFFSET = 20;

    private static final Identifier BAR_TEXTURE = ScathaPro.getIdentifier("textures/drops_roll/bar.png");


    private final ScathaPro scathaPro;
    private final @Nullable SlotContent[] scrolledBySlots = new SlotContent[MAIN_SLOT_COUNT + START_END_EXTRA_SLOT_COUNT * 2];
    private final ScathaProSound slotClickSound;

    private short preRollTicksLeft = 0;
    private short preRollDelayTicksLeft = 0;
    private int animationTicksLeft = 0;
    private float randomOffset = 0f;
    private int currentSlotIndex = -1;
    private int currentSlotIndexTicks = 0;
    private @Nullable ScathaPetDrop petDrop;
    private boolean hasDroppedBlockBran = false;
    private final List<Component> delayedMessages = new ArrayList<>();

    public ScathaDropsSlotMachineManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        this.slotClickSound = new ScathaProSound(
            scathaPro, Identifier.withDefaultNamespace("ui.button.click"),
            0.8f, 1f
        );
    }

    public void startPreRoll()
    {
        startPreRoll((short) 40, (short) 0);
    }

    /**
     * Starts hiding some things for a short while to prevent spoiling
     * a drop without actually starting the animation yet
     */
    public void startPreRoll(short ticks, short delayTicks)
    {
        preRollTicksLeft = ticks;
        preRollDelayTicksLeft = delayTicks;

        if (shouldHideScreen(scathaPro.minecraft.gui.screen()))
        {
            scathaPro.minecraft.gui.setScreen(null);
        }
    }

    public void startRolling()
    {
        animationTicksLeft = scathaPro.config.miscellaneous.dropsSlotMachineAnimationTicks.get();
        randomOffset = scathaPro.config.miscellaneous.dropsSlotMachineApplyRandomOffset.get()
                        ? Util.random.nextFloat(0.1f, 0.9f)
                        : 0.5f;
        currentSlotIndex = -1;
        currentSlotIndexTicks = 0;

        SlotContent.SCATHA_RARE.setWeight(0);
        SlotContent.SCATHA_EPIC.setWeight(0);
        SlotContent.SCATHA_LEGENDARY.setWeight(0);
        switch (scathaPro.config.miscellaneous.dropsSlotMachineMaxFakeScathaRarity.get())
        {
            case LEGENDARY:
                SlotContent.SCATHA_LEGENDARY.setWeight(1);
            case EPIC:
                SlotContent.SCATHA_EPIC.setWeight(3);
            case RARE:
                SlotContent.SCATHA_RARE.setWeight(6);
            case NONE:
                break;
        }
        for (int i = 0; i < scrolledBySlots.length; i++)
        {
            scrolledBySlots[i] = SlotContent.getRandom();
        }

        if (shouldHideScreen(scathaPro.minecraft.gui.screen()))
        {
            scathaPro.minecraft.gui.setScreen(null);
        }
    }

    public boolean isRolling()
    {
        return animationTicksLeft > 0;
    }

    public boolean shouldHideDrops()
    {
        return (preRollTicksLeft > 0 && preRollDelayTicksLeft <= 0) || animationTicksLeft > 0;
    }

    public void setPetDrop(@Nullable ScathaPetDrop petDrop)
    {
        setPetDrop(petDrop, true);
    }

    public void setPetDrop(@Nullable ScathaPetDrop petDrop, boolean allowOverride)
    {
        if (!allowOverride && this.petDrop != null) return;
        this.petDrop = petDrop;
    }

    public void setHasDroppedBlockBran(boolean value)
    {
        this.hasDroppedBlockBran = value;
    }

    public void addDelayedChatMessage(Component message)
    {
        this.delayedMessages.add(message);
    }

    public void reset()
    {
        preRollTicksLeft = 0;
        preRollDelayTicksLeft = 0;
        animationTicksLeft = 0;
        currentSlotIndex = -1;
        currentSlotIndexTicks = 0;
        hasDroppedBlockBran = false;

        for (Component message : delayedMessages)
        {
            scathaPro.chatManager.sendChatMessage(message, false);
        }
        delayedMessages.clear();

        if (petDrop != null) petDrop.trigger(scathaPro);
        petDrop = null;
    }

    public boolean shouldHideScreen(@Nullable Screen screen)
    {
        if (!shouldHideDrops()) return false;
        return screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen;
    }

    public void tick()
    {
        if (preRollTicksLeft > 0) preRollTicksLeft --;
        if (preRollDelayTicksLeft > 0) preRollDelayTicksLeft --;

        if (animationTicksLeft <= 0) return;

        int animationDuration = scathaPro.config.miscellaneous.dropsSlotMachineAnimationTicks.get();

        animationTicksLeft = (short) (Math.min(animationTicksLeft, animationDuration) - 1);
        if (animationTicksLeft <= 0)
        {
            reset();
            return;
        }

        currentSlotIndexTicks++;

        float scrollProgress = getScrollProgress(
            Mth.clamp((float) (animationDuration - animationTicksLeft) / animationDuration, 0f, 1f)
        );

        int selectedSlotIndex = -1;

        for (int i = 0; i < scrolledBySlots.length; i++)
        {
            if (getSlotRelativeX(i, scrollProgress) < SLOT_GAP / 2f)
            {
                selectedSlotIndex = i;
            }
        }

        if (selectedSlotIndex != currentSlotIndex)
        {
            currentSlotIndex = selectedSlotIndex;
            currentSlotIndexTicks = 0;
            slotClickSound.setPitch(
                0.7f + Mth.lerp(scrollProgress, 0.6f, 0f) + Mth.lerp(Util.random.nextFloat(), -0.1f, 0.1f)
            );
            scathaPro.soundManager.play(slotClickSound);
        }
    }

    public void extractHudRenderState(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker)
    {
        if (animationTicksLeft <= 0) return;

        int guiScale = scathaPro.minecraft.getWindow().getGuiScale();
        int guiMaxScale = scathaPro.minecraft.getWindow().calculateScale(0, false);
        float scale = Math.max(guiMaxScale - Mth.floor(guiMaxScale * 0.334f), 1)
            * scathaPro.config.miscellaneous.dropsSlotMachineScaleMultiplier.get();
        float partialTicks = deltaTracker.getRealtimeDeltaTicks();
        int animationDuration = scathaPro.config.miscellaneous.dropsSlotMachineAnimationTicks.get();
        float ticksPassed = animationDuration - animationTicksLeft - partialTicks;
        float progress = Mth.clamp(ticksPassed / animationDuration, 0f, 1f);
        float scrollProgress = getScrollProgress(progress);
        int alpha = Math.round(
            Math.min(
                Mth.clamp(ticksPassed * (1f/6f), 0f, 1f), // fade in
                petDrop == null ? Mth.clamp(animationTicksLeft * 0.2f, 0f, 1f) : 1f // fade out
            )
            * 255f
        );
        float halfGuiWidth = guiGraphics.guiWidth() / 2f;
        float scaledHalfGuiWidth = (halfGuiWidth*guiScale)/scale;
        float halfGuiHeight = guiGraphics.guiHeight() / 2f;
        float halfSlotSize = SLOT_SIZE / 2f;

        guiGraphics.nextStratum();
        guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), ARGB.black(Math.round(alpha * (0xA0 / 255f))));
        guiGraphics.nextStratum();

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(halfGuiWidth, halfGuiHeight);
        guiGraphics.pose().scale(1f/guiScale);
        guiGraphics.pose().scale(scale);

        Component dropName = null;

        for (int i = 0; i < scrolledBySlots.length; i++)
        {
            float x = getSlotRelativeX(i, scrollProgress);
            if (x + scaledHalfGuiWidth + SLOT_SIZE < 0 || x >= scaledHalfGuiWidth) continue;

            boolean isTargetSlot = i == START_END_EXTRA_SLOT_COUNT + MAIN_SLOT_COUNT - 1;

            SlotContent slot;
            if (isTargetSlot)
            {
                if (petDrop != null) slot = switch (petDrop.rarity()) {
                    case UNKNOWN -> null;
                    case RARE -> SlotContent.SCATHA_RARE;
                    case EPIC -> SlotContent.SCATHA_EPIC;
                    case LEGENDARY -> SlotContent.SCATHA_LEGENDARY;
                };
                else if (hasDroppedBlockBran) slot = SlotContent.BLOCK_BRAN;
                else slot = SlotContent.GEMSTONES;
            }
            else slot = this.scrolledBySlots[i];

            if (i == currentSlotIndex)
            {
                dropName = slot != null ? slot.name : Component.literal("MISSING SLOT").withColor(TextColor.RED);
            }

            if (slot == null)
            {
                guiGraphics.pose().pushMatrix();
                guiGraphics.pose().translate(x + halfSlotSize, Y_OFFSET);
                guiGraphics.pose().scale(3f);
                guiGraphics.centeredText(scathaPro.minecraft.font, "?", 0, -4, ARGB.white(alpha));
                guiGraphics.pose().popMatrix();
                continue;
            }

            float alphaMultiplier = Mth.clamp(1 - (Mth.abs(x + halfSlotSize) / 450f), 0f, 1f);

            guiGraphics.pose().pushMatrix();
            if (i == currentSlotIndex)
            {
                float targetSize = (float) (SLOT_SIZE + SLOT_GAP*2) / SLOT_SIZE;
                float distanceFromCenter = x+halfSlotSize;
                float fadeX = Mth.clampedMap(
                    isTargetSlot ? Math.max(distanceFromCenter, 0f) : Math.abs(distanceFromCenter),
                    halfSlotSize*0.65f, halfSlotSize, 0f, 1f
                );
                float scaleIncreaseT = 1 - (fadeX*fadeX);
                guiGraphics.pose().translate(x + halfSlotSize, Y_OFFSET);
                guiGraphics.pose().scale(Mth.clampedLerp(scaleIncreaseT, 1f, targetSize));
                guiGraphics.pose().translate(-halfSlotSize, -halfSlotSize);
            }
            else guiGraphics.pose().translate(x, -halfSlotSize + Y_OFFSET);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, slot.textureIdentifier,
                0, 0, 0, 0, SLOT_SIZE, SLOT_SIZE,
                slot.textureWidth, slot.textureHeight, slot.textureWidth, slot.textureHeight,
                ARGB.white(Math.round(alpha * alphaMultiplier))
            );
            guiGraphics.pose().popMatrix();
        }

        guiGraphics.nextStratum();

        int barWidth = 16;
        int barHeight = 128;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BAR_TEXTURE,
            -barWidth/2, -barHeight/2 + Y_OFFSET, 0, 0,
            barWidth, barHeight, barWidth, barHeight, barWidth, barHeight,
            ARGB.white(alpha)
        );

        float a = Mth.clamp((currentSlotIndexTicks + partialTicks) / 8f, 0f, 1f) - 1f;
        float textAnimT = -Math.abs(a*a*a) + 1f;
        float textScale = Mth.lerp(textAnimT, 2.2f, 1.5f);
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(0, -halfSlotSize - 30 - scathaPro.minecraft.font.lineHeight * textScale * 0.5f + Y_OFFSET);
        guiGraphics.pose().scale(textScale);
        guiGraphics.centeredText(scathaPro.minecraft.font,
            dropName != null ? dropName : Component.literal("NO SLOT SELECTED").withColor(TextColor.RED),
            0, 0, ARGB.white(Math.round(alpha * Mth.lerp(textAnimT, 0.5f, 1f)))
        );
        guiGraphics.pose().popMatrix();

        guiGraphics.pose().popMatrix();
    }

    private float getSlotRelativeX(int slotIndex, float scrollProgress)
    {
        return (slotIndex - START_END_EXTRA_SLOT_COUNT) * (SLOT_SIZE + SLOT_GAP) // put slots next to each other
            - (MAIN_SLOT_COUNT - 1) * (SLOT_SIZE + SLOT_GAP) * scrollProgress // scroll animation
            - SLOT_SIZE * randomOffset; // offset from start of slot
    }

    private float getScrollProgress(float animationProgress)
    {
        float a = animationProgress - 1f;
        return -(a*a*a*a) + 1f;
    }


    private enum SlotContent
    {
        GEMSTONES(15,
            Component.literal("Gemstones").withColor(TextColor.WHITE),
            ScathaPro.getIdentifier("textures/drops_roll/gemstones.png"), 256, 256
        ),
        BLOCK_BRAN(10,
            Component.literal("Dwarven O's Block Bran").withColor(TextColor.GREEN),
            ScathaPro.getIdentifier("textures/drops_roll/block_bran.png"), 256, 256
        ),
        SCATHA_RARE(0,
            Component.literal("Rare Scatha Pet").withColor(TextColor.BLUE),
            ScathaPro.getIdentifier("textures/generic/scatha_pet_rare.png"), 256, 256
        ),
        SCATHA_EPIC(0,
            Component.literal("Epic Scatha Pet").withColor(TextColor.DARK_PURPLE),
            ScathaPro.getIdentifier("textures/generic/scatha_pet_epic.png"), 256, 256
        ),
        SCATHA_LEGENDARY(0,
            Component.literal("Legendary Scatha Pet").withColor(TextColor.GOLD),
            ScathaPro.getIdentifier("textures/generic/scatha_pet_legendary.png"), 256, 256
        );

        private static int WEIGHT_SUM = -1;

        private static void updateWeights()
        {
            int weightSum = 0;
            for (SlotContent slotContent : SlotContent.values())
            {
                weightSum += Math.max(slotContent.weight, 0);
            }
            WEIGHT_SUM = weightSum;
        }

        private int weight;
        public final Component name;
        public final Identifier textureIdentifier;
        public final int textureWidth;
        public final int textureHeight;

        SlotContent(int weight, Component name, Identifier textureIdentifier, int textureWidth, int textureHeight)
        {
            this.weight = weight;
            this.name = name;
            this.textureIdentifier = textureIdentifier;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
        }

        public void setWeight(int weight)
        {
            this.weight = weight;
            WEIGHT_SUM = -1;
        }

        public static SlotContent getRandom()
        {
            if (WEIGHT_SUM < 0) updateWeights();

            int rngValue = Util.random.nextInt(WEIGHT_SUM);
            for (SlotContent slotContent : SlotContent.values())
            {
                rngValue -= Math.max(slotContent.weight, 0);
                if (rngValue < 0) return slotContent;
            }
            return GEMSTONES; // should never happen but I gotta keep the compiler happy
        }
    }
}