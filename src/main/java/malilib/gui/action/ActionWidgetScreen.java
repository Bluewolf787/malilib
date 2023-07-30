package malilib.gui.action;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import org.apache.commons.lang3.StringUtils;

import malilib.MaLiLibConfigs;
import malilib.action.ActionContext;
import malilib.action.ActionExecutionWidgetManager;
import malilib.gui.BaseScreen;
import malilib.gui.TextInputScreen;
import malilib.gui.icon.DefaultIcons;
import malilib.gui.util.GuiUtils;
import malilib.gui.util.ScreenContext;
import malilib.gui.widget.CheckBoxWidget;
import malilib.gui.widget.InfoIconWidget;
import malilib.gui.widget.IntegerEditWidget;
import malilib.gui.widget.LabelWidget;
import malilib.gui.widget.MenuEntryWidget;
import malilib.gui.widget.MenuWidget;
import malilib.gui.widget.button.GenericButton;
import malilib.gui.widget.button.OnOffButton;
import malilib.input.ActionResult;
import malilib.input.KeyBindImpl;
import malilib.input.Keys;
import malilib.overlay.message.MessageDispatcher;
import malilib.render.ShapeRenderUtils;
import malilib.render.text.StyledText;
import malilib.render.text.StyledTextLine;
import malilib.util.data.EdgeInt;
import malilib.util.data.json.JsonUtils;
import malilib.util.position.Vec2i;

public class ActionWidgetScreen extends BaseScreen implements ActionWidgetContainer
{
    protected final ActionWidgetScreenData data;
    protected final List<BaseActionExecutionWidget> widgetList = new ArrayList<>();
    protected final List<BaseActionExecutionWidget> selectedWidgets = new ArrayList<>();
    protected final GenericButton addWidgetButton;
    protected final GenericButton editModeButton;
    protected final GenericButton gridEnabledButton;
    protected final GenericButton exportSettingsButton;
    protected final GenericButton importSettingsButton;
    protected final LabelWidget editModeLabel;
    protected final LabelWidget gridLabel;
    protected final IntegerEditWidget gridEditWidget;
    protected final CheckBoxWidget closeOnExecuteCheckbox;
    protected final CheckBoxWidget closeOnKeyReleaseCheckbox;
    protected final InfoIconWidget infoWidget;
    protected final String name;
    @Nullable protected MenuWidget menuWidget;
    protected Vec2i selectionStart = Vec2i.ZERO;
    protected boolean closeScreenOnExecute;
    protected boolean closeScreenOnKeyRelease;
    protected boolean dirty;
    protected boolean dragSelecting;
    protected boolean editMode;
    protected boolean gridEnabled = true;
    protected boolean hasGroupSelection;
    protected int gridSize = 8;

    public ActionWidgetScreen(String name, ActionWidgetScreenData data)
    {
        this.data = data;
        this.name = name;

        this.editModeLabel = new LabelWidget("malilib.label.misc.edit_mode");
        this.editModeButton = OnOffButton.simpleSlider(16, () -> this.editMode, this::toggleEditMode);

        this.infoWidget = new InfoIconWidget(DefaultIcons.INFO_ICON_18, "");

        this.closeOnExecuteCheckbox = new CheckBoxWidget("malilib.checkbox.action_widget_screen.close_on_execute",
                                                         "malilib.hover.action.command_deck.close_screen_on_execute",
                                                         this::shouldCloseScreenOnExecute, this::setCloseScreenOnExecute);

        this.closeOnKeyReleaseCheckbox = new CheckBoxWidget("malilib.checkbox.action_widget_screen.close_on_key_release",
                                                            "malilib.hover.action.command_deck.close_screen_on_key_release",
                                                            () -> this.closeScreenOnKeyRelease, this::setCloseScreenOnKeyRelease);

        this.addWidgetButton = GenericButton.create(16, "malilib.button.action_widgets.add_action", this::openAddWidgetScreen);

        this.exportSettingsButton = GenericButton.create(16, "malilib.button.misc.export");
        this.exportSettingsButton.setActionListener(this::onExportSettings);
        this.exportSettingsButton.translateAndAddHoverString("malilib.hover.button.action_widget_screen.export_settings");

        this.importSettingsButton = GenericButton.create(16, "malilib.button.misc.import");
        this.importSettingsButton.setActionListener(this::onImportSettings);
        this.importSettingsButton.translateAndAddHoverString("malilib.hover.button.action_widget_screen.import_settings");

        this.gridLabel = new LabelWidget("malilib.label.misc.grid");
        this.gridEnabledButton = OnOffButton.simpleSlider(16, () -> this.gridEnabled, this::toggleGridEnabled);
        this.gridEditWidget = new IntegerEditWidget(40, 16, this.gridSize, 1, 256, this::setGridSize);

        this.addPreScreenCloseListener(this::onPreScreenClose);
        this.readData(data);
    }

