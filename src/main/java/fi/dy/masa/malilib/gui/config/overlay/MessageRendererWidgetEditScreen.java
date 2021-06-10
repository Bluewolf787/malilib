package fi.dy.masa.malilib.gui.config.overlay;

import fi.dy.masa.malilib.config.value.OptionListConfigValue;
import fi.dy.masa.malilib.gui.BaseScreen;
import fi.dy.masa.malilib.gui.EdgeIntEditScreen;
import fi.dy.masa.malilib.gui.position.ScreenLocation;
import fi.dy.masa.malilib.gui.util.GuiUtils;
import fi.dy.masa.malilib.gui.widget.BaseTextFieldWidget;
import fi.dy.masa.malilib.gui.widget.ColorIndicatorWidget;
import fi.dy.masa.malilib.gui.widget.DoubleEditWidget;
import fi.dy.masa.malilib.gui.widget.DropDownListWidget;
import fi.dy.masa.malilib.gui.widget.IntegerEditWidget;
import fi.dy.masa.malilib.gui.widget.LabelWidget;
import fi.dy.masa.malilib.gui.widget.button.GenericButton;
import fi.dy.masa.malilib.gui.widget.button.OnOffButton;
import fi.dy.masa.malilib.overlay.InfoOverlay;
import fi.dy.masa.malilib.overlay.InfoWidgetManager;
import fi.dy.masa.malilib.overlay.widget.MessageRendererWidget;
import fi.dy.masa.malilib.render.text.TextRenderSettings;

public class MessageRendererWidgetEditScreen extends BaseScreen
{
    protected final MessageRendererWidget widget;
    protected final DropDownListWidget<ScreenLocation> locationDropdownWidget;
    protected final LabelWidget nameLabelWidget;
    protected final LabelWidget textColorLabelWidget;
    protected final LabelWidget backgroundLabelWidget;
    protected final LabelWidget borderLabelWidget;
    protected final LabelWidget lineHeightLabelWidget;
    protected final LabelWidget textScaleLabelWidget;
    protected final LabelWidget textShadowLabelWidget;
    protected final GenericButton enabledToggleButton;
    protected final GenericButton backgroundEnabledToggleButton;
    protected final GenericButton borderEnabledToggleButton;
    protected final GenericButton marginEditButton;
    protected final GenericButton paddingEditButton;
    protected final GenericButton textShadowToggleButton;
    protected final ColorIndicatorWidget textColorWidget;
    protected final ColorIndicatorWidget backgroundColorWidget;
    protected final ColorIndicatorWidget borderColorWidget;
    protected final IntegerEditWidget lineHeightEditWidget;
    protected final DoubleEditWidget textScaleEditWidget;
    protected final BaseTextFieldWidget nameTextField;

    public MessageRendererWidgetEditScreen(MessageRendererWidget widget)
    {
        this.widget = widget;
        this.useTitleHierarchy = false;
        this.setTitle("malilib.gui.title.message_renderer_configuration");

        this.locationDropdownWidget = new DropDownListWidget<>(-1, 16, 160, 10, ScreenLocation.VALUES,
                                                               OptionListConfigValue::getDisplayName);
        this.locationDropdownWidget.setSelectedEntry(widget.getScreenLocation());
        this.locationDropdownWidget.setSelectionListener(this::changeWidgetLocation);

        this.nameLabelWidget = new LabelWidget("malilib.label.name.colon");
        this.lineHeightLabelWidget = new LabelWidget("malilib.label.line_height.colon");
        this.textColorLabelWidget = new LabelWidget("malilib.label.text_color.colon");
        this.textScaleLabelWidget = new LabelWidget("malilib.label.text_scale.colon");
        this.textShadowLabelWidget = new LabelWidget("malilib.label.text_shadow.colon");
        this.backgroundLabelWidget = new LabelWidget("malilib.label.background.colon");
        this.borderLabelWidget = new LabelWidget("malilib.label.border.colon");

        this.nameTextField = new BaseTextFieldWidget(200, 16, widget.getName());
        this.nameTextField.setListener(widget::setName);

        this.lineHeightEditWidget = new IntegerEditWidget(72, 16, widget.getLineHeight(), 6, 40, widget::setLineHeight);
        this.textScaleEditWidget = new DoubleEditWidget(72, 16, widget.getScale(), 0.25, 20, widget::setScale);

        this.marginEditButton = GenericButton.simple(16, "malilib.label.margin", this::openMarginEditScreen);
        this.marginEditButton.setHoverStringProvider("tooltip", this.widget.getMargin()::getHoverTooltip);

        this.paddingEditButton = GenericButton.simple(16, "malilib.label.padding", this::openPaddingEditScreen);
        this.paddingEditButton.setHoverStringProvider("tooltip", this.widget.getPadding()::getHoverTooltip);

        this.enabledToggleButton = OnOffButton.simpleSlider(16, widget::isEnabled, widget::toggleEnabled);

        final TextRenderSettings textSettings = widget.getTextSettings();

        this.backgroundEnabledToggleButton = OnOffButton.simpleSlider(16, textSettings::getUseBackground, textSettings::toggleUseBackground);
        this.borderEnabledToggleButton = OnOffButton.simpleSlider(16, textSettings::getUseBackground, textSettings::toggleUseBackground); // FIXME

        this.textShadowToggleButton = OnOffButton.simpleSlider(16, textSettings::getUseTextShadow, textSettings::toggleUseTextShadow);

        this.textColorWidget = new ColorIndicatorWidget(16, 16, textSettings::getTextColor, textSettings::setTextColor);
        this.backgroundColorWidget = new ColorIndicatorWidget(16, 16, textSettings::getBackgroundColor, textSettings::setBackgroundColor);
        this.borderColorWidget = new ColorIndicatorWidget(16, 16, textSettings::getBackgroundColor, textSettings::setBackgroundColor); // FIXME
    }

