package malilib.gui.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import com.google.common.collect.ImmutableList;

import malilib.action.MacroAction;
import malilib.action.NamedAction;
import malilib.action.ParameterizableNamedAction;
import malilib.action.ParameterizedNamedAction;
import malilib.action.util.ActionUtils;
import malilib.gui.BaseScreen;
import malilib.gui.DualTextInputScreen;
import malilib.gui.widget.LabelWidget;
import malilib.gui.widget.button.GenericButton;
import malilib.gui.widget.list.DataListWidget;
import malilib.gui.widget.list.entry.BaseListEntryWidget;
import malilib.gui.widget.list.entry.DataListEntryWidgetData;
import malilib.gui.widget.list.entry.action.ActionListBaseActionEntryWidget;
import malilib.gui.widget.list.entry.action.ParameterizableActionEntryWidget;
import malilib.overlay.message.MessageDispatcher;
import malilib.registry.Registry;
import malilib.util.data.AppendOverwrite;

public class MacroActionEditScreen extends BaseActionListScreen
{
    protected final ImmutableList<NamedAction> originalMacroActionsList;
    protected final List<NamedAction> macroActionsList;
    protected final LabelWidget macroActionsLabelWidget;
    protected final GenericButton addActionsButton;
    protected final MacroAction macro;

    public MacroActionEditScreen(MacroAction macro)
    {
        super("", Collections.emptyList(), null);

        this.macro = macro;
        this.originalMacroActionsList = macro.getActionList();

        this.setTitle("malilib.title.screen.edit_macro");

        this.macroActionsLabelWidget = new LabelWidget("malilib.label.actions.macro_edit_screen.contained_actions", macro.getName());

        this.addActionsButton = GenericButton.create(15, "malilib.button.macro_edit_screen.add_actions", this::addSelectedActions);
        this.addActionsButton.translateAndAddHoverString("malilib.hover.macro_edit_screen.add_actions");
        this.addActionsButton.setEnabledStatusSupplier(this::canAddActions);

        this.leftSideListWidget.setDataListEntryWidgetFactory(this::createMacroSourceActionsWidget);
        this.rightSideListWidget = this.createRightSideActionListWidget();

        this.importRadioWidgetHoverText = "malilib.hover.macro_action_export_import_screen.append_overwrite";

        // fetch the backing list reference from the list widget
        this.macroActionsList = this.rightSideListWidget.getNonFilteredDataList();
        this.macroActionsList.addAll(this.originalMacroActionsList);
    }

    @Override
    protected void addActionListScreenWidgets()
    {
        super.addActionListScreenWidgets();

        this.addWidget(this.macroActionsLabelWidget);
        this.addWidget(this.addActionsButton);
        this.addListWidget(this.rightSideListWidget);
    }

    @Override
    protected void updateActionListScreenWidgetPositions(int x, int y, int w)
    {
        super.updateActionListScreenWidgetPositions(x, y, w);

        this.addActionsButton.setY(y);
        this.addActionsButton.setRight(this.leftSideListWidget.getRight());

        x = this.leftSideListWidget.getRight() + this.centerGap;
        y = this.leftSideListWidget.getY();
        int h = this.screenHeight - y - 6;
        this.rightSideListWidget.setPositionAndSize(x, y, w, h);
        this.macroActionsLabelWidget.setPosition(x + 2, y - 10);
    }

    @Override
    protected void saveChangesOnScreenClose()
    {
        if (this.originalMacroActionsList.equals(this.macroActionsList) == false)
        {
            this.macro.setActionList(ImmutableList.copyOf(this.macroActionsList));
            Registry.ACTION_REGISTRY.saveToFile();
        }
    }

    protected boolean canAddActions()
    {
        return this.leftSideListWidget.getEntrySelectionHandler().getSelectedEntryCount() > 0;
    }

    protected void addSelectedActions()
    {
        Collection<NamedAction> selectedActions = this.leftSideListWidget.getSelectedEntries();

        if (selectedActions.isEmpty() == false)
        {
            if (ActionUtils.containsMacroLoop(this.macro, selectedActions))
            {
                MessageDispatcher.error("malilib.message.error.action.macro_add_actions_loop_detected");
                return;
            }

            this.macroActionsList.addAll(selectedActions);
            this.rightSideListWidget.refreshEntries();
        }
    }

    protected boolean addAction(NamedAction action)
    {
        if (ActionUtils.containsMacroLoop(this.macro, Collections.singletonList(action)))
        {
            MessageDispatcher.error("malilib.message.error.action.macro_add_actions_loop_detected");
            return false;
        }

        this.macroActionsList.add(action);
        this.rightSideListWidget.refreshEntries();

        return true;
    }

    protected void removeAction(int originalListIndex, NamedAction action)
    {
        if (originalListIndex >= 0 && originalListIndex < this.macroActionsList.size())
        {
            this.macroActionsList.remove(originalListIndex);
        }

        this.rightSideListWidget.getEntrySelectionHandler().clearSelection();
        this.rightSideListWidget.refreshEntries();
    }

