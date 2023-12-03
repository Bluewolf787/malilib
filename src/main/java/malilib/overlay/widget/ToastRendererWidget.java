package malilib.overlay.widget;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nullable;
import com.google.common.collect.Queues;
import com.google.gson.JsonObject;

import malilib.MaLiLibReference;
import malilib.config.value.HorizontalAlignment;
import malilib.config.value.ScreenLocation;
import malilib.gui.BaseScreen;
import malilib.gui.edit.overlay.ToastRendererWidgetEditScreen;
import malilib.gui.util.ScreenContext;
import malilib.gui.widget.list.entry.BaseInfoRendererWidgetEntryWidget;
import malilib.overlay.message.MessageDispatcher;
import malilib.overlay.widget.sub.ToastWidget;
import malilib.render.text.StyledText;
import malilib.render.text.StyledTextLine;
import malilib.util.StringUtils;
import malilib.util.data.json.JsonUtils;

public class ToastRendererWidget extends InfoRendererWidget
{
    protected final List<ToastWidget> activeToasts = new ArrayList<>();
    protected final Deque<ToastWidget> toastQueue = Queues.newArrayDeque();
    protected int defaultLifeTime = 5000;
    protected int defaultFadeInTime = 200;
    protected int defaultFadeOutTime = 200;
    protected int maxToasts = 5;
    protected int messageGap = 4;

    public ToastRendererWidget()
    {
        super();

        this.shouldSerialize = true;
        this.renderAboveScreen = true;
        this.setName(StringUtils.translate("malilib.label.misc.default_toast_renderer"));
        this.setMaxWidth(240);
        this.padding.setAll(6, 10, 6, 10);
    }

    @Override
    public String getWidgetTypeId()
    {
        return MaLiLibReference.MOD_ID + ":toast_renderer";
    }

    @Override
    public boolean isFixedPosition()
    {
        return true;
    }

    public int getMessageGap()
    {
        return this.messageGap;
    }

    public void setMessageGap(int messageGap)
    {
        this.messageGap = messageGap;
    }

    public int getDefaultLifeTime()
    {
        return this.defaultLifeTime;
    }

    public void setDefaultLifeTime(int defaultLifeTime)
    {
        this.defaultLifeTime = defaultLifeTime;
    }

    public int getDefaultFadeInTime()
    {
        return this.defaultFadeInTime;
    }

    public void setDefaultFadeInTime(int defaultFadeInTime)
    {
        this.defaultFadeInTime = defaultFadeInTime;
    }

    public int getDefaultFadeOutTime()
    {
        return this.defaultFadeOutTime;
    }

    public void setDefaultFadeOutTime(int defaultFadeOutTime)
    {
        this.defaultFadeOutTime = defaultFadeOutTime;
    }

    public int getMaxToasts()
    {
        return this.maxToasts;
    }

    public void setMaxToasts(int maxToasts)
    {
        this.maxToasts = maxToasts;
    }

    public void addToast(String translatedMessage, MessageDispatcher messageDispatcher)
    {
        StyledText text = StyledText.parse(translatedMessage);
        this.addToast(text, messageDispatcher);
    }

    public void addToast(StyledText text, MessageDispatcher messageDispatcher)
    {
        int displayTimeMs = messageDispatcher.getDisplayTimeMs();
        int fadeInTimeMs = messageDispatcher.getFadeInTimeMs();
        int fadeOutTimeMs = messageDispatcher.getFadeOutTimeMs();
        boolean append = messageDispatcher.getAppend();
        String messageMarker = messageDispatcher.getMessageMarker();

        this.addToast(text, displayTimeMs, fadeInTimeMs, fadeOutTimeMs, messageMarker, append);
    }

