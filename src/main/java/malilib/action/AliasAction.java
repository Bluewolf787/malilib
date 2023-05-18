package malilib.action;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import com.google.gson.JsonObject;

import malilib.input.ActionResult;
import malilib.registry.Registry;
import malilib.render.text.StyledTextLine;
import malilib.util.StringUtils;
import malilib.util.data.ModInfo;
import malilib.util.data.json.JsonUtils;

public class AliasAction extends NamedAction
{
    protected static final ModInfo ALIAS_MOD_INFO = createAliasModInfo();

    protected final NamedAction baseAction;

    public AliasAction(String alias, NamedAction action)
    {
        super(ActionType.ALIAS, alias, action.getNameTranslationKey(), ALIAS_MOD_INFO);

        this.baseAction = action;
        this.coloredDisplayNameTranslationKey = "malilib.label.actions.alias_entry_widget_name";
    }

    @Override
    public boolean isUserAdded()
    {
        return true;
    }

    @Override
    public ActionResult execute(ActionContext ctx)
    {
        return this.baseAction.execute(ctx);
    }

    @Override
    public StyledTextLine getColoredWidgetDisplayName()
    {
        String name = this.getName();
        String originalName = this.baseAction.getName();
        String modName = this.baseAction.getModInfo().getModName();
        return StyledTextLine.translateFirstLine(this.coloredDisplayNameTranslationKey, name, modName, originalName);
    }

    @Override
    public List<StyledTextLine> getHoverInfo()
    {
        List<StyledTextLine> lines = new ArrayList<>();
        String name = this.getName();
        String displayName = this.baseAction.getDisplayName();

        StyledTextLine.translate(lines, "malilib.hover.action.alias", name);

        if (name.equals(displayName) == false)
        {
            StyledTextLine.translate(lines, "malilib.hover.action.display_name", displayName);
        }

        if (this.registryName != null)
        {
            StyledTextLine.translate(lines, "malilib.hover.action.registry_name", this.registryName);
        }

        StyledTextLine.translate(lines, "malilib.hover.action.action_type", this.type.getDisplayName());
        StyledTextLine.translate(lines, "malilib.hover.action.base_action_mod", this.baseAction.getModInfo().getModName());
        StyledTextLine.translate(lines, "malilib.hover.action.base_action_name", this.baseAction.getName());

        String origRegName = this.baseAction.getRegistryName();

        if (origRegName != null)
        {
            StyledTextLine.translate(lines, "malilib.hover.action.base_action_registry_name", origRegName);
        }

        return lines;
    }

    @Override
    public JsonObject toJson()
    {
        JsonObject obj = super.toJson();
        String regName = this.baseAction.getRegistryName();

        if (regName != null)
        {
            obj.addProperty("parent", regName);
        }

        obj.addProperty("alias", this.name);

        return obj;
    }

    @Nullable
    public static AliasAction aliasActionFromJson(JsonObject obj)
    {
        NamedAction action = baseActionFromJson(obj);

        if (action instanceof AliasAction)
        {
            return (AliasAction) action;
        }

        if (JsonUtils.hasString(obj, "parent") &&
            JsonUtils.hasString(obj, "alias"))
        {
            String parentRegName = JsonUtils.getString(obj, "parent");
            String aliasName = JsonUtils.getString(obj, "alias");
            NamedAction baseAction = Registry.ACTION_REGISTRY.getAction(parentRegName);

            if (baseAction == null)
            {
                // Preserve entries in the config if the mod adding the action is temporarily disabled/removed
                baseAction = new SimpleNamedAction("<N/A>", "<N/A>", ModInfo.NO_MOD, (ctx) -> ActionResult.PASS);
                baseAction.setRegistryName(parentRegName);
            }

            return new AliasAction(aliasName, baseAction);
        }

        return null;
    }

    public static ModInfo createAliasModInfo()
    {
        return new ModInfo("<alias>", StringUtils.translate("malilib.label.actions.alias_action"));
    }

    @Override
    public AliasAction createAlias(String aliasName)
    {
        return new AliasAction(aliasName, this.baseAction);
    }
}
