package malilib.action.util;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;

import malilib.action.Action;
import malilib.action.ActionType;
import malilib.action.MacroAction;
import malilib.action.NamedAction;
import malilib.action.ParameterizableNamedAction;
import malilib.action.ParameterizedAction;
import malilib.action.SimpleNamedAction;
import malilib.action.builtin.BooleanDisableAction;
import malilib.action.builtin.BooleanEnableAction;
import malilib.action.builtin.BooleanToggleAction;
import malilib.config.option.BooleanConfig;
import malilib.config.option.BooleanContainingConfig;
import malilib.config.option.ConfigInfo;
import malilib.config.option.HotkeyedBooleanConfig;
import malilib.input.KeyBind;
import malilib.listener.EventListener;
import malilib.overlay.message.MessageHelpers.BooleanConfigMessageFactory;
import malilib.overlay.message.MessageOutput;
import malilib.registry.Registry;
import malilib.util.StringUtils;
import malilib.util.data.ModInfo;
import malilib.util.data.json.JsonUtils;

public class ActionUtils
{
    public static void registerBooleanConfigActions(List<? extends ConfigInfo> list)
    {
        for (ConfigInfo cfg : list)
        {
            if (cfg instanceof HotkeyedBooleanConfig)
            {
                registerHotkeyedBooleanConfigActions(cfg.getModInfo(), (HotkeyedBooleanConfig) cfg);
            }
            else if (cfg instanceof BooleanConfig)
            {
                registerBooleanConfigActions(cfg.getModInfo(), (BooleanConfig) cfg);
            }
        }
    }

    public static SimpleNamedAction register(ModInfo modInfo,
                                             String name,
                                             EventListener action)
    {
        SimpleNamedAction namedAction = SimpleNamedAction.of(modInfo, name, action);
        Registry.ACTION_REGISTRY.registerAction(namedAction);
        return namedAction;
    }

    public static SimpleNamedAction register(ModInfo modInfo,
                                             String name,
                                             Action action)
    {
        SimpleNamedAction namedAction = SimpleNamedAction.of(modInfo, name, action);
        Registry.ACTION_REGISTRY.registerAction(namedAction);
        return namedAction;
    }

    public static ParameterizableNamedAction register(ModInfo modInfo,
                                                      String name,
                                                      ParameterizedAction action)
    {
        ParameterizableNamedAction namedAction = ParameterizableNamedAction.of(modInfo, name, action);
        Registry.ACTION_REGISTRY.registerAction(namedAction);
        return namedAction;
    }

    public static void registerBooleanConfigActions(ModInfo modInfo, BooleanContainingConfig<?> config)
    {
        registerBooleanConfigActions(modInfo, config, null, null);
    }

    /**
     * Registers the boolean config toggle, enable and disable actions, using the
     * provided keybind's KeybindSettings as the source of the MessageOutput
     */
    public static void registerBooleanConfigActions(ModInfo modInfo,
                                                    BooleanConfig config,
                                                    KeyBind keyBind)
    {
        registerBooleanConfigActions(modInfo, config, null, keyBind.getSettings()::getMessageType);
    }

    public static void registerBooleanConfigActions(ModInfo modInfo,
                                                    BooleanContainingConfig<?> config,
                                                    @Nullable BooleanConfigMessageFactory messageFactory,
                                                    @Nullable Supplier<MessageOutput> messageTypeSupplier)
    {
        BooleanToggleAction toggleAction = BooleanToggleAction.of(config, messageFactory, messageTypeSupplier);
        registerBooleanConfigActions(modInfo, config, messageFactory, messageTypeSupplier, toggleAction);
    }

    public static void registerBooleanConfigActions(ModInfo modInfo,
                                                    BooleanContainingConfig<?> config,
                                                    @Nullable BooleanConfigMessageFactory messageFactory,
                                                    @Nullable Supplier<MessageOutput> messageTypeSupplier,
                                                    Action toggleAction)
    {
        String configName = org.apache.commons.lang3.StringUtils.capitalize(config.getName());
        String commentKey = config.getCommentTranslationKey();

        BooleanEnableAction enableAction = BooleanEnableAction.of(config, messageFactory, messageTypeSupplier);
        BooleanDisableAction disableAction = BooleanDisableAction.of(config, messageFactory, messageTypeSupplier);
        SimpleNamedAction namedToggleAction = SimpleNamedAction.of(modInfo, "toggle" + configName, toggleAction, commentKey);
        SimpleNamedAction namedEnableAction = SimpleNamedAction.of(modInfo, "enable" + configName, enableAction, commentKey);
        SimpleNamedAction namedDisableAction = SimpleNamedAction.of(modInfo, "disable" + configName, disableAction, commentKey);

        Registry.ACTION_REGISTRY.registerAction(namedToggleAction);
        Registry.ACTION_REGISTRY.registerAction(namedEnableAction);
        Registry.ACTION_REGISTRY.registerAction(namedDisableAction);
    }