    /**
     * @param fadeInTimeMs the fade in time (or rather slide in time) in milliseconds
     * @param fadeOutTimeMs the fade out time (or rather slide out time) in milliseconds
     * @param marker the widget marker, if any. If a marker is set, then the same existing
     *               toast widget can be used for other future messages as well, by using the same marker.
     * @param append if true, and a marker was used and a matching existing toast widget was found,
     *               then the text will be appended to the old toast. If false, then the text
     *               will replace the existing text in the existing toast widget.
     *               Has no effect/meaning if the marker is not used to target an existing toast.
     */
    public void addToast(String message, int displayTimeMs, int fadeInTimeMs, int fadeOutTimeMs,
                         @Nullable String marker, boolean append)
    {
        StyledText text = StyledText.parse(message);
        this.addToast(text, displayTimeMs, fadeInTimeMs, fadeOutTimeMs, marker, append);
    }

    /**
     * @param fadeInTimeMs the fade in time (or rather slide in time) in milliseconds
     * @param fadeOutTimeMs the fade out time (or rather slide out time) in milliseconds
     * @param marker the widget marker, if any. If a marker is set, then the same existing
     *               toast widget can be used for other future messages as well, by using the same marker.
     * @param append if true, and a marker was used and a matching existing toast widget was found,
     *               then the text will be appended to the old toast. If false, then the text
     *               will replace the existing text in the existing toast widget.
     *               Has no effect/meaning if the marker is not used to target an existing toast.
     */
    public void addToast(StyledText text, int displayTimeMs, int fadeInTimeMs, int fadeOutTimeMs,
                         @Nullable String marker, boolean append)
    {
        if (this.tryAppendTextToExistingToasts(text, displayTimeMs, marker, append))
        {
            return;
        }

        ToastWidget widget = new ToastWidget(this.getMaxWidth(), this.getLineHeight(), this.messageGap,
                                             this.padding, fadeInTimeMs, fadeOutTimeMs,
                                             this.getScreenLocation().horizontalLocation);
        widget.setZ(this.getZ() + 1f);
        widget.getTextSettings().setFrom(this.getTextSettings());
        widget.addText(text, displayTimeMs);

        if (marker != null)
        {
            widget.getMarkerManager().addMarker(marker);
        }

        this.toastQueue.add(widget);
    }

