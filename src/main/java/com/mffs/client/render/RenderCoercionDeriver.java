package com.mffs.client.render;

import com.mffs.ModularForcefieldSystem;
import com.mffs.client.render.model.ModelCoercionDeriver;
import com.mffs.common.TileMFFS;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * @author Calclavia
 */
@SideOnly(Side.CLIENT)
public class RenderCoercionDeriver extends TileEntitySpecialRenderer {

    public static final ResourceLocation TEXTURE_ON = new ResourceLocation(ModularForcefieldSystem.MODID, "textures/models/coercionDeriver_on.png");
    public static final ResourceLocation TEXTURE_OFF = new ResourceLocation(ModularForcefieldSystem.MODID, "textures/models/coercionDeriver_off.png");
    public static final ModelCoercionDeriver MODEL = new ModelCoercionDeriver();

    @Override
    public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f) {
        TileMFFS tileEntity = (TileMFFS) t;
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(tileEntity.isActive() ? TEXTURE_ON : TEXTURE_OFF);
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y + 1.95D, z + 0.5D);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GL11.glScalef(1.3F, 1.3F, 1.3F);

        MODEL.render(tileEntity.animation, 0.0625F);

        GL11.glPopMatrix();
    }
}
