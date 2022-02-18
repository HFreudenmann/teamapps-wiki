package org.teamapps.wiki.app.editor;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.perspective.AbstractApplicationPerspective;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.application.ux.form.FormWindow;
import org.teamapps.common.format.Color;
import org.teamapps.databinding.MutableValue;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.icon.emoji.EmojiIcon;
import org.teamapps.ux.application.layout.ExtendedLayout;
import org.teamapps.ux.application.perspective.Perspective;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.field.DisplayField;
import org.teamapps.ux.component.field.FieldEditingMode;
import org.teamapps.ux.component.field.TextField;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.field.richtext.RichTextEditor;
import org.teamapps.ux.component.flexcontainer.VerticalLayout;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;
import org.teamapps.ux.component.tree.Tree;
import org.teamapps.ux.component.tree.TreeNodeInfoImpl;
import org.teamapps.ux.model.ListTreeModel;
import org.teamapps.wiki.app.WikiUtils;
import org.teamapps.wiki.model.wiki.*;

import java.util.HashMap;
import java.util.Map;

public class EditorPerspective extends AbstractApplicationPerspective {

    private View navigationView;
    private View contentView;
    private final TwoWayBindableValue<Book> selectedBook = TwoWayBindableValue.create();
    private final TwoWayBindableValue<Chapter> selectedChapter = TwoWayBindableValue.create();
    private final TwoWayBindableValue<Page> selectedPage = TwoWayBindableValue.create();
    private final TwoWayBindableValue<Boolean> editingModeEnabled = TwoWayBindableValue.create(Boolean.FALSE);
    private RichTextEditor contentEditor;

    public EditorPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
        super(applicationInstanceData, perspectiveInfoBadgeValue);
        PerspectiveSessionData perspectiveSessionData = (PerspectiveSessionData) getApplicationInstanceData();

