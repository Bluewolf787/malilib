package malilib.action;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;

import malilib.action.util.ActionUtils;
import malilib.input.ActionResult;
import malilib.render.text.StyledTextLine;
import malilib.util.StringUtils;
import malilib.util.data.ModInfo;
import malilib.util.data.json.JsonUtils;

public class MacroAction extends NamedAction
{
    protected static final ModInfo MACRO_MOD_INFO = getMacroModInfo();

    protected ImmutableList<NamedAction> actionList;

    public MacroAction(String name, ImmutableList<NamedAction> actionList)
    {
        super(ActionType.MACRO, name, name, MACRO_MOD_INFO);

        this.actionList = actionList;
        this.coloredDisplayNameTranslationKey = "malilib.label.actions.macro_entry_widget_name";
    }

    @Override
    public boolean isUserAdded()
    {
        return true;
    }

    public ImmutableList<NamedAction> getActionList()
    {
        return this.actionList;
    }

    public void setActionList(ImmutableList<NamedAction> actionList)
    {
        this.actionList = actionList;
    }

    @Override
    public ActionResult execute(ActionContext ctx)
    {
        for (NamedAction action : this.actionList)
        {
            action.execute(ctx);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public StyledTextLine getColoredWidgetDisplayName()
    {
        String name = this.getName();
        int size = this.getActionList().size();
        return StyledTextLine.translateFirstLine(this.coloredDisplayNameTranslationKey, name, size);
    }

    @Override
    public List<StyledTextLine> getHoverInfo()
    {
        List<StyledTextLine> lines = new ArrayList<>();
        StyledTextLine.translate(lines, "malilib.hover.action.name", this.getName());

        if (this.registryName != null)
        {
            StyledTextLine.translate(lines, "malilib.hover.action.registry_name", this.registryName);
        }

        StyledTextLine.translate(lines, "malilib.hover.action.action_type", this.type.getDisplayName());
        getContainedActionsTooltip(lines, this.actionList, 8);

        return lines;
    }

    public static void getContainedActionsTooltip(List<StyledTextLine> linesOut,
                                                  List<NamedAction> actions,
                                                  int maxEntriesShown)
    {
        int size = actions.size();

        if (size > 0)
        {
            String titleKey = "malilib.hover.action.contained_actions";
            String entryKey = "malilib.hover.action.contained_actions.entry";
            int count = Math.min(size, maxEntriesShown);

            if (maxEntriesShown == size - 1)
            {
                count = size;
            }

            StyledTextLine.translate(linesOut, titleKey, size);

            for (int i = 0; i < count; ++i)
            {
                StyledTextLine.translate(linesOut, entryKey, actions.get(i).getName());
            }

            if (size > count)
            {
                String footerKey = "malilib.hover.action.contained_actions.more";
                StyledTextLine.translate(linesOut, footerKey, size - count);
            }
        }
    }

    @Override
    public JsonObject toJson()
    {
        JsonObject obj = super.toJson();
        obj.addProperty("name", this.name);
        obj.add("actions", JsonUtils.toArray(this.actionList, NamedAction::toJson));
        return obj;
    }

    @Nullable
    public static MacroAction macroActionFromJson(JsonObject obj)
    {
        NamedAction action = NamedAction.baseActionFromJson(obj);

        if (action instanceof MacroAction)
        {
            return (MacroAction) action;
        }

        if (JsonUtils.hasArray(obj, "actions") &&
            JsonUtils.hasString(obj, "name"))
        {
            String name = JsonUtils.getString(obj, "name");
            return new MacroAction(name, ActionUtils.readActionsFromList(obj, "actions"));
        }

        return null;
    }

    public static ModInfo getMacroModInfo()
    {
        return new ModInfo("<macro>", StringUtils.translate("malilib.label.actions.macro_action"));
    }
}