    public static void registerHotkeyedBooleanConfigActions(ModInfo modInfo,
                                                            HotkeyedBooleanConfig config)
    {
        registerHotkeyedBooleanConfigActions(modInfo, config, null, null);
    }

    public static void registerHotkeyedBooleanConfigActions(ModInfo modInfo,
                                                            HotkeyedBooleanConfig config,
                                                            @Nullable BooleanConfigMessageFactory messageFactory,
                                                            @Nullable Supplier<MessageOutput> messageTypeSupplier)
    {
        Action toggleAction = config.getToggleAction();
        registerBooleanConfigActions(modInfo, config, messageFactory, messageTypeSupplier, toggleAction);
    }

    /**
     * Constructs the default registry name for the given action,
     * in the format "modid:action_name".
     */
    public static String createRegistryNameFor(ModInfo modInfo, String name)
    {
        return modInfo.getModId() + ":" + name;
    }

    /**
     * Constructs the default translation key for the given action.
     * Tries, in order, the keys in the format "modid.action.name.action_name",
     * "modid.hotkey.name.action_name" and "modid.config.name.action_name"
     * to see which one has a translation.
     * If none of them do, then the name is returned as-is.
     */
    public static String createTranslationKeyFor(ModInfo modInfo, String name)
    {
        String modId = modInfo.getModId();
        String key = modId + ".action.name." + name.toLowerCase(Locale.ROOT);

        if (StringUtils.translate(key).equals(key) == false)
        {
            return key;
        }

        key = modId + ".hotkey.name." + name.toLowerCase(Locale.ROOT);

        if (StringUtils.translate(key).equals(key) == false)
        {
            return key;
        }

        key = modId + ".config.name." + name.toLowerCase(Locale.ROOT);

        if (StringUtils.translate(key).equals(key) == false)
        {
            return key;
        }

        return name;
    }

    public static ImmutableList<SimpleNamedAction> getSimpleActions()
    {
        ImmutableList.Builder<SimpleNamedAction> builder = ImmutableList.builder();

        for (NamedAction action : Registry.ACTION_REGISTRY.getBaseActions())
        {
            if (action instanceof SimpleNamedAction)
            {
                builder.add((SimpleNamedAction) action);
            }
        }

        return builder.build();
    }

    public static ImmutableList<ParameterizableNamedAction> getParameterizableActions()
    {
        ImmutableList.Builder<ParameterizableNamedAction> builder = ImmutableList.builder();

        for (NamedAction action : Registry.ACTION_REGISTRY.getBaseActions())
        {
            if (action instanceof ParameterizableNamedAction)
            {
                builder.add((ParameterizableNamedAction) action);
            }
        }

        return builder.build();
    }

    public static ImmutableList<NamedAction> getUserAddedActions()
    {
        ImmutableList.Builder<NamedAction> builder = ImmutableList.builder();

        for (NamedAction action : Registry.ACTION_REGISTRY.getAllActions())
        {
            if (action.isUserAdded())
            {
                builder.add(action);
            }
        }

        return builder.build();
    }

    public static boolean containsMacroLoop(MacroAction macro, Collection<NamedAction> actions)
    {
        for (NamedAction action : actions)
        {
            if (action == macro)
            {
                return true;
            }

            if (action instanceof MacroAction)
            {
                MacroAction m = (MacroAction) action;

                if (containsMacroLoop(macro, m.getActionList()))
                {
                    return true;
                }
            }
        }

        return false;
    }

    public static ImmutableList<NamedAction> readActionsFromList(JsonObject obj, String arrayName)
    {
        ImmutableList.Builder<NamedAction> builder = ImmutableList.builder();
        readActionsFromList(obj, arrayName, builder::add);
        return builder.build();
    }

    public static void readActionsFromList(JsonObject obj, String arrayName, Consumer<NamedAction> consumer)
    {
        JsonUtils.getArrayElementsIfObjects(obj, arrayName, (o) -> loadActionFrom(o, consumer));
    }

    public static void loadActionFrom(JsonObject obj, Consumer<NamedAction> consumer)
    {
        NamedAction action = ActionType.loadActionFromJson(obj);

        if (action != null)
        {
            consumer.accept(action);
        }
    }
}