        createUi();
    }

    private void createUi() {
        Perspective perspective = getPerspective();
        navigationView = perspective.addView(View.createView(ExtendedLayout.LEFT, EmojiIcon.COMPASS, "Book Navigation", null));
        contentView = perspective.addView(View.createView(ExtendedLayout.CENTER, EmojiIcon.PAGE_FACING_UP, "Inhalt", null));

        navigationView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.84f));
        contentView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.84f));
        contentView.getPanel().setPadding(10);

        ToolbarButtonGroup navigationButtonGroup = navigationView.addLocalButtonGroup(new ToolbarButtonGroup());
        navigationButtonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.PLUS, "New Page")).onClick.addListener(() -> {
            createNewPage(selectedChapter.get());
            // pageTreeModel.setRecords(selectedChapter.get().getPages());
            updateNavigationView();
        });

        ToolbarButtonGroup buttonGroup = contentView.addLocalButtonGroup(new ToolbarButtonGroup());
        ToolbarButton saveButton = ToolbarButton.createTiny(EmojiIcon.CHECK_MARK_BUTTON, "Save Changes");
        saveButton.setVisible(false);
        buttonGroup.addButton(saveButton).onClick.addListener(() -> {
            editingModeEnabled.set(Boolean.FALSE);
            selectedPage.get().setContent(contentEditor.getValue());
            selectedPage.get().save();
            updateContentView(selectedPage.get());
        });
        buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.WRITING_HAND, "Edit")).onClick.addListener(() -> {
            editingModeEnabled.set(!editingModeEnabled.get()); // switch on/off
        });
        buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.GEAR, "Page Settings")).onClick.addListener(() -> {
            showPageSettingsWindow(selectedPage.get());
        });

        selectedBook.set(Book.getAll().stream().findFirst().orElse(Book.create().setTitle("New Book")));
        selectedChapter.set(selectedBook.get().getChapters().stream().findFirst().orElse(null)); // Chapter.create().setBook(selectedBook.get()).setTitle("Chapter 1")

        selectedPage.onChanged().addListener(page -> {
            if (selectedPage.get() != null ) {
                updateContentView(selectedPage.get());
                contentView.getPanel().setTitle(page.getTitle());
                contentView.focus();
            } else {
                updateNavigationView();
            }
        });
        editingModeEnabled.onChanged().addListener(enabled -> {
            saveButton.setVisible(enabled);
            updateContentView(selectedPage.get());
        });
        updateNavigationView();
    }

    private void showPageSettingsWindow(Page page) {

        FormWindow formWindow = new FormWindow(EmojiIcon.GEAR, "Page Settings", getApplicationInstanceData());
        ToolbarButton saveButton = formWindow.addSaveButton();
        formWindow.addCancelButton();

        TextField pageTitleField = new TextField();
        TextField pageDescriptionField = new TextField();

        pageTitleField.setValue(page.getTitle());
        pageDescriptionField.setValue(page.getDescription());

//        ComboBox<Chapter> chapterComboBox = new ComboBox<>();
//        ListTreeModel<Chapter> chapterListTreeModel = new ListTreeModel<>(selectedBook.get().getChapters());
//        chapterComboBox.setModel(chapterListTreeModel);
//        chapterComboBox.setTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES);
//        chapterComboBox.setPropertyProvider((chapter, propertyNames) -> {
//            Map<String, Object> map = new HashMap<>();
//            map.put(BaseTemplate.PROPERTY_ICON, EmojiIcon.OPEN_BOOK);
//            map.put(BaseTemplate.PROPERTY_CAPTION, chapter.getTitle());
//            map.put(BaseTemplate.PROPERTY_DESCRIPTION, chapter.getDescription());
//            return map;
//        });
//        chapterComboBox.setValue(page.getChapter());
//        chapterComboBox.setRecordToStringFunction(chapter -> chapter.getTitle() + " - " + chapter.getDescription());

        ComboBox<Page> pageComboBox = new ComboBox<>();
        ListTreeModel<Page> pageListModel = new ListTreeModel<>(page.getChapter().getPages());
        pageComboBox.setModel(pageListModel);
        pageComboBox.setTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES);
        pageComboBox.setPropertyProvider((p, propertyNames) -> {
            Map<String, Object> map = new HashMap<>();
            map.put(BaseTemplate.PROPERTY_ICON, EmojiIcon.PAGE_FACING_UP);
            map.put(BaseTemplate.PROPERTY_CAPTION, p.getTitle());
            map.put(BaseTemplate.PROPERTY_DESCRIPTION, p.getDescription());
            return map;
        });
        pageComboBox.setShowClearButton(true);
        pageComboBox.setValue(page.getParent());
        pageListModel.setTreeNodeInfoFunction(p -> new TreeNodeInfoImpl<>(p.getParent(), WikiUtils.getPageLevel(p) == 0, true, false));
        pageComboBox.setRecordToStringFunction(chapter -> chapter.getTitle() + " - " + chapter.getDescription());

        formWindow.addSection();
        formWindow.addField("Page Title", pageTitleField);
        formWindow.addField("Page Description", pageDescriptionField);
        formWindow.addSection(EmojiIcon.CARD_INDEX_DIVIDERS, "Placement");
        formWindow.addField("Parent Page", pageComboBox);

        // formWindow.addSection(EmojiIcon.WARNING, "Danger Zone");
        ToolbarButton deletePageButton = ToolbarButton.create(EmojiIcon.CROSS_MARK, "Delete Page", null);
        deletePageButton.onClick.addListener(() -> {
            // Todo ConfirmationDialog
            page.delete();
            selectedPage.set(null);
            formWindow.close();
        });
        formWindow.addButtonGroup().addButton(deletePageButton);
        formWindow.show();

        saveButton.onClick.addListener(() -> {
            page.setTitle(pageTitleField.getValue());
            page.setDescription(pageDescriptionField.getValue());

            Page newParent = pageComboBox.getValue();
            if (newParent.equals(page)) { newParent = null; }
            page.setParent(newParent);
            page.save();
            updateContentView();
            updateNavigationView();
            selectedPage.set(page); // update views
            formWindow.close();
        });

    }

    private void updateContentView(){
        updateContentView(selectedPage.get());
    }
    private void updateContentView(Page page) {
        VerticalLayout contentVerticalLayout = new VerticalLayout();

        contentView.getPanel().setTitle(page.getTitle());
        contentView.focus();

        DisplayField titleField = new DisplayField();
        titleField.setValue("<h1>" + page.getTitle() + "</h1>");
        titleField.setShowHtml(true);
        contentVerticalLayout.addComponent(titleField);

        DisplayField descriptionField = new DisplayField();

        String description = page.getDescription();
        if (description != null ) {
            descriptionField.setValue("<p>" + description + "</p>");
            descriptionField.setShowHtml(true);
            contentVerticalLayout.addComponent(descriptionField);
        }

        if (editingModeEnabled.get()){
            contentEditor = new RichTextEditor();
            contentEditor.setValue(page.getContent());
            contentEditor.setEditingMode(FieldEditingMode.EDITABLE);
            contentVerticalLayout.addComponent(contentEditor);

            page.getContentBlocks().forEach(contentBlock -> {
                switch (contentBlock.getContentBlockType()) {
                    case RICH_TEXT -> {
                        RichTextEditor richTextEditor = new RichTextEditor();
                        richTextEditor.setValue(contentBlock.getValue());
                        richTextEditor.setEditingMode(FieldEditingMode.EDITABLE);
                        richTextEditor.setDebuggingId(String.valueOf(contentBlock.getId()));
                        contentVerticalLayout.addComponent(richTextEditor);
                    }
                }
            });
        } else {

            DisplayField contentDisplay = new DisplayField();
            contentDisplay.setValue(page.getContent());
            contentDisplay.setShowHtml(true);
            contentVerticalLayout.addComponent(contentDisplay);

            page.getContentBlocks().forEach(contentBlock -> {
                switch (contentBlock.getContentBlockType()) {
                    case RICH_TEXT -> {
                        DisplayField contentBlockField = new DisplayField();
                        contentBlockField.setValue(contentBlock.getValue());
                        contentBlockField.setShowHtml(true);
                        contentVerticalLayout.addComponent(contentBlockField);
                    }
                }

                });

        }
        contentView.setComponent(contentVerticalLayout);
    }

    private void updateNavigationView() {
        VerticalLayout navigationLayout = new VerticalLayout();
        ComboBox<Book> bookComboBox = new ComboBox<>();
        bookComboBox.setModel(new ListTreeModel<Book>(Book.getAll()));
        bookComboBox.setTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES);
        bookComboBox.setPropertyProvider((book, propertyNames) -> {
            Map<String, Object> map = new HashMap<>();
            map.put(BaseTemplate.PROPERTY_ICON, EmojiIcon.CLOSED_BOOK);
            map.put(BaseTemplate.PROPERTY_CAPTION, book.getTitle());
            map.put(BaseTemplate.PROPERTY_DESCRIPTION, book.getDescription());
            return map;
        });
        bookComboBox.setRecordToStringFunction(book -> book.getTitle() + " - " + book.getDescription());
        bookComboBox.setValue(selectedBook.get());
        bookComboBox.onValueChanged.addListener(selectedBook::set);
        navigationLayout.addComponent(bookComboBox);

        ComboBox<Chapter> chapterComboBox = new ComboBox<>();
        ListTreeModel<Chapter> chapterListTreeModel = new ListTreeModel<>(selectedBook.get().getChapters());
        chapterComboBox.setModel(chapterListTreeModel);
        chapterComboBox.setTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES);
        chapterComboBox.setPropertyProvider((chapter, propertyNames) -> {
            Map<String, Object> map = new HashMap<>();
            map.put(BaseTemplate.PROPERTY_ICON, EmojiIcon.OPEN_BOOK);
            map.put(BaseTemplate.PROPERTY_CAPTION, chapter.getTitle());
            map.put(BaseTemplate.PROPERTY_DESCRIPTION, chapter.getDescription());
            return map;
        });
        chapterComboBox.setRecordToStringFunction(chapter -> chapter.getTitle() + " - " + chapter.getDescription());
        chapterComboBox.onValueChanged.addListener(selectedChapter::set);
        navigationLayout.addComponent(chapterComboBox);

        ListTreeModel<Page> pageTreeModel = new ListTreeModel<>(selectedChapter.get().getPages());
        Tree<Page> pageTree = new Tree<>(pageTreeModel);
        pageTreeModel.setTreeNodeInfoFunction(page -> new TreeNodeInfoImpl<>(page.getParent(), WikiUtils.getPageLevel(page) == 0, true, false));
        pageTree.setOpenOnSelection(true);
        pageTree.setEntryTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES);
        pageTree.setPropertyProvider((page, propertyNames) -> {
            Map<String, Object> map = new HashMap<>();
            map.put(BaseTemplate.PROPERTY_ICON, EmojiIcon.PAGE_FACING_UP);
            map.put(BaseTemplate.PROPERTY_CAPTION, page.getTitle());
            map.put(BaseTemplate.PROPERTY_DESCRIPTION, page.getDescription());
            return map;
        });

        pageTree.onNodeSelected.addListener(selectedPage::set);
        navigationLayout.addComponent(pageTree);

        selectedBook.bindWritingTo(book -> {
            chapterListTreeModel.setRecords(book.getChapters());
            selectedChapter.set(book.getChapters().stream().findFirst().orElse(null));
        });
        selectedChapter.bindWritingTo(chapter -> {
            chapterComboBox.setValue(chapter);
            selectedPage.set(chapter.getPages().stream().findFirst().orElse(null));
            pageTreeModel.setRecords(selectedChapter.get().getPages());
        });

        navigationView.setComponent(navigationLayout);
    }

    private Page createNewPage(Chapter chapter) {
        Page new_page = Page.create()
                .setParent(null)
                .setTitle("New Page")
                .setDescription("")
                .setChapter(chapter)
                .setContentBlocks(ContentBlock.create().setContentBlockType(ContentBlockType.RICH_TEXT).setValue("<h2>Title</h2><p>Text</p>"))
                .save();
        showPageSettingsWindow(new_page);
        editingModeEnabled.set(true);
        return new_page;
    }

}