    @Override
    protected void initScreen()
    {
        super.initScreen();

        for (BaseActionExecutionWidget widget : this.widgetList)
        {
            widget.setContainer(this);
            widget.onAdded(this);
            this.addWidget(widget);
        }

        this.hasGroupSelection = this.selectedWidgets.isEmpty() == false;
    }

    @Override
    protected void reAddActiveWidgets()
    {
        super.reAddActiveWidgets();

        this.addWidget(this.editModeLabel);
        this.addWidget(this.editModeButton);

        if (this.editMode)
        {
            this.addWidget(this.closeOnExecuteCheckbox);
            this.addWidget(this.closeOnKeyReleaseCheckbox);

            this.addWidget(this.addWidgetButton);
            this.addWidget(this.infoWidget);

            this.addWidget(this.gridLabel);
            this.addWidget(this.gridEnabledButton);

            this.addWidget(this.exportSettingsButton);
            this.addWidget(this.importSettingsButton);

            if (this.gridEnabled)
            {
                this.addWidget(this.gridEditWidget);
            }
        }
    }

    @Override
    protected void updateWidgetPositions()
    {
        super.updateWidgetPositions();

        int x = this.x + 10;
        int y = this.y + 2;

        this.editModeLabel.setPosition(x, y + 4);
        this.editModeButton.setPosition(this.editModeLabel.getRight() + 6, y);

        if (this.editMode)
        {
            this.gridLabel.setPosition(this.editModeButton.getRight() + 16, y + 4);
            this.gridEnabledButton.setPosition(this.gridLabel.getRight() + 6, y);
            this.gridEditWidget.setPosition(this.gridEnabledButton.getRight() + 6, y);
            this.exportSettingsButton.setPosition(this.gridEditWidget.getRight() + 6, y);
            this.importSettingsButton.setPosition(this.exportSettingsButton.getRight() + 6, y);
            y += 20;

            this.closeOnExecuteCheckbox.setPosition(x, y);
            y += 12;
            this.closeOnKeyReleaseCheckbox.setPosition(x, y);

            int tmpX = Math.max(this.closeOnExecuteCheckbox.getRight(), this.closeOnKeyReleaseCheckbox.getRight()) + 6;
            this.infoWidget.setPosition(tmpX, this.closeOnExecuteCheckbox.getY());
            y += 14;

            this.addWidgetButton.setPosition(x, y);
        }
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (this.editMode)
        {
            if (mouseButton == 0)
            {
                if (this.menuWidget != null)
                {
                    this.menuWidget.tryMouseClick(mouseX, mouseY, mouseButton);
                    this.closeMenu();

                    return true;
                }

                if (this.hasGroupSelection && this.leftClickWithGroupSelection(mouseX, mouseY))
                {
                    return true;
                }

                /* FIXME TODO update this
                if (this.mouseActionHandlers.isEmpty())
                {
                    this.selectionStart = new Vec2i(mouseX, mouseY);
                    this.dragSelecting = true;
                    return true;
                }
                */
            }
            else if (mouseButton == 1 && isShiftDown() == false)
            {
                if (this.hasGroupSelection)
                {
                    this.openGroupMenu(mouseX, mouseY);
                    return true;
                }
                else
                {
                    // Close any possible previous menu
                    this.closeMenu();

                    BaseActionExecutionWidget widget = this.getHoveredActionWidget(mouseX, mouseY);

                    if (widget != null)
                    {
                        this.openSingleWidgetMenu(mouseX, mouseY, widget);
                        return true;
                    }
                }
            }
        }

        return super.onMouseClicked(mouseX, mouseY, mouseButton);
    }

    protected boolean leftClickWithGroupSelection(int mouseX, int mouseY)
    {
        boolean clickedWidget = this.getHoveredActionWidget(mouseX, mouseY) != null;

        if (clickedWidget)
        {
            for (BaseActionExecutionWidget widget : this.selectedWidgets)
            {
                widget.startDragging(mouseX, mouseY);
            }

            return true;
        }
        /* FIXME TODO update this
        else if (isCtrlDown() == false && this.mouseActionHandlers.isEmpty())
        {
            this.clearSelectedWidgets();
        }
        */

        return false;
    }

