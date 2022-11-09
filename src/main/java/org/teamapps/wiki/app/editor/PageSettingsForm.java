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
import org.teamapps.ux.component.field.MultiLineTextField;
import org.teamapps.ux.component.field.TextField;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.form.ResponsiveFormSection;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.template.BaseTemplateRecord;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.window.Window;
import org.teamapps.ux.model.ComboBoxModel;
import org.teamapps.ux.model.ListTreeModel;
import org.teamapps.ux.session.CurrentSessionContext;
import org.teamapps.wiki.app.PageTreeUtils;
import org.teamapps.wiki.app.WikiUtils;
import org.teamapps.wiki.model.wiki.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PageSettingsForm {

    private FormWindow formWindow;

    private TextField pageTitleField;
    private MultiLineTextField pageDescriptionField;
    private ComboBox<EmojiIcon> emojiIconComboBox;
    private ComboBox<Page> pageComboBox;

    ResponsiveFormSection deleteSection;
    @SuppressWarnings("rawtypes")
    Button<BaseTemplateRecord> deleteButton;

    private Page page;


    public void create(ApplicationInstanceData applicationInstanceData,
                       ListTreeModel<Page> pageListModel,
                       Function<Page, Void> onPageSettingsSave,
                       Runnable onPageSettingsCancel,
                       Runnable onPageSettingsPageDelete) {

        formWindow = new FormWindow(EmojiIcon.GEAR, "Page Settings", applicationInstanceData);

        Window window = formWindow.getWindow();
        window.setModal(true);
        window.setCloseable(false);
        window.setMaximizable(true);
        window.setCloseOnEscape(false);
        window.setCloseOnClickOutside(false);
        window.setModalBackgroundDimmingColor(Color.MATERIAL_BLUE_100.withAlpha(0.4f));

        ToolbarButton saveButton = formWindow.addSaveButton();
        ToolbarButton cancelButton = formWindow.addCancelButton();

        pageTitleField = new TextField();
        pageDescriptionField = new MultiLineTextField();
        pageDescriptionField.setMaxCharacters(999);
        pageDescriptionField.setShowClearButton(true);
//      WORKAROUND: setMaxHeight seems to have no effect
//                  --> auto adjust the height
        pageDescriptionField.setAdjustHeightToContent(true);

        emojiIconComboBox = new ComboBox<>();
        emojiIconComboBox.setModel(getEmojiIconComboBoxModel());
        emojiIconComboBox.setTemplate(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
        emojiIconComboBox.setPropertyProvider(getEmojiIconPropertyProvider());
        emojiIconComboBox.setRecordToStringFunction(EmojiIcon::getIconId);

        pageComboBox = new ComboBox<>();
        pageComboBox.setModel(pageListModel);
        pageComboBox.setTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES);
        pageComboBox.setPropertyProvider(getPagePropertyProvider());
        pageComboBox.setShowClearButton(true);
        pageComboBox.setRecordToStringFunction(chapter -> chapter.getTitle() + " - " + chapter.getDescription());

        pageListModel.setTreeNodeInfoFunction(WikiUtils.getPageTreeNodeInfoFunction());

        formWindow.addSection();
        formWindow.addField("Page Icon", emojiIconComboBox);
        formWindow.addField("Page Title", pageTitleField);
        formWindow.addField("Page Description", pageDescriptionField);
        formWindow.addSection(EmojiIcon.CARD_INDEX_DIVIDERS, "Placement");
        formWindow.addField("Parent Page", pageComboBox);

        deleteSection = formWindow.getFormLayout().addSection(EmojiIcon.WARNING, "Delete page").setCollapsed(true);
        deleteButton = Button.create(EmojiIcon.WASTEBASKET, "DELETE PAGE PERMANENTLY").setColor(Color.MATERIAL_ORANGE_600);
        formWindow.addField("", deleteButton);
//      Alternative: left adjusted position of the button without (empty) label
//        formWindow.getFormLayout().addLabelComponent(deleteButton);

        deleteButton.onClicked.addListener(onDeletePageClicked(onPageSettingsPageDelete));
        saveButton.onClick.addListener(onSavePageClicked(onPageSettingsSave));
        cancelButton.onClick.addListener(onCancelPageClicked(onPageSettingsCancel));
    }


    public void close() {
        System.out.println("   PSF.close");
        formWindow.close();
    }

    public void show(Page editPage, ListTreeModel<Page> pageListModel, boolean isNewPage) {

        System.out.println("   PSF.show");
        this.page = editPage;

        boolean isDeleteButtonAvailable = !isNewPage;

//      WORKAROUND:
//         deleteSection.setVisible has no effect in org.teamapps:teamapps-ux, v0.9.159!!!
//         Hence we must set the delete button invisible and set deleteSection.setHideWhenNoVisibleFields(true) in order
//         to hide the delete section.
//      deleteSection.setVisible(isDeleteButtonAvailable);
        deleteSection.setHideWhenNoVisibleFields(true);
        deleteButton.setVisible(isDeleteButtonAvailable);

        pageListModel.setTreeNodeInfoFunction(WikiUtils.getPageTreeNodeInfoFunction());

        pageTitleField.setValue(page.getTitle());
        pageDescriptionField.setValue(page.getDescription());
        emojiIconComboBox.setValue(WikiUtils.getIconFromName(page.getEmoji()));
        pageComboBox.setModel(pageListModel);
        pageComboBox.setValue(page.getParent());

        formWindow.show();
    }



    @NotNull
    private Runnable onDeletePageClicked(Runnable onPageSettingsPageDelete) {
        return () -> {
            System.out.println("PSF.onDeletePageClicked");

            Dialogue okCancel = Dialogue.createOkCancel(
                    EmojiIcon.WARNING,
                    "Delete confirmation",
                    "Page: '" + page.getTitle() +  "'<br>Do you really want to DELETE this page PERMANENTLY?");
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
            System.out.println("PSF.onSavePageClicked");

            page.setTitle(pageTitleField.getValue());
            page.setDescription(pageDescriptionField.getValue());

            Page newParent = pageComboBox.getValue();
            if (PageTreeUtils.isChildPage(newParent, page)) {
                CurrentSessionContext.get().showNotification(EmojiIcon.PROHIBITED, "Invalid new Parent will be ignored!");
                newParent = page.getParent();
            }
            page.setParent(page.equals(newParent) ? null : newParent);
            page.setEmoji(emojiIconComboBox.getValue() != null ? emojiIconComboBox.getValue().getUnicode() : null);
            page.save();

            onPageSettingsSave.apply(page);
            formWindow.close();
        };
    }

    @NotNull
    private Runnable onCancelPageClicked(Runnable onPageSettingsCancel) {
        return () -> {
            System.out.println("PSF.onCancelPageClicked");
            onPageSettingsCancel.run();
        };
    }


    private PropertyProvider<Page> getPagePropertyProvider() {

        return (page, propertyNames) -> {
            Map<String, Object> map = new HashMap<>();

            Icon<EmojiIcon, ?> pageIcon;
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
                .limit(70)
                .collect(Collectors.toList());
    }

}


