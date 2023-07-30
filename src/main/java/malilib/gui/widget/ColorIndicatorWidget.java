package malilib.gui.widget;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;

import malilib.config.option.ColorConfig;
import malilib.config.option.ConfigOption;
import malilib.gui.BaseScreen;
import malilib.gui.edit.ColorEditorHSVScreen;
import malilib.gui.util.ScreenContext;
import malilib.render.ShapeRenderUtils;
import malilib.render.buffer.VanillaWrappingVertexBuilder;
import malilib.render.buffer.VertexBuilder;
import malilib.util.data.Color4f;

public class ColorIndicatorWidget extends InteractableWidget
{
    protected final IntSupplier valueSupplier;
    protected final IntConsumer valueConsumer;
    @Nullable protected ConfigOption<?> config;

    public ColorIndicatorWidget(int width, int height, int color, IntConsumer consumer)
    {
        this(width, height, () -> color, consumer);
    }

    public ColorIndicatorWidget(int width, int height, ColorConfig config, IntConsumer consumer)
    {
        this(width, height, config::getIntegerValue, consumer);

        this.config = config;
    }

    public ColorIndicatorWidget(int width, int height, IntSupplier valueSupplier, IntConsumer consumer)
    {
        super(width, height);

        this.valueSupplier = valueSupplier;
        this.valueConsumer = consumer;
        String color = Color4f.getHexColorString(valueSupplier.getAsInt());
        this.translateAndAddHoverString("malilib.hover.config.open_color_editor", color);

        this.setClickListener(this::openColorEditorScreenIfConfigNotLocked);
    }

    protected void openColorEditorScreenIfConfigNotLocked()
    {
        if (this.config == null || this.config.isLocked() == false)
        {
            BaseScreen.openPopupScreenWithCurrentScreenAsParent(this.createColorEditorScreen());
        }
    }

    protected BaseScreen createColorEditorScreen()
    {
        int originalColor = this.valueSupplier.getAsInt();
        return new ColorEditorHSVScreen(originalColor, this.valueConsumer);
    }

    @Override
    public void renderAt(int x, int y, float z, ScreenContext ctx)
    {
        int width = this.getWidth();
        int height = this.getHeight();

        VertexBuilder builder = VanillaWrappingVertexBuilder.coloredQuads();
        ShapeRenderUtils.renderRectangle(x    , y    , z, width    , height    , 0xFFFFFFFF, builder);
        ShapeRenderUtils.renderRectangle(x + 1, y + 1, z, width - 2, height - 2, 0xFF000000, builder);
        ShapeRenderUtils.renderRectangle(x + 2, y + 2, z, width - 4, height - 4, 0xFF000000 | this.valueSupplier.getAsInt(), builder);
        builder.draw();
    }
}