    @Override
    public boolean onMouseReleased(int mouseX, int mouseY, int mouseButton)
    {
        if (this.dragSelecting)
        {
            int minX = Math.min(mouseX, this.selectionStart.x);
            int minY = Math.min(mouseY, this.selectionStart.y);
            int maxX = Math.max(mouseX, this.selectionStart.x);
            int maxY = Math.max(mouseY, this.selectionStart.y);
            EdgeInt selection = new EdgeInt(minY, maxX, maxY, minX);

            for (BaseActionExecutionWidget widget : this.widgetList)
            {
                if (widget.intersects(selection))
                {
                    widget.toggleSelected();
                    this.selectedWidgets.remove(widget);

                    if (widget.isSelected())
                    {
                        this.selectedWidgets.add(widget);
                    }
                }
            }

            this.dragSelecting = false;
            this.hasGroupSelection = this.selectedWidgets.isEmpty() == false;
        }

        return super.onMouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean onKeyTyped(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == Keys.KEY_DELETE)
        {
            this.deleteSelectedWidgets();
            return true;
        }

        return super.onKeyTyped(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean onKeyReleased(int keyCode, int scanCode, int modifiers)
    {
        if (this.editMode == false && this.closeScreenOnKeyRelease &&
            KeyBindImpl.getCurrentlyPressedKeysCount() == 0)
        {
            this.closeScreen();

            int mouseX = GuiUtils.getMouseScreenX();
            int mouseY = GuiUtils.getMouseScreenY();
            BaseActionExecutionWidget widget = this.getHoveredActionWidget(mouseX, mouseY);

            if (widget != null)
            {
                widget.executeActions();
            }
        }

        return false;
    }

    protected void clearActionWidgetContainerData()
    {
        for (BaseActionExecutionWidget widget : this.widgetList)
        {
            // Remove the references to this screen
            widget.setContainer(null);
        }
    }

    protected void readData(ActionWidgetScreenData data)
    {
        this.clearActionWidgetContainerData();

        this.widgetList.clear();
        this.selectedWidgets.clear();

        this.closeScreenOnExecute = data.closeScreenOnExecute;
        this.closeScreenOnKeyRelease = data.closeScreenOnKeyRelease;

        for (BaseActionExecutionWidget widget : data.widgets)
        {
            this.widgetList.add(widget);

            if (widget.isSelected())
            {
                this.selectedWidgets.add(widget);
            }
        }
    }

    protected void onPreScreenClose()
    {
        if (this.dirty ||
            this.closeScreenOnExecute != this.data.closeScreenOnExecute ||
            this.closeScreenOnKeyRelease != this.data.closeScreenOnKeyRelease)
        {
            ActionWidgetScreenData data = this.getCurrentData();
            ActionExecutionWidgetManager.INSTANCE.saveWidgetScreenData(this.name, data);
        }

        this.clearActionWidgetContainerData();
    }

    @Nullable
    protected BaseActionExecutionWidget getHoveredActionWidget(int mouseX, int mouseY)
    {
        for (BaseActionExecutionWidget widget : this.widgetList)
        {
            if (widget.isMouseOver(mouseX, mouseY))
            {
                return widget;
            }
        }

        return null;
    }

    protected ActionWidgetScreenData getCurrentData()
    {
        ImmutableList<BaseActionExecutionWidget> widgets = ImmutableList.copyOf(this.widgetList);
        return new ActionWidgetScreenData(widgets, this.closeScreenOnExecute, this.closeScreenOnKeyRelease);
    }

    protected void closeMenu()
    {
        this.removeWidget(this.menuWidget);
        this.menuWidget = null;
    }

    protected void openSingleWidgetMenu(int mouseX, int mouseY, BaseActionExecutionWidget widget)
    {
        this.menuWidget = new MenuWidget(mouseX + 4, mouseY);

        StyledTextLine textEdit = StyledTextLine.translateFirstLine("malilib.label.misc.edit");
        StyledTextLine textRemove = StyledTextLine.translateFirstLine("malilib.label.misc.delete.colored");
        this.menuWidget.setMenuEntries(new MenuEntryWidget(textEdit, () -> this.editActionWidget(widget)),
                                       new MenuEntryWidget(textRemove, () -> this.removeActionWidget(widget)));

        this.addWidget(this.menuWidget);
        this.menuWidget.setZ(this.z + 40);
        this.menuWidget.updateSubWidgetPositions();
    }

    protected void openGroupMenu(int mouseX, int mouseY)
    {
        this.menuWidget = new MenuWidget(mouseX + 4, mouseY);

        StyledTextLine textEdit = StyledTextLine.translateFirstLine("malilib.label.misc.edit_selected");
        StyledTextLine textRemove = StyledTextLine.translateFirstLine("malilib.label.misc.delete_selected.colored");
        this.menuWidget.setMenuEntries(new MenuEntryWidget(textEdit, this::editSelectedWidgets),
                                       new MenuEntryWidget(textRemove, this::deleteSelectedWidgets));

        this.addWidget(this.menuWidget);
        this.menuWidget.setZ(this.z + 40);
        this.menuWidget.updateSubWidgetPositions();
    }

    protected void addActionWidget(BaseActionExecutionWidget widget)
    {
        widget.setContainer(this);
        widget.setPosition(this.x + this.screenWidth / 2, this.y + 10);
        widget.onAdded(this);

        this.widgetList.add(widget);
        this.addWidget(widget);
        this.notifyWidgetEdited();
    }

    protected void removeActionWidget(BaseActionExecutionWidget widget)
    {
        widget.setContainer(null);
        this.widgetList.remove(widget);
        this.selectedWidgets.remove(widget);
        this.removeWidget(widget);
        this.notifyWidgetEdited();
    }

    protected void editActionWidget(BaseActionExecutionWidget widget)
    {
        this.openActionWidgetEditScreen(ImmutableList.of(widget));
    }

    protected void editSelectedWidgets()
    {
        if (this.selectedWidgets.isEmpty() == false)
        {
            this.openActionWidgetEditScreen(ImmutableList.copyOf(this.selectedWidgets));
        }
    }

    protected void deleteSelectedWidgets()
    {
        if (this.selectedWidgets.isEmpty() == false)
        {
            this.widgetList.removeAll(this.selectedWidgets);

            for (BaseActionExecutionWidget widget : this.selectedWidgets)
            {
                this.removeWidget(widget);
            }

            this.selectedWidgets.clear();
            this.hasGroupSelection = false;
            this.notifyWidgetEdited();
        }
    }

    protected void clearSelectedWidgets()
    {
        for (BaseActionExecutionWidget widget : this.selectedWidgets)
        {
            widget.setSelected(false);
        }

        this.selectedWidgets.clear();
        this.hasGroupSelection = false;
    }

    @Override
    public boolean isEditMode()
    {
        return this.editMode;
    }

    @Override
    public int getGridSize()
    {
        return this.gridEnabled ? this.gridSize : -1;
    }

    @Override
    public boolean shouldCloseScreenOnExecute()
    {
        return this.closeScreenOnExecute;
    }

    @Override
    public void notifyWidgetEdited()
    {
        this.dirty = true;
    }

    protected void toggleGridEnabled()
    {
        this.gridEnabled = ! this.gridEnabled;

        this.removeWidget(this.gridEditWidget);

        if (this.gridEnabled)
        {
            this.addWidget(this.gridEditWidget);
        }
    }

    public void setCloseScreenOnExecute(boolean closeScreenOnExecute)
    {
        this.closeScreenOnExecute = closeScreenOnExecute;
    }

    public void setCloseScreenOnKeyRelease(boolean closeScreenOnKeyRelease)
    {
        this.closeScreenOnKeyRelease = closeScreenOnKeyRelease;
    }

    protected void toggleEditMode()
    {
        this.editMode = ! this.editMode;
        this.initScreen();
    }

    protected void setGridSize(int gridSize)
    {
        this.gridSize = gridSize;
    }

    protected void openAddWidgetScreen()
    {
        AddActionExecutionWidgetScreen screen = new AddActionExecutionWidgetScreen(this::addActionWidget);
        screen.setParent(this);
        BaseScreen.openPopupScreen(screen);
    }

    protected void openActionWidgetEditScreen(List<BaseActionExecutionWidget> widgets)
    {
        EditActionExecutionWidgetScreen screen = new EditActionExecutionWidgetScreen(widgets);
        screen.setParent(this);
        BaseScreen.openPopupScreen(screen);

        // Not technically correct... but micro-optimizing here and detecting
        // widget changes is probably a waste of time
        this.notifyWidgetEdited();
    }

    protected boolean onExportSettings(int mouseButton)
    {
        if (mouseButton == 0 && isShiftDown())
        {
            setStringToClipboard(this.getSettingsExportString());
            MessageDispatcher.success("malilib.message.info.action_screen_settings_copied_to_clipboard");
        }
        else if (mouseButton == 0)
        {
            String str = this.getSettingsExportString();
            TextInputScreen screen = new TextInputScreen("malilib.title.screen.action_screen.export_settings",
                                                         str, (s) -> true);
            screen.setParent(this);
            openPopupScreen(screen);
        }

        return true;
    }

    protected boolean onImportSettings(int mouseButton)
    {
        if (mouseButton == 1 && isCtrlDown() && isShiftDown())
        {
            this.applySettingsFromImportString(getStringFromClipboard());
        }
        else if (mouseButton == 0)
        {
            TextInputScreen screen = new TextInputScreen("malilib.title.screen.action_screen.import_settings",
                                                         "", this::applySettingsFromImportString);
            screen.setParent(this);
            openPopupScreen(screen);
        }

        return true;
    }

    protected String getSettingsExportString()
    {
        ActionWidgetScreenData data = this.getCurrentData();
        return JsonUtils.jsonToString(data.toJson(), true);
    }

    protected boolean applySettingsFromImportString(@Nullable String str)
    {
        if (str != null)
        {
            JsonElement el = JsonUtils.parseJsonFromString(str);

            if (el != null)
            {
                ActionWidgetScreenData data = ActionWidgetScreenData.fromJson(el);

                if (data != null)
                {
                    this.readData(data);
                    this.initScreen();
                    this.notifyWidgetEdited();
                    MessageDispatcher.success("malilib.message.info.action_screen_settings_imported");
                    return true;
                }
            }
        }
    
        return false;
    }

    @Override
    protected void renderCustomContents(ScreenContext ctx)
    {
        if (this.editMode)
        {
            if (this.gridEnabled)
            {
                int color = 0x30FFFFFF;

                ShapeRenderUtils.renderGrid(this.x, this.y, this.z + 0.1f,
                                            this.screenWidth, this.screenHeight, this.gridSize, 1, color, ctx);
            }

            if (this.dragSelecting)
            {
                int minX = Math.min(ctx.mouseX, this.selectionStart.x);
                int minY = Math.min(ctx.mouseY, this.selectionStart.y);
                int maxX = Math.max(ctx.mouseX, this.selectionStart.x);
                int maxY = Math.max(ctx.mouseY, this.selectionStart.y);

                ShapeRenderUtils.renderOutlinedRectangle(minX, minY, this.z + 50f,
                                                         maxX - minX, maxY - minY, 0x30FFFFFF, 0xFFFFFFFF, ctx);
            }
        }
    }

    public static void openCreateActionWidgetScreen()
    {
        TextInputScreen screen = new TextInputScreen("malilib.title.screen.create_action_widget_screen", "",
                                                     ActionExecutionWidgetManager::createActionWidgetScreen);
        screen.setInfoText(StyledText.translate("malilib.info.action.create_action_widget_screen.name_is_final"));
        screen.setLabelText(StyledText.translate("malilib.label.misc.name.colon"));
        BaseScreen.openPopupScreenWithCurrentScreenAsParent(screen);
    }

    public static ActionResult openActionWidgetScreen(ActionContext ctx, String arg)
    {
        ActionWidgetScreenData data = ActionExecutionWidgetManager.INSTANCE.getOrLoadWidgetScreenData(arg);

        if (data != null)
        {
            MaLiLibConfigs.Internal.PREVIOUS_ACTION_WIDGET_SCREEN.setValue(arg);
            BaseScreen.openScreen(new ActionWidgetScreen(arg, data));
            return ActionResult.SUCCESS;
        }
        else
        {
            MessageDispatcher.error("malilib.message.error.no_action_screen_found_by_name", arg);
            return ActionResult.FAIL;
        }
    }

    public static ActionResult openPreviousActionWidgetScreen(ActionContext ctx)
    {
        String name = MaLiLibConfigs.Internal.PREVIOUS_ACTION_WIDGET_SCREEN.getValue();

        if (StringUtils.isBlank(name))
        {
            MessageDispatcher.error("malilib.message.error.no_previously_opened_action_screen");
            return ActionResult.FAIL;
        }

        return openActionWidgetScreen(ctx, name);
    }
}
