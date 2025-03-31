package com.namelessju.scathapro.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Session;

public class Util
{
    /**
     * Utility class for hex codes of Minecraft vanilla text colors
     */
    public class Color
    {
        public static final int DARK_RED = 0xFFAA0000;
        public static final int RED = 0xFFFF5555;
        public static final int GOLD = 0xFFFFAA00;
        public static final int YELLOW = 0xFFFFFF55;
        public static final int DARK_GREEN = 0xFF00AA00;
        public static final int GREEN = 0xFF55FF55;
        public static final int AQUA = 0xFF55FFFF;
        public static final int DARK_AQUA = 0xFF00AAAA;
        public static final int DARK_BLUE = 0xFF0000AA;
        public static final int BLUE = 0xFF5555FF;
        public static final int LIGHT_PURPLE = 0xFFFF55FF;
        public static final int DARK_PURPLE = 0xFFAA00AA;
        public static final int WHITE = 0xFFFFFFFF;
        public static final int GRAY = 0xFFAAAAAA;
        public static final int DARK_GRAY = 0xFF555555;
        public static final int BLACK = 0xFF000000;
        
        private Color() {}
    }

    
    public static String getUUIDString(UUID uuid)
    {
        if (uuid != null) return uuid.toString().replace("-", "").toLowerCase();
        return null;
    }
    
    public static UUID getPlayerUUID()
    {
        Session session = Minecraft.getMinecraft().getSession();
        
        if ("true".equalsIgnoreCase(System.getProperty("scathapro.offlineUuid"))) // JVM argument: -Dscathapro.offlineUuid=true
        {
            return Util.getOfflinePlayerUUID(session.getUsername());
        }
        
        try
        {
            return session.getProfile().getId();
        }
        catch (NullPointerException e)
        {
        	e.printStackTrace();
            return null;
        }
    }

    /**
     * Generates a UUID from a player name the same way Minecraft does it when the session is offline
     */
    public static UUID getOfflinePlayerUUID(String username)
    {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes());
    }

    public static String getPlayerUUIDString()
    {
        return getUUIDString(getPlayerUUID());
    }
    
    /**
     * Returns a zero-size AABB at the position of a given entity
     * @param entity
     * @return
     */
    public static AxisAlignedBB getEntityPositionAABB(Entity entity)
    {
        return new AxisAlignedBB(entity.posX, entity.posY, entity.posZ, entity.posX, entity.posY, entity.posZ);
    }
    
    /**
     * The default Entity.getBlockPos() method is fucked up and changes in the middle of a block, hence this method to receive an actual block position
     */
    public static BlockPos entityBlockPos(Entity entity)
    {
        return new BlockPos(
            (int) Math.floor(entity.posX),
            (int) Math.floor(entity.posY),
            (int) Math.floor(entity.posZ)
        );
    }
    
    /**
     * <p>Returns the direction that the player is looking towards:</p>
     * 
     * 0: -Z<br> 
     * 1: +X<br> 
     * 2: +Z<br> 
     * 3: -X
     */
    public static int getDirection(EntityPlayer player)
    {
        int direction = (int) Math.floor(player.rotationYaw / 90 - 1.5f) % 4;
        if (direction < 0) direction += 4;
        return direction;
    }
    
    public static boolean isPlayerListOpened()
    {
        ScoreObjective scoreobjective = Minecraft.getMinecraft().theWorld.getScoreboard().getObjectiveInDisplaySlot(0);
        NetHandlerPlayClient handler = Minecraft.getMinecraft().thePlayer.sendQueue;
        return Minecraft.getMinecraft().gameSettings.keyBindPlayerList.isKeyDown() && (!Minecraft.getMinecraft().isIntegratedServerRunning() || handler.getPlayerInfoMap().size() > 1 || scoreobjective != null);
    }
    
    public static boolean openFileInExplorer(File file)
    {
        if (!file.exists()) return false;
        
        try
        {
            Runtime.getRuntime().exec("explorer.exe " + (file.isFile() ? "/select," : "") + file.getAbsolutePath());
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * @param integer A nullable <code>Integer</code> object
     * @return 0 if <code>integer</code> is <code>null</code>, otherwise returns <code>integer</code>
     */
    public static int intOrZero(Integer integer)
    {
        return integer == null ? 0 : integer;
    }
    
    /**
     * Prepares GL for image rendering
     */
    public static void startImageRendering()
    {
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }
    
    public static void endImageRendering()
    {
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
    }
    
    public static void copyToClipboard(String string)
    {
        copyToClipboard(new StringSelection(string));
    }
    
    public static void copyToClipboard(BufferedImage image)
    {
        copyToClipboard(new ImageTransferable(image));
    }

    private static void copyToClipboard(Transferable transferable)
    {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(transferable, null);
    }
    
    
    private Util() {}
    
    
    private static class ImageTransferable implements Transferable
    {
        private Image image;

        public ImageTransferable(Image image)
        {
            this.image = image;
        }
        
        @Override
        public DataFlavor[] getTransferDataFlavors()
        {
            return new DataFlavor[] {DataFlavor.imageFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
            DataFlavor[] flavors = getTransferDataFlavors();
            for (int i = 0; i < flavors.length; i ++)
            {
                if (flavor.equals(flavors[i])) return true;
            }
            return false;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
        {
            if (flavor.equals(DataFlavor.imageFlavor) && image != null)
            {
                return image;
            }
            else throw new UnsupportedFlavorException(flavor);
        }
    }
}
