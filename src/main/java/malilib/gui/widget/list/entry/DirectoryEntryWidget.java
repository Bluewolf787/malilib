package malilib.gui.widget.list.entry;

import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import javax.annotation.Nullable;

import malilib.gui.icon.FileBrowserIconProvider;
import malilib.gui.icon.Icon;
import malilib.gui.util.GuiUtils;
import malilib.gui.util.ScreenContext;
import malilib.gui.widget.InteractableWidget;
import malilib.gui.widget.list.BaseFileBrowserWidget;
import malilib.gui.widget.list.BaseFileBrowserWidget.DirectoryEntry;
import malilib.gui.widget.list.BaseFileBrowserWidget.DirectoryEntryType;
import malilib.gui.widget.list.DataListWidget;
import malilib.gui.widget.list.ListEntryWidgetInitializer;
import malilib.gui.widget.list.header.DataColumn;
import malilib.render.ShapeRenderUtils;
import malilib.render.text.StyledTextLine;
import malilib.render.text.StyledTextUtils;
import malilib.util.FileNameUtils;
import malilib.util.FileUtils;
import malilib.util.StringUtils;
import malilib.util.data.LeftRight;

public class DirectoryEntryWidget extends BaseDataListEntryWidget<DirectoryEntry>
{
    public static final DataColumn<DirectoryEntry> NAME_COLUMN =
            new DataColumn<DirectoryEntry>("malilib.label.file_browser.column.file_name",
                                           Comparator.naturalOrder());

    public static final DataColumn<DirectoryEntry> SIZE_COLUMN =
            new DataColumn<>("malilib.label.file_browser.column.file_size",
                             Comparator.comparingLong((e) -> FileUtils.size(e.getFullPath())));

    public static final DataColumn<DirectoryEntry> TIME_COLUMN =
            new DataColumn<>("malilib.label.file_browser.column.last_modified",
                             Comparator.comparingLong((e) -> FileUtils.getMTime(e.getFullPath())));

    protected static final DecimalFormat FILE_SIZE_FORMAT = new DecimalFormat("###,###,###.#");

    protected final BaseFileBrowserWidget fileBrowserWidget;
    protected final StyledTextLine fileSizeText;
    protected final StyledTextLine modificationTimeText;
    protected final StyledTextLine fullNameText;
    @Nullable protected StyledTextLine clampedNameText;
    protected boolean showSize;
    protected boolean showMTime;
    protected int sizeColumnEndX;
    protected int mTimeColumnEndX;

    public DirectoryEntryWidget(DirectoryEntry entry,
                                DataListEntryWidgetData constructData,
                                BaseFileBrowserWidget fileBrowserWidget,
                                @Nullable FileBrowserIconProvider iconProvider)
    {
        super(entry, constructData);

        this.canReceiveMouseClicks = true;
        this.fileBrowserWidget = fileBrowserWidget;
        this.getTextSettings().setTextShadowEnabled(false);

        this.fullNameText = StyledTextLine.unParsed(this.getDisplayName());
        this.getBackgroundRenderer().getNormalSettings().setEnabledAndColor(true, this.isOdd ? 0xFF202020 : 0xFF303030);
        this.getBackgroundRenderer().getHoverSettings().setColor(0xFF404040);
        this.getBorderRenderer().getHoverSettings().setEnabled(true);

        int textXOffset = 3;
        @Nullable Icon icon = iconProvider != null ? iconProvider.getIconForEntry(entry) : null;

        if (icon != null)
        {
            textXOffset += iconProvider.getEntryIconWidth(entry) + 2;
            this.iconOffset.setXOffset(2);
            this.setIcon(icon);
        }

        this.textOffset.setXOffset(textXOffset);
        this.fileSizeText = StyledTextLine.parseFirstLine(getFileSizeStringFor(entry));

        String mTimeStr = fileBrowserWidget.getDateFormat().format(new Date(FileUtils.getMTime(entry.getFullPath())));
        this.modificationTimeText = StyledTextLine.parseFirstLine(mTimeStr);
    }

    @Override
    protected boolean onMouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (mouseButton == 0)
        {
            this.fileBrowserWidget.closeCurrentContextMenu();

            if (this.data.getType() == DirectoryEntryType.DIRECTORY)
            {
                this.fileBrowserWidget.switchToDirectory(this.data.getDirectory().resolve(this.data.getName()));
                return true;
            }
        }

        if (mouseButton == 1)
        {
            this.fileBrowserWidget.openContextMenuForEntry(mouseX, mouseY, this.originalListIndex);
            return true;
        }

        return super.onMouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void renderAt(int x, int y, float z, ScreenContext ctx)
    {
        super.renderAt(x, y, z, ctx);
        this.renderInfoColumns(x, y, z, ctx);
    }

    protected void renderInfoColumns(int x, int y, float z, ScreenContext ctx)
    {
        boolean hovered = this.isHoveredForRender(ctx);
        int color = this.getTextSettings().getEffectiveTextColor(hovered);
        int nameColor = color;
        int usableHeight = this.getHeight() - this.padding.getVerticalTotal();
        int ty = this.getTextPositionY(y, usableHeight, this.getLineHeight());
        float nameZ = this.clampedNameText != null ? z + 1.0125f : z + 0.0125f;
        int height = this.getHeight();
        StyledTextLine nameText = this.clampedNameText != null ? this.clampedNameText : this.fullNameText;

        if (this.clampedNameText != null &&
            GuiUtils.isMouseInRegion(ctx.mouseX, ctx.mouseY, x, y, NAME_COLUMN.getWidth(), height))
        {
            // Render a black background for the full name text
            int bgWidth = this.fullNameText.renderWidth + 10;
            int bgX = x + this.textOffset.getXOffset() - 3;
            ShapeRenderUtils.renderOutlinedRectangle(bgX, y, z + 1.0f, bgWidth, height, 0xFF000000, 0xFFC0C0C0, ctx);
            nameText = this.fullNameText;
            nameColor = 0xFF40FFFF;
        }

        this.renderTextLine(x, y, nameZ, nameColor, nameText, ctx);

        if (this.showSize)
        {
            this.renderTextLineRightAligned(x + this.sizeColumnEndX, ty, z, color, false, this.fileSizeText, ctx);
        }

        if (this.showMTime)
        {
            this.renderTextLineRightAligned(x + this.mTimeColumnEndX, ty, z, color, false, this.modificationTimeText, ctx);
        }
    }