    protected void removeSelectedActions()
    {
        Set<Integer> selectedActions = this.rightSideListWidget.getEntrySelectionHandler().getSelectedEntryIndices();

        if (selectedActions.isEmpty() == false)
        {
            List<Integer> indices = new ArrayList<>(selectedActions);

            // reverse order, so that we can remove the entries without the indices being shifted over
            indices.sort(Comparator.reverseOrder());

            for (int index : indices)
            {
                if (index >= 0 && index < this.macroActionsList.size())
                {
                    this.macroActionsList.remove(index);
                }
            }

            this.rightSideListWidget.getEntrySelectionHandler().clearSelection();
            this.rightSideListWidget.refreshEntries();
        }
    }

    protected void openParameterizedActionEditScreen(int originalListIndex)
    {
        if (originalListIndex >= 0 && originalListIndex < this.macroActionsList.size())
        {
            NamedAction action = this.macroActionsList.get(originalListIndex);

            if (action instanceof ParameterizedNamedAction)
            {
                ParameterizedNamedAction parAction = (ParameterizedNamedAction) action;
                DualTextInputScreen screen = ParameterizableActionEntryWidget.createParameterizationPrompt(
                        action.getName(), parAction.getArgument(),
                        (str1, str2) -> this.editParameterizedAction(originalListIndex, parAction, str1, str2));
                BaseScreen.openPopupScreen(screen);
            }
        }
    }

    protected boolean editParameterizedAction(int originalListIndex,
                                              ParameterizedNamedAction originalAction,
                                              String newName,
                                              String newArgument)
    {
        NamedAction newAction = originalAction.createCopy(newName, newArgument);
        return this.editParameterizedAction(originalListIndex, newAction);
    }

    protected boolean editParameterizedAction(int originalListIndex, NamedAction action)
    {
        if (originalListIndex >= 0 && originalListIndex < this.macroActionsList.size())
        {
            this.macroActionsList.set(originalListIndex, action);
            this.rightSideListWidget.refreshEntries();
            return true;
        }

        return false;
    }

    @Override
    protected void importEntries(List<NamedAction> list, AppendOverwrite mode)
    {
        if (mode == AppendOverwrite.OVERWRITE)
        {
            this.macroActionsList.clear();
        }

        int count = list.size();
        this.macroActionsList.addAll(list);
        this.rightSideListWidget.refreshEntries();

        if (count > 0)
        {
            MessageDispatcher.success("malilib.message.info.successfully_imported_n_entries", count);
        }
        else
        {
            MessageDispatcher.warning("malilib.message.warn.import_entries.didnt_import_any_entries");
        }
    }

    @Override
    protected DataListWidget<NamedAction> createRightSideActionListWidget()
    {
        DataListWidget<NamedAction> listWidget = this.createBaseActionListWidget(Collections::emptyList, false);

        listWidget.setDataListEntryWidgetFactory(this::createMacroMemberWidget);

        return listWidget;
    }

    protected ActionListBaseActionEntryWidget createBaseMacroEditScreenActionWidget(
            NamedAction data, DataListEntryWidgetData constructData)
    {
        ActionListBaseActionEntryWidget widget;

        if (data instanceof ParameterizableNamedAction)
        {
            ParameterizableActionEntryWidget parWidget = new ParameterizableActionEntryWidget(data, constructData);
            parWidget.setParameterizedActionConsumer(this::addAction);
            parWidget.setParameterizationButtonHoverText("malilib.hover.button.parameterize_action_for_macro");
            widget = parWidget;
        }
        else
        {
            widget = new ActionListBaseActionEntryWidget(data, constructData);
        }

        return widget;
    }

    protected BaseListEntryWidget createMacroSourceActionsWidget(NamedAction data,
                                                                 DataListEntryWidgetData constructData)
    {
        ActionListBaseActionEntryWidget widget = this.createBaseMacroEditScreenActionWidget(data, constructData);

        widget.setNoRemoveButtons();

        return widget;
    }

    protected BaseListEntryWidget createMacroMemberWidget(NamedAction data,
                                                          DataListEntryWidgetData constructData)
    {
        ActionListBaseActionEntryWidget widget = this.createBaseMacroEditScreenActionWidget(data, constructData);

        if (data instanceof ParameterizedNamedAction)
        {
            widget.setActionEditFunction((i, a) -> this.openParameterizedActionEditScreen(i));
            widget.setEditButtonHoverText("malilib.hover.button.re_parameterize_action_for_macro");
        }
        else if (data instanceof MacroAction)
        {
            widget.setActionEditFunction((i, a) -> ActionListBaseActionEntryWidget.openMacroEditScreen(a, this));
        }

        widget.setCanReOrder(true);
        widget.setActionRemoveFunction(this::removeAction);

        return widget;
    }
}
