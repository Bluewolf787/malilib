package malilib.gui.widget;

import malilib.gui.util.BackgroundSettings;
import malilib.gui.util.BorderSettings;
import malilib.gui.util.ScreenContext;
import malilib.render.ShapeRenderUtils;
import malilib.util.data.EdgeInt;

public abstract class BaseModelWidget extends InteractableWidget
{
    protected int dimensions;
    protected int highlightColor;
    protected float scale = 1f;
    protected boolean doHighlight;

    public BaseModelWidget(int dimensions)
    {
        super(dimensions, dimensions);

        this.setDimensions(dimensions);
    }

    public BaseModelWidget setDoHighlight(boolean doHighlight)
    {
        this.doHighlight = doHighlight;
        return this;
    }

    public BaseModelWidget setHighlightColor(int color)
    {
        this.highlightColor = color;
        return this;
    }

    public BaseModelWidget setDimensions(int dimensions)
    {
        this.dimensions = dimensions;

        if (dimensions > 0)
        {
            this.scale = (float) dimensions / 16.0f;
        }

        return this;
    }

    public BaseModelWidget setScale(float scale)
    {
        this.scale = scale;
        return this;
    }

    @Override
    protected int getRequestedContentWidth()
    {
        return this.dimensions;
    }

    @Override
    protected int getRequestedContentHeight()
    {
        return this.dimensions;
    }

    protected abstract void renderModel(int x, int y, float z, float scale, ScreenContext ctx);

    @Override
    public void renderAt(int x, int y, float z, ScreenContext ctx)
    {
        super.renderAt(x, y, z, ctx);

        int width = this.getWidth();
        int height = this.getHeight();
        BackgroundSettings settings = this.getBackgroundRenderer().getActiveSettings(false);

        if (settings.isEnabled())
        {
            BorderSettings borderSettings = this.getBorderRenderer().getActiveSettings(false);
            int bw = borderSettings.getActiveBorderWidth();
            EdgeInt padding = this.padding;
            x += padding.getLeft() + bw;
            y += padding.getTop() + bw;
        }

        if (this.doHighlight && this.isHoveredForRender(ctx))
        {
            ShapeRenderUtils.renderRectangle(x, y, z, width, height, this.highlightColor, ctx);
        }

        this.renderModel(x, y, z + 0.5f, this.scale, ctx);
    }
}