    protected boolean tryAppendTextToExistingToasts(StyledText text, int displayTimeMs,
                                                    @Nullable String marker, boolean append)
    {
        if (marker != null)
        {
            List<ToastWidget> list = new ArrayList<>(this.activeToasts);
            list.addAll(this.toastQueue);

            for (ToastWidget toast : list)
            {
                if (this.tryAppendTextToExistingToast(toast, text, displayTimeMs, marker, append))
                {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean tryAppendTextToExistingToast(ToastWidget toast, StyledText text,
                                                   int displayTimeMs, @Nullable String marker, boolean append)
    {
        if (toast.getMarkerManager().matchesMarker(marker))
        {
            if (append == false)
            {
                toast.replaceText(text, displayTimeMs);
                this.updateSizeAndPosition();
                return true;
            }
            else if (this.canAppendToToast(toast))
            {
                toast.addText(text, -1);
                this.updateSizeAndPosition();
                return true;
            }
        }

        return false;
    }

    protected boolean canAppendToToast(ToastWidget toast)
    {
        return toast.getRelativeAge() <= 0.25f;
    }

    @Override
    public void onAdded()
    {
        this.updateSizeAndPosition();
    }

    protected void updateSizeAndPosition()
    {
        this.updateSize();
        this.updateWidgetPosition();
        this.updateSubWidgetPositions();
    }

    @Override
    public void updateWidth()
    {
        int width = 0;

        for (ToastWidget toast : this.activeToasts)
        {
            width = Math.max(width, toast.getWidth());
        }

        this.setWidth(width);
    }

    @Override
    public void updateHeight()
    {
        this.setHeight(this.getTotalToastHeight());
    }

    protected int getTotalToastHeight()
    {
        int height = 0;

        for (ToastWidget toast : this.activeToasts)
        {
            height += toast.getHeight();
        }

        return height;
    }

    @Override
    public void updateSubWidgetPositions()
    {
        super.updateSubWidgetPositions();

        ScreenLocation location = this.getScreenLocation();
        HorizontalAlignment align = location.horizontalLocation;
        int x;
        int y = this.getY() + location.getMarginY(this.margin);
        float z = this.getZ();
        int width = this.viewportWidthSupplier.getAsInt();
        int marginX = location.getMarginX(this.margin);

        for (ToastWidget toast : this.activeToasts)
        {
            x = align.getStartX(toast.getWidth(), width, marginX);
            toast.setPosition(x, y);
            toast.setZLevelBasedOnParent(z);
            y += toast.getHeight();
        }
    }

    protected void addToastsFromQueue()
    {
        if (this.activeToasts.size() < this.maxToasts && this.toastQueue.isEmpty() == false)
        {
            int countToAdd = Math.min(this.maxToasts - this.activeToasts.size(), this.toastQueue.size());
            long currentTime = System.nanoTime();

            for (int i = 0; i < countToAdd; ++i)
            {
                ToastWidget toast = this.toastQueue.remove();
                toast.onBecomeActive(currentTime);
                this.activeToasts.add(toast);
            }

            this.updateSizeAndPosition();
        }
    }

    @Override
    public void openEditScreen()
    {
        BaseScreen.openScreenWithParent(new ToastRendererWidgetEditScreen(this));
    }

    @Override
    public void initListEntryWidget(BaseInfoRendererWidgetEntryWidget widget)
    {
        widget.setCanConfigure(true);
        widget.setCanRemove(true);
        widget.setText(StyledTextLine.translateFirstLine("malilib.hover.toast_renderer.entry_name",
                                                         this.getName(), this.getScreenLocation().getDisplayName()));
    }

    @Override
    public void updateState()
    {
        this.addToastsFromQueue();
        super.updateState();
    }

    @Override
    protected void renderContents(int x, int y, float z, ScreenContext ctx)
    {
        this.drawMessages(ctx);
    }

    public void drawMessages(ScreenContext ctx)
    {
        if (this.activeToasts.isEmpty() == false)
        {
            long currentTime = System.nanoTime();
            int countBefore = this.activeToasts.size();

            for (int i = 0; i < this.activeToasts.size(); ++i)
            {
                ToastWidget toast = this.activeToasts.get(i);

                if (toast.hasExpired(currentTime))
                {
                    this.activeToasts.remove(i);
                    --i;
                }
                else
                {
                    toast.render(currentTime, ctx);
                }
            }

            if (this.activeToasts.size() < countBefore)
            {
                this.updateSizeAndPosition();
            }
        }
    }

    @Override
    public JsonObject toJson()
    {
        JsonObject obj = super.toJson();

        obj.addProperty("max_toasts", this.maxToasts);
        obj.addProperty("message_gap", this.messageGap);
        obj.addProperty("toast_lifetime", this.defaultLifeTime);
        obj.addProperty("toast_fade_in", this.defaultFadeInTime);
        obj.addProperty("toast_fade_out", this.defaultFadeOutTime);

        return obj;
    }

    @Override
    public void fromJson(JsonObject obj)
    {
        super.fromJson(obj);

        this.maxToasts = JsonUtils.getIntegerOrDefault(obj, "max_toasts", this.maxToasts);
        this.messageGap = JsonUtils.getIntegerOrDefault(obj, "message_gap", this.messageGap);
        this.defaultLifeTime = JsonUtils.getIntegerOrDefault(obj, "toast_lifetime", this.defaultLifeTime);
        this.defaultFadeInTime = JsonUtils.getIntegerOrDefault(obj, "toast_fade_in", this.defaultFadeInTime);
        this.defaultFadeOutTime = JsonUtils.getIntegerOrDefault(obj, "toast_fade_out", this.defaultFadeOutTime);
    }
}
