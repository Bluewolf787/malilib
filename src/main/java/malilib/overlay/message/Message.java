package malilib.overlay.message;

import malilib.gui.util.ScreenContext;
import malilib.render.text.StyledText;
import malilib.render.text.StyledTextLine;
import malilib.render.text.StyledTextUtils;
import malilib.render.text.TextRenderer;
import malilib.util.data.FloatUnaryOperator;

public class Message
{
    public static final int INFO = 0xFFFFFFFF;
    public static final int SUCCESS = 0xFF55FF55;
    public static final int WARNING = 0xFFFFAA00;
    public static final int ERROR = 0xFFFF5555;

    protected final StyledText message;
    protected final int defaultTextColor;
    protected final int width;
    protected final long expireTime;
    protected final long fadeOutDuration;
    protected final long fadeOutTime;

    public Message(StyledText text, int defaultTextColor, int displayTimeMs, int fadeTimeMs, int maxLineWidth)
    {
        this.defaultTextColor = defaultTextColor;
        this.expireTime = System.nanoTime() + (long) displayTimeMs * 1000000L;
        this.fadeOutDuration = Math.min((long) fadeTimeMs * 1000000L, (long) displayTimeMs * 1000000L / 2L);
        this.fadeOutTime = this.expireTime - this.fadeOutDuration;

        this.message = StyledTextUtils.wrapStyledTextToMaxWidth(text, maxLineWidth);
        this.width = StyledTextLine.getRenderWidth(this.message.lines);
    }

    public boolean hasExpired(long currentTime)
    {
        return currentTime >= this.expireTime;
    }

    protected boolean isFading(long currentTime)
    {
        return currentTime >= this.fadeOutTime;
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getLineCount()
    {
        return this.message.lines.size();
    }

    /**
     * Renders the styled text for this message
     */
    public void renderAt(int x, int y, float z, int lineHeight, long currentTime, ScreenContext ctx)
    {
        FloatUnaryOperator alphaModifier = null;

        if (this.isFading(currentTime))
        {
            int alphaInt = (this.defaultTextColor & 0xFF000000) >>> 24;
            double fadeProgress = 1.0 - (double) (currentTime - this.fadeOutTime) / (double) this.fadeOutDuration;
            final float alpha = (float) alphaInt * (float) fadeProgress / 255.0f;
            alphaModifier = (old) -> old * alpha;
        }

        TextRenderer.INSTANCE.renderText(x, y, z, this.defaultTextColor, true, lineHeight,
                                         this.message, alphaModifier, ctx);
    }
}
