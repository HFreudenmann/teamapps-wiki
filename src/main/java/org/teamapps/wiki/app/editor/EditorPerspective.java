package org.teamapps.wiki.app.editor;

import org.jetbrains.annotations.NotNull;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.perspective.AbstractApplicationPerspective;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.common.format.Color;
import org.teamapps.databinding.MutableValue;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.icon.emoji.EmojiIcon;
import org.teamapps.ux.application.layout.ExtendedLayout;
import org.teamapps.ux.application.perspective.Perspective;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.Component;
import org.teamapps.ux.component.field.DisplayField;
import org.teamapps.ux.component.field.FieldEditingMode;
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
    private final TwoWayBindableValue<Boolean> editingMode = TwoWayBindableValue.create(Boolean.FALSE);

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

        ToolbarButtonGroup buttonGroup = contentView.addLocalButtonGroup(new ToolbarButtonGroup());
        buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.CHECK_MARK_BUTTON, "Save Changes")).onClick.addListener(() -> {
            editingMode.set(Boolean.FALSE);
            selectedPage.get().save();
        });
        buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.WRITING_HAND, "Edit")).onClick.addListener(() -> {
            editingMode.set(Boolean.TRUE);
        });

        selectedBook.set(Book.getAll().stream().findFirst().orElse(Book.create().setTitle("New Book")));
        selectedChapter.set(selectedBook.get().getChapters().stream().findFirst().orElse(Chapter.create().setBook(selectedBook.get()).setTitle("Chapter 1")));

        selectedPage.onChanged().addListener(page -> {
            updateContentView(selectedPage.get());
            contentView.getPanel().setTitle(page.getTitle());
            contentView.focus();
        });
        editingMode.onChanged().addListener(() -> updateContentView(selectedPage.get()));

        navigationView.setComponent(createNavigationView());
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
        descriptionField.setValue("<p>" + page.getDescription() + "</p>");
        descriptionField.setShowHtml(true);
        contentVerticalLayout.addComponent(descriptionField);

        if (editingMode.get()){
            page.getContentBlocks().forEach(contentBlock -> {
                switch (contentBlock.getContentBlockType()) {
                    case RICH_TEXT -> {
                        RichTextEditor richTextEditor = new RichTextEditor();
                        richTextEditor.setValue(contentBlock.getValue());
                        richTextEditor.setEditingMode(FieldEditingMode.EDITABLE_IF_FOCUSED);
                        richTextEditor.setDebuggingId(String.valueOf(contentBlock.getId()));
                        contentVerticalLayout.addComponent(richTextEditor);
                    }
                }
            });
        } else {
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

    private Component createNavigationView() {
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

        pageTree.onNodeSelected.addListener(page -> {
            selectedPage.set(page);
        });
        navigationLayout.addComponent(pageTree);

        selectedBook.bindWritingTo(book -> {
            chapterListTreeModel.setRecords(book.getChapters());
            selectedChapter.set(book.getChapters().stream().findFirst().orElse(Chapter.create().setTitle("new Chapter")));
        });
        selectedChapter.bindWritingTo(chapter -> {
            chapterComboBox.setValue(chapter);
            pageTreeModel.setRecords(selectedChapter.get().getPages());
        });


        ToolbarButtonGroup buttonGroup = navigationView.addLocalButtonGroup(new ToolbarButtonGroup());
        buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.PLUS, "New Page")).onClick.addListener(() -> {
            Page parent = null; // selectedPage.get();
            Page.create()
                    .setParent(parent)
                    .setTitle("New Page")
                    .setDescription("")
                    .setChapter(selectedChapter.get())
                    .setContentBlocks(ContentBlock.create().setContentBlockType(ContentBlockType.RICH_TEXT).setValue("<h2>Title</h2><p>Text</p>"))
                    .save();
            pageTreeModel.setRecords(selectedChapter.get().getPages());
        });

        return navigationLayout;
    }

}