    @Override
    protected void reAddActiveWidgets()
    {
        super.reAddActiveWidgets();

        this.addWidget(this.locationDropdownWidget);
        this.addWidget(this.enabledToggleButton);
        this.addWidget(this.marginEditButton);
        this.addWidget(this.paddingEditButton);

        this.addWidget(this.nameLabelWidget);
        this.addWidget(this.nameTextField);

        this.addWidget(this.textScaleLabelWidget);
        this.addWidget(this.textScaleEditWidget);

        this.addWidget(this.lineHeightLabelWidget);
        this.addWidget(this.lineHeightEditWidget);

        this.addWidget(this.textShadowLabelWidget);
        this.addWidget(this.textShadowToggleButton);

        this.addWidget(this.textColorLabelWidget);
        this.addWidget(this.textColorWidget);

        this.addWidget(this.backgroundLabelWidget);
        this.addWidget(this.backgroundColorWidget);
        this.addWidget(this.backgroundEnabledToggleButton);

        this.addWidget(this.borderLabelWidget);
        this.addWidget(this.borderColorWidget);
        this.addWidget(this.borderEnabledToggleButton);
    }

    @Override
    protected void updateWidgetPositions()
    {
        super.updateWidgetPositions();

        int x = this.x + 10;
        int y = this.y + 24;
        int tmpX;

        this.marginEditButton.updateHoverStrings();
        this.paddingEditButton.updateHoverStrings();

        this.locationDropdownWidget.setPosition(x, y);
        this.enabledToggleButton.setPosition(this.locationDropdownWidget.getRight() + 6, y);

        this.marginEditButton.setPosition(this.enabledToggleButton.getRight() + 6, y);
        this.paddingEditButton.setPosition(this.marginEditButton.getRight() + 6, y);

        y += 20;
        this.nameLabelWidget.setPosition(x, y + 4);
        this.nameTextField.setPosition(this.nameLabelWidget.getRight() + 6, y);

        y += 20;
        this.textScaleLabelWidget.setPosition(x, y + 4);
        this.lineHeightLabelWidget.setPosition(x, y + 24);
        this.textShadowLabelWidget.setPosition(x, y + 44);

        tmpX = Math.max(this.textScaleLabelWidget.getRight(), this.lineHeightLabelWidget.getRight()) + 6;
        this.textScaleEditWidget.setPosition(tmpX, y);
        this.lineHeightEditWidget.setPosition(tmpX, y + 20);

        tmpX = this.textShadowLabelWidget.getRight() + 6;
        this.textShadowToggleButton.setPosition(tmpX, y + 40);

        tmpX = this.lineHeightEditWidget.getRight() + 6;
        this.textColorLabelWidget.setPosition(tmpX, y + 4);
        this.backgroundLabelWidget.setPosition(tmpX, y + 24);
        this.borderLabelWidget.setPosition(tmpX, y + 44);

        tmpX = Math.max(this.textColorLabelWidget.getRight(), this.backgroundLabelWidget.getRight());
        tmpX = Math.max(tmpX, this.borderLabelWidget.getRight()) + 6;
        this.textColorWidget.setPosition(tmpX, y);
        this.backgroundColorWidget.setPosition(tmpX, y + 20);
        this.borderColorWidget.setPosition(tmpX, y + 40);

        tmpX += 22;
        this.backgroundEnabledToggleButton.setPosition(tmpX, y + 20);
        this.borderEnabledToggleButton.setPosition(tmpX, y + 40);
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();

        InfoWidgetManager.INSTANCE.saveToFile();
    }

    protected void changeWidgetLocation(ScreenLocation location)
    {
        InfoOverlay.INSTANCE.getOrCreateInfoArea(this.widget.getScreenLocation()).removeWidget(this.widget);
        // This also sets the location in the widget
        InfoOverlay.INSTANCE.getOrCreateInfoArea(location).addWidget(this.widget);
    }

    protected void openMarginEditScreen()
    {
        EdgeIntEditScreen screen = new EdgeIntEditScreen(this.widget.getMargin(), false,
                                                         "malilib.gui.title.edit_margin", "malilib.label.margin");
        screen.setParent(GuiUtils.getCurrentScreen());
        BaseScreen.openScreen(screen);
    }

    protected void openPaddingEditScreen()
    {
        EdgeIntEditScreen screen = new EdgeIntEditScreen(this.widget.getPadding(), false,
                                                         "malilib.gui.title.edit_padding", "malilib.label.padding");
        screen.setParent(GuiUtils.getCurrentScreen());
        BaseScreen.openScreen(screen);
    }
}
