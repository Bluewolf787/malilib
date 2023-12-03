package malilib.overlay.widget;

import javax.annotation.Nullable;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import malilib.gui.util.GeometryResizeNotifier;
import malilib.gui.widget.ContainerWidget;
import malilib.listener.EventListener;
import malilib.util.data.MarkerManager;
import malilib.util.data.json.JsonUtils;

public abstract class BaseOverlayWidget extends ContainerWidget
{
    protected final MarkerManager<String> markerManager = new MarkerManager<>(JsonPrimitive::new, JsonElement::getAsString);
    protected final GeometryResizeNotifier geometryResizeNotifier;
    @Nullable protected EventListener enabledChangeListener;
    protected boolean forceNotifyGeometryChangeListener;
    protected boolean needsReLayout;

    public BaseOverlayWidget()
    {
        super(0, 0, 0, 0);

        this.geometryResizeNotifier = new GeometryResizeNotifier(this::getWidth, this::getHeight);
        this.margin.setChangeListener(this::requestUnconditionalReLayout);
        this.padding.setChangeListener(this::requestUnconditionalReLayout);
    }

    /**
     * Returns a unique id for the widget type. The id is used in the registry
     * to register the widget factories, and it is also saved to the config file when
     * serializing and de-serializing widgets.
     */
    public abstract String getWidgetTypeId();

    /**
     * Sets a listener that should be notified if the dimensions of this widget get changed,
     * such as the widget height or width changing due to changes in the displayed contents.
     */
    public void setGeometryChangeListener(@Nullable EventListener listener)
    {
        this.geometryResizeNotifier.setGeometryChangeListener(listener);
    }

    /**
     * Sets a listener that should be notified if the dimensions of this widget get changed,
     * such as the widget height or width changing due to changes in the displayed contents.
     */
    public void setEnabledChangeListener(@Nullable EventListener listener)
    {
        this.enabledChangeListener = listener;
    }

    public MarkerManager<String> getMarkerManager()
    {
        return this.markerManager;
    }

    @Override
    protected void onEnabledStateChanged(boolean isEnabled)
    {
        if (this.enabledChangeListener != null)
        {
            this.enabledChangeListener.onEvent();
        }
    }

    protected void requestConditionalReLayout()
    {
        this.needsReLayout = true;
    }

    protected void requestUnconditionalReLayout()
    {
        this.needsReLayout = true;
        this.forceNotifyGeometryChangeListener = true;
    }

    protected void reLayoutWidgets(boolean forceNotify)
    {
        this.updateSize();
        this.updateSubWidgetPositions();
        this.geometryResizeNotifier.checkAndNotifyContainerOfChanges(forceNotify);

        this.needsReLayout = false;
        this.forceNotifyGeometryChangeListener = false;
    }

    /**
     * Called to allow the widget to update its state before all the enabled widgets are rendered.
     */
    public void updateState()
    {
        if (this.needsReLayout)
        {
            this.reLayoutWidgets(this.forceNotifyGeometryChangeListener);
        }

        this.geometryResizeNotifier.updateState();
    }

    @Override
    protected void onPositionChanged(int oldX, int oldY)
    {
        this.updateSubWidgetPositions();
    }

    public JsonObject toJson()
    {
        JsonObject obj = new JsonObject();

        obj.addProperty("type", this.getWidgetTypeId());
        obj.addProperty("enabled", this.isEnabled());
        obj.addProperty("width", this.getWidth());
        JsonUtils.addIfNotEqual(obj, "auto_width", this.automaticWidth, false);

        if (this.hasMaxWidth())
        {
            obj.addProperty("max_width", this.maxWidth);
        }

        this.margin.writeToJsonIfModified(obj, "margin");
        this.padding.writeToJsonIfModified(obj, "padding");

        return obj;
    }

    public void fromJson(JsonObject obj)
    {
        this.enabled = JsonUtils.getBooleanOrDefault(obj, "enabled", true);
        JsonUtils.getBooleanIfExists(obj, "auto_width", v -> this.automaticWidth = v);
        JsonUtils.getIntegerIfExists(obj, "max_width", this::setMaxWidth);
        JsonUtils.getIntegerIfExists(obj, "width", this::setWidth);
        JsonUtils.getArrayIfExists(obj, "padding", this.padding::fromJson);
        JsonUtils.getArrayIfExists(obj, "margin", this.margin::fromJson);
    }
}
