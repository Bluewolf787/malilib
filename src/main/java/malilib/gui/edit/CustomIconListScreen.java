package malilib.gui.edit;

import com.google.common.collect.ImmutableList;

import malilib.MaLiLibConfigScreen;
import malilib.MaLiLibReference;
import malilib.action.ActionContext;
import malilib.gui.BaseListScreen;
import malilib.gui.BaseScreen;
import malilib.gui.icon.NamedIcon;
import malilib.gui.widget.button.GenericButton;
import malilib.gui.widget.list.DataListWidget;
import malilib.gui.widget.list.entry.IconEntryWidget;
import malilib.input.ActionResult;
import malilib.registry.Registry;

public class CustomIconListScreen extends BaseListScreen<DataListWidget<NamedIcon>>
{
    protected final GenericButton addIconButton;

    public CustomIconListScreen()
    {
        super(10, 74, 20, 80, MaLiLibReference.MOD_ID, MaLiLibConfigScreen.ALL_TABS, MaLiLibConfigScreen.GENERIC);

        this.addIconButton = GenericButton.create(16, "malilib.button.custom_icons.add_icon", this::openAddIconScreen);

        this.addPreScreenCloseListener(Registry.ICON::saveToFileIfDirty);
        this.createSwitchModConfigScreenDropDown(MaLiLibReference.MOD_INFO);
        this.setTitle("malilib.title.screen.configs.custom_icons_list_screen", MaLiLibReference.MOD_VERSION);
    }

    @Override
    protected void reAddActiveWidgets()
    {
        super.reAddActiveWidgets();

        this.addWidget(this.addIconButton);
    }

    @Override
    protected void updateWidgetPositions()
    {
        super.updateWidgetPositions();

        this.addIconButton.setPosition(this.x + 10, this.y + 57);
    }

    @Override
    protected DataListWidget<NamedIcon> createListWidget()
    {
        DataListWidget<NamedIcon> listWidget = new DataListWidget<>(Registry.ICON::getUserIcons, true);

        listWidget.setListEntryWidgetFixedHeight(22);
        listWidget.setDataListEntryWidgetFactory(IconEntryWidget::new);
        listWidget.addDefaultSearchBar();
        listWidget.setEntryFilterStringFunction(i -> ImmutableList.of(i.getName(), i.getTexture().toString()));

        return listWidget;
    }

    public void addIcon(NamedIcon icon)
    {
        Registry.ICON.registerUserIcon(icon);
        this.getListWidget().refreshEntries();
    }

    protected void openAddIconScreen()
    {
        CustomIconEditScreen screen = new CustomIconEditScreen(this::addIcon);
        screen.setParent(this);
        BaseScreen.openPopupScreen(screen);
    }

    public static ActionResult openCustomIconListScreenAction(ActionContext ctx)
    {
        CustomIconListScreen screen = new CustomIconListScreen();
        screen.setCurrentTab(MaLiLibConfigScreen.ICONS);
        BaseScreen.openScreen(screen);
        return ActionResult.SUCCESS;
    }

    public static BaseScreen openCustomIconListScreen()
    {
        return new CustomIconListScreen();
    }
}
