package malilib.input;

import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import malilib.MaLiLibReference;
import malilib.action.ActionContext;
import malilib.action.util.ActionUtils;
import malilib.action.NamedAction;
import malilib.config.option.CommonDescription;
import malilib.config.option.ConfigInfo;
import malilib.input.callback.HotkeyCallback;
import malilib.render.text.StyledTextLine;
import malilib.util.data.json.JsonUtils;

public class CustomHotkeyDefinition extends CommonDescription implements Hotkey, ConfigInfo
{
    protected final String name;
    protected final KeyBind keyBind;
    protected ImmutableList<NamedAction> actions;

    public CustomHotkeyDefinition(String name, KeyBind keyBind, ImmutableList<NamedAction> actions)
    {
        super(name, MaLiLibReference.MOD_INFO);

        this.name = name;
        this.keyBind = keyBind;
        this.actions = actions;

        this.keyBind.setModInfo(this.getModInfo());
        this.keyBind.setCallback(HotkeyCallback.of(this::execute));
        this.keyBind.setNameTranslationKey(name);
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public String getDisplayName()
    {
        return this.name;
    }

    @Override
    public boolean isModified()
    {
        return this.keyBind.isModified();
    }

    @Override
    public void resetToDefault()
    {
        this.keyBind.resetToDefault();
    }

    @Override
    public KeyBind getKeyBind()
    {
        return this.keyBind;
    }

    public ImmutableList<NamedAction> getActionList()
    {
        return this.actions;
    }

    public void setActionList(ImmutableList<NamedAction> actions)
    {
        this.actions = actions;
    }

    public StyledTextLine getActionDisplayName()
    {
        if (this.actions.size() == 1)
        {
            return this.actions.get(0).getColoredWidgetDisplayName();
        }

        String key = "malilib.label.custom_hotkeys.widget.action_display_name.multiple_actions";
        return StyledTextLine.translateFirstLine(key, this.actions.size());
    }

    protected ActionResult execute(ActionContext ctx)
    {
        for (NamedAction action : this.actions)
        {
            action.execute(ctx);
        }

        return ActionResult.SUCCESS;
    }

    public JsonObject toJson()
    {
        JsonObject obj = new JsonObject();

        obj.addProperty("name", this.name);
        obj.add("hotkey", this.keyBind.getAsJsonElement());
        obj.add("actions", JsonUtils.toArray(this.actions, NamedAction::toJson));

        return obj;
    }

    @Nullable
    public static CustomHotkeyDefinition fromJson(JsonElement el)
    {
        if (el.isJsonObject() == false)
        {
            return null;
        }

        JsonObject obj = el.getAsJsonObject();
        String name = JsonUtils.getStringOrDefault(obj, "name", "?");
        KeyBind keyBind = KeyBindImpl.fromStorageString("", KeyBindSettings.INGAME_DEFAULT);

        if (JsonUtils.hasObject(obj, "hotkey"))
        {
            keyBind.setValueFromJsonElement(obj.get("hotkey"), name);
        }

        return new CustomHotkeyDefinition(name, keyBind, ActionUtils.readActionsFromList(obj, "actions"));
    }
}