    protected String getDisplayName()
    {
        if (this.data.getType() == DirectoryEntryType.DIRECTORY)
        {
            return this.data.getDisplayName();
        }
        else
        {
            return FileNameUtils.getFileNameWithoutExtension(this.data.getDisplayName());
        }
    }

    public static String getFileSizeStringFor(DirectoryEntry entry)
    {
        long fileSize = FileUtils.size(entry.getFullPath());
        if (fileSize >= 1024 * 1024 * 1024)
            return FILE_SIZE_FORMAT.format((double) fileSize / 1024.0 / 1024.0 / 1024.0) + " GiB";
        if (fileSize >= 1024 * 1024)
            return FILE_SIZE_FORMAT.format((double) fileSize / 1024.0 / 1024.0) + " MiB";
        return FILE_SIZE_FORMAT.format((double) fileSize / 1024.0) + " KiB";
    }

    public static class WidgetInitializer implements ListEntryWidgetInitializer<DirectoryEntry>
    {
        protected boolean showFileSize;
        protected boolean showFileMTime;

        @Override
        public void onListContentsRefreshed(DataListWidget<DirectoryEntry> dataListWidget, int entryWidgetWidth)
        {
            BaseFileBrowserWidget fileBrowserWidget = (BaseFileBrowserWidget) dataListWidget;
            SimpleDateFormat fmt = fileBrowserWidget.getDateFormat();
            int maxSizeColumnLength = 0;
            int maxTimeColumnLength = 0;
            int sizeTitleWidth = (SIZE_COLUMN.getName().isPresent() ? SIZE_COLUMN.getName().get().renderWidth + 10 : 0);
            int timeTitleWidth = (TIME_COLUMN.getName().isPresent() ? TIME_COLUMN.getName().get().renderWidth + 10 : 0);

            for (DirectoryEntry e : dataListWidget.getFilteredDataList())
            {
                int w = StringUtils.getStringWidth(getFileSizeStringFor(e));
                maxSizeColumnLength = Math.max(maxSizeColumnLength, w);
                w = StringUtils.getStringWidth(fmt.format(new Date(FileUtils.getMTime(e.getFullPath()))));
                maxTimeColumnLength = Math.max(maxTimeColumnLength, w);
            }

            this.showFileSize = fileBrowserWidget.getShowFileSize();
            this.showFileMTime = fileBrowserWidget.getShowFileModificationTime();

            final int padding = 6;
            final int mTimeLen = Math.max(maxTimeColumnLength, timeTitleWidth) + padding;
            final int sizeMaxWidth = Math.max(maxSizeColumnLength, sizeTitleWidth) + padding;

            int relativeRight = entryWidgetWidth - 2;

            if (this.showFileMTime)
            {
                TIME_COLUMN.setWidth(mTimeLen);
                TIME_COLUMN.setMaxContentWidth(mTimeLen - padding);
                TIME_COLUMN.setRelativeStartX(relativeRight - mTimeLen);
                relativeRight -= mTimeLen + 2;
            }

            if (this.showFileSize)
            {
                SIZE_COLUMN.setWidth(sizeMaxWidth);
                SIZE_COLUMN.setMaxContentWidth(sizeMaxWidth - padding);
                SIZE_COLUMN.setRelativeStartX(relativeRight - sizeMaxWidth);
                relativeRight -= sizeMaxWidth + 2;
            }

            NAME_COLUMN.setWidth(relativeRight);
            NAME_COLUMN.setMaxContentWidth(relativeRight - padding);
            NAME_COLUMN.setRelativeStartX(0);
        }

        @Override
        public void applyToEntryWidgets(DataListWidget<DirectoryEntry> dataListWidget)
        {
            int nameColumnWidth = NAME_COLUMN.getWidth() - 20;
            int timeColumnRight = TIME_COLUMN.getRelativeRight() - 3;
            int sizeColumnRight = SIZE_COLUMN.getRelativeRight() - 3;

            for (InteractableWidget w : dataListWidget.getEntryWidgetList())
            {
                if (w instanceof DirectoryEntryWidget)
                {
                    DirectoryEntryWidget widget = (DirectoryEntryWidget) w;

                    widget.showSize = this.showFileSize && Files.isRegularFile(widget.data.getFullPath());
                    widget.showMTime = this.showFileMTime;
                    widget.mTimeColumnEndX = timeColumnRight;
                    widget.sizeColumnEndX = sizeColumnRight;

                    if (widget.fullNameText.renderWidth >= nameColumnWidth)
                    {
                        widget.clampedNameText = StyledTextUtils.clampStyledTextToMaxWidth(
                                widget.fullNameText, nameColumnWidth, LeftRight.RIGHT, " ...");
                    }
                    else
                    {
                        widget.clampedNameText = null;
                    }
                }
            }
        }
    }
}
