package org.teamapps.wiki.app.editor;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.ux.form.FormWindow;
import org.teamapps.common.format.Color;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.icon.emoji.EmojiIcon;
import org.teamapps.icons.Icon;
import org.teamapps.ux.component.dialogue.Dialogue;
import org.teamapps.ux.component.field.Button;
import org.teamapps.ux.component.field.TextField;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.template.BaseTemplateRecord;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.tree.TreeNodeInfoImpl;
import org.teamapps.ux.model.ComboBoxModel;
import org.teamapps.ux.model.ListTreeModel;
import org.teamapps.ux.session.CurrentSessionContext;
import org.teamapps.wiki.app.WikiUtils;
import org.teamapps.wiki.model.wiki.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PageSettingsForm {

    private FormWindow formWindow;

    private TextField pageTitleField;
    private TextField pageDescriptionField;

    private ComboBox<EmojiIcon> emojiIconComboBox;

    private ComboBox<Page> pageComboBox;

    private Page page;


    public void create(ApplicationInstanceData applicationInstanceData,
                       ListTreeModel<Page> pageListModel,
                       Function<Page, Void> onPageSettingsSave,
                       Runnable onPageSettingsCancel,
                       Runnable onPageSettingsPageDelete) {

        formWindow = new FormWindow(EmojiIcon.GEAR, "Page Settings", applicationInstanceData);

        ToolbarButton saveButton = formWindow.addSaveButton();
        ToolbarButton cancelButton = formWindow.addCancelButton();

        pageTitleField = new TextField();
        pageDescriptionField = new TextField();

//        pageTitleField.setValue(page.getTitle());
//        pageDescriptionField.setValue(page.getDescription());

        emojiIconComboBox = new ComboBox<>();

        emojiIconComboBox.setModel(getEmojiIconComboBoxModel());
        emojiIconComboBox.setTemplate(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
        emojiIconComboBox.setPropertyProvider(getEmojiIconPropertyProvider());
        emojiIconComboBox.setRecordToStringFunction(EmojiIcon::getIconId);
//        emojiIconComboBox.setValue((page.getEmoji() != null) ? EmojiIcon.forUnicode(page.getEmoji()) : null);

        pageComboBox = new ComboBox<>();
//        ListTreeModel<Page> pageListModel = new ListTreeModel<>(getReOrderedPages(selectedChapter.get()));
        pageComboBox.setModel(pageListModel);
        pageComboBox.setTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES);
        pageComboBox.setPropertyProvider(getPagePropertyProvider());
        pageComboBox.setShowClearButton(true);
//        pageComboBox.setValue(page.getParent());

        pageListModel.setTreeNodeInfoFunction(p -> new TreeNodeInfoImpl<>(p.getParent(), true, true, false));
        pageComboBox.setRecordToStringFunction(chapter -> chapter.getTitle() + " - " + chapter.getDescription());

        formWindow.addSection();
        formWindow.addField("Page Icon", emojiIconComboBox);
        formWindow.addField("Page Title", pageTitleField);
        formWindow.addField("Page Description", pageDescriptionField);
        formWindow.addSection(EmojiIcon.CARD_INDEX_DIVIDERS, "Placement");
        formWindow.addField("Parent Page", pageComboBox);

        Button<BaseTemplateRecord> deleteButton = Button.create(EmojiIcon.WASTEBASKET, "DELETE PAGE").setColor(Color.MATERIAL_RED_600);
        formWindow.getFormLayout().addSection(EmojiIcon.WARNING, "Danger Zone").setCollapsed(true);
        formWindow.getFormLayout().addLabelComponent(deleteButton);
        formWindow.addField("Delete page permanently", deleteButton);

        deleteButton.onClicked.addListener(onDeletePageClicked(onPageSettingsPageDelete));
        saveButton.onClick.addListener(onSavePageClicked(onPageSettingsSave));
        cancelButton.onClick.addListener(onCancelPageClicked(onPageSettingsCancel));

//        formWindow.show();
    }

    public void close() {
        formWindow.close();
    }

    public void show(Page editPage, ListTreeModel<Page> pageListModel) {

        // ToDo : Form Window can be closed with the x-button. The behaviour of the x-button must be the same as the
        //        cancel-button. But we can neither disable the x-button nor add a handler for the onClickedEvent.
        //        Hence, if the user clicks the x-button instead of the cancel-button, the toolbar buttons are not
        //        set to visible again!

        page = editPage;

        if (Objects.isNull(page)) {
            CurrentSessionContext.get().showNotification(EmojiIcon.WARNING, "Failed to edit settings of a non-existing (NULL) page!");
            System.err.println("showPageSettingsWindow : page is null!");
            return;
        } else {
            System.out.println("showPageSettingsWindow : page title = " + page.getTitle());
        }


//        FormWindow formWindow = new FormWindow(EmojiIcon.GEAR, "Page Settings", applicationInstanceData);
//        ToolbarButton saveButton = formWindow.addSaveButton();
//        ToolbarButton cancelButton = formWindow.addCancelButton();
//
//        TextField pageTitleField = new TextField();
//        TextField pageDescriptionField = new TextField();

        pageTitleField.setValue(page.getTitle());
        pageDescriptionField.setValue(page.getDescription());

//        ComboBox<EmojiIcon> emojiIconComboBox = new ComboBox<>();
//        ComboBoxModel<EmojiIcon> iconModel = getEmojiIconComboBoxModel();
//        emojiIconComboBox.setModel(iconModel);
//        emojiIconComboBox.setTemplate(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
//        emojiIconComboBox.setPropertyProvider(getEmojiIconPropertyProvider());
//        emojiIconComboBox.setRecordToStringFunction(EmojiIcon::getIconId);
        emojiIconComboBox.setValue((page.getEmoji() != null) ? EmojiIcon.forUnicode(page.getEmoji()) : null);

//        ComboBox<Page> pageComboBox = new ComboBox<>();
//        ListTreeModel<Page> pageListModel = new ListTreeModel<>(getReOrderedPages(selectedChapter.get()));
        pageComboBox.setModel(pageListModel);
//        pageComboBox.setTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES);
//        pageComboBox.setPropertyProvider(getPagePropertyProvider());
//        pageComboBox.setPropertyProvider(pagePropertyProvider);
//        pageComboBox.setShowClearButton(true);
        pageComboBox.setValue(page.getParent());
//        pageListModel.setTreeNodeInfoFunction(p -> new TreeNodeInfoImpl<>(p.getParent(), true, true, false));
//        pageComboBox.setRecordToStringFunction(chapter -> chapter.getTitle() + " - " + chapter.getDescription());

//        formWindow.addSection();
//        formWindow.addField("Page Icon", emojiIconComboBox);
//        formWindow.addField("Page Title", pageTitleField);
//        formWindow.addField("Page Description", pageDescriptionField);
//        formWindow.addSection(EmojiIcon.CARD_INDEX_DIVIDERS, "Placement");
//        formWindow.addField("Parent Page", pageComboBox);
//
//        Button<BaseTemplateRecord> deleteButton = Button.create(EmojiIcon.WASTEBASKET, "DELETE PAGE").setColor(Color.MATERIAL_RED_600);
//        formWindow.getFormLayout().addSection(EmojiIcon.WARNING, "Danger Zone").setCollapsed(true);
//        formWindow.getFormLayout().addLabelComponent(deleteButton);
//        formWindow.addField("Delete page permanently", deleteButton);
//        deleteButton.onClicked.addListener(() -> {
//            System.out.println("  PageSettings.deleteButton.onClick");
//
//            // ToDo: Cascading delete; currently children are lifted up one level
//            Dialogue okCancel = Dialogue.createOkCancel(EmojiIcon.WARNING, "Permanently delete page \"" + page.getTitle() + "\"?", "Do you really want to delete this page?");
//            okCancel.show();
//            okCancel.onResult.addListener(isConfirmed -> {
//                if (isConfirmed) {
//                    // ToDo Unlock page
//                    onPageSettingsPageDelete.run();
////                    page.delete();
////                    selectedPage.set(null);
////                    bookContentView.setPageEditMode(BookContentView.PAGE_EDIT_MODE.OFF);
//                    formWindow.close();
//                }
//            });
//        });
//
//        saveButton.onClick.addListener(() -> {
//            System.out.println("  PageSettings.saveButton.onClick");
//
//            page.setTitle(pageTitleField.getValue());
//            page.setDescription(pageDescriptionField.getValue());
//
//            Page newParent = pageComboBox.getValue();
//            if (WikiUtils.isChildPage(newParent, page)) {
//                CurrentSessionContext.get().showNotification(EmojiIcon.PROHIBITED, "Invalid new Parent");
//                newParent = page.getParent();
//            }
//            page.setParent(page.equals(newParent) ? null : newParent);
//            page.setEmoji(emojiIconComboBox.getValue() != null ? emojiIconComboBox.getValue().getUnicode() : null);
//            page.save();
////            bookContentView.setPageEditMode(BookContentView.PAGE_EDIT_MODE.OFF);
////            updateContentView();
////            updatePageTree();
////            selectedPage.set(page); // update views
//            onPageSettingsSave.apply(page);
//            formWindow.close();
//        });
//
//        cancelButton.onClick.addListener(onCancelPageClicked(onPageSettingsCancel));

        formWindow.show();
    }



    @NotNull
    private Runnable onDeletePageClicked(Runnable onPageSettingsPageDelete) {
        return () -> {
            System.out.println("  PageSettings.deleteButton.onClick");

            // ToDo: Cascading delete; currently children are lifted up one level
            Dialogue okCancel = Dialogue.createOkCancel(
                    EmojiIcon.WARNING,
                    "Permanently delete page \"" + page.getTitle() + "\"?",
                    "Do you really want to delete this page?");

            okCancel.show();
            okCancel.onResult.addListener(isConfirmed -> {
                if (isConfirmed) {
                    onPageSettingsPageDelete.run();
                    formWindow.close();
                }
            });
        };
    }

    @NotNull
    private Runnable onSavePageClicked(Function<Page, Void>  onPageSettingsSave) {

        return () -> {
            System.out.println("  PageSettings.saveButton.onClick");

            page.setTitle(pageTitleField.getValue());
            page.setDescription(pageDescriptionField.getValue());

            Page newParent = pageComboBox.getValue();
            if (WikiUtils.isChildPage(newParent, page)) {
                CurrentSessionContext.get().showNotification(EmojiIcon.PROHIBITED, "Invalid new Parent");
                newParent = page.getParent();
            }
            page.setParent(page.equals(newParent) ? null : newParent);
            page.setEmoji(emojiIconComboBox.getValue() != null ? emojiIconComboBox.getValue().getUnicode() : null);
            page.save();
//            bookContentView.setPageEditMode(BookContentView.PAGE_EDIT_MODE.OFF);
//            updateContentView();
//            updatePageTree();
//            selectedPage.set(page); // update views
            onPageSettingsSave.apply(page);

            formWindow.close();
        };
    }

    @NotNull
    private Runnable onCancelPageClicked(Runnable onPageSettingsCancel) {
        return () -> {
            System.out.println("  PageSettings.cancelButton.onClick");
//            bookContentView.setPageEditMode(BookContentView.PAGE_EDIT_MODE.OFF);
            onPageSettingsCancel.run();
        };
    }


    private PropertyProvider<Page> getPagePropertyProvider() {

        return (page, propertyNames) -> {
            Map<String, Object> map = new HashMap<>();

            Icon pageIcon;
            String emoji = page.getEmoji();
            if (emoji != null) {
                pageIcon = EmojiIcon.forUnicode(emoji);
            } else {
                pageIcon = EmojiIcon.PAGE_FACING_UP;
            }
            map.put(BaseTemplate.PROPERTY_ICON, pageIcon);
            map.put(BaseTemplate.PROPERTY_CAPTION, page.getTitle());
            map.put(BaseTemplate.PROPERTY_DESCRIPTION, page.getDescription());
            return map;
        };
    }

    @NotNull
    private PropertyProvider<EmojiIcon> getEmojiIconPropertyProvider() {

        return (emojiIcon, propertyNames) -> {
            Map<String, Object> map = new HashMap<>();
            map.put(BaseTemplate.PROPERTY_ICON, emojiIcon);
            map.put(BaseTemplate.PROPERTY_CAPTION, emojiIcon.getIconId());
            map.put(BaseTemplate.PROPERTY_DESCRIPTION, null);
            return map;
        };
    }


    @NotNull
    private ComboBoxModel<EmojiIcon> getEmojiIconComboBoxModel() {

        List<EmojiIcon> iconList = EmojiIcon.getIcons();

        return (s) -> iconList.stream()
                .filter(emojiIcon -> s == null || StringUtils.containsIgnoreCase(emojiIcon.getIconId(), s))
                .limit(100)
                .collect(Collectors.toList());
    }


}


