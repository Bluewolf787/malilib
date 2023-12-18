package malilib.gui.edit;

import javax.annotation.Nullable;

import malilib.gui.BaseScreen;
import malilib.gui.widget.EdgeIntEditWidget;
import malilib.util.data.EdgeInt;

public class EdgeIntEditScreen extends BaseScreen
{
    protected final EdgeIntEditWidget editWidget;

    public EdgeIntEditScreen(EdgeInt value, boolean isColor, String titleKey, @Nullable String centerText)
    {
        this.useTitleHierarchy = false;
        this.backgroundColor = 0xFF000000;
        this.renderBorder = true;
        this.setTitle(titleKey);

        this.editWidget = new EdgeIntEditWidget(300, 100, value, isColor, centerText);

        this.setScreenWidthAndHeight(320, 130);
        this.centerOnScreen();
    }

    @Override
    protected void reAddActiveWidgets()
    {
        super.reAddActiveWidgets();

        this.addWidget(this.editWidget);
    }

    @Override
    protected void updateWidgetPositions()
    {
        super.updateWidgetPositions();

        this.editWidget.setPosition(this.x + 10, this.y + 26);
    }
}
