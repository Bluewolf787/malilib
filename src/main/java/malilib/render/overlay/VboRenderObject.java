package malilib.render.overlay;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;

import malilib.listener.EventListener;
import malilib.render.buffer.VertexBuilder;

public class VboRenderObject extends BaseRenderObject
{
    protected final VertexBuffer vertexBuffer;
    protected final EventListener arrayPointerSetter;

    public VboRenderObject(int glMode, VertexFormat vertexFormat, EventListener arrayPointerSetter)
    {
        super(glMode, vertexFormat);

        this.vertexBuffer = new VertexBuffer(vertexFormat);
        this.arrayPointerSetter = arrayPointerSetter;
    }

    @Override
    public void uploadData(VertexBuilder builder)
    {
        builder.finishDrawing();
        builder.reset();
        this.vertexBuffer.bufferData(builder.getByteBuffer());
    }

    @Override
    public void draw()
    {
        if (this.hasTexture)
        {
            GlStateManager.enableTexture2D();

            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        }
        else
        {
            GlStateManager.disableTexture2D();
        }

        this.vertexBuffer.bindBuffer();
        this.arrayPointerSetter.onEvent();
        this.vertexBuffer.drawArrays(this.getGlMode());

        if (this.hasTexture)
        {
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        }
    }

    @Override
    public void deleteGlResources()
    {
        this.vertexBuffer.deleteGlBuffers();
    }

    public static void setupArrayPointersPosColor()
    {
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 16, 0);
        GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 16, 12);
    }

    public static void setupArrayPointersPosUvColor()
    {
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 24, 0);
        GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, 24, 12);
        GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 24, 20);
    }
}
