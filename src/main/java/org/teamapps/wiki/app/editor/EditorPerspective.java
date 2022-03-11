package org.teamapps.wiki.app.editor;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.perspective.AbstractApplicationPerspective;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.application.ux.form.FormWindow;
import org.teamapps.common.format.Color;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.databinding.MutableValue;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.icon.emoji.EmojiIcon;
import org.teamapps.icons.Icon;
import org.teamapps.icons.composite.CompositeIcon;
import org.teamapps.ux.application.layout.ExtendedLayout;
import org.teamapps.ux.application.perspective.Perspective;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.dialogue.Dialogue;
import org.teamapps.ux.component.field.Button;
import org.teamapps.ux.component.field.DisplayField;
import org.teamapps.ux.component.field.FieldEditingMode;
import org.teamapps.ux.component.field.TextField;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.field.richtext.RichTextEditor;
import org.teamapps.ux.component.flexcontainer.VerticalLayout;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.template.BaseTemplateRecord;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;
import org.teamapps.ux.component.tree.Tree;
import org.teamapps.ux.component.tree.TreeNodeInfoImpl;
import org.teamapps.ux.model.ComboBoxModel;
import org.teamapps.ux.model.ListTreeModel;
import org.teamapps.ux.session.CurrentSessionContext;
import org.teamapps.wiki.app.WikiUtils;
import org.teamapps.wiki.model.wiki.Book;
import org.teamapps.wiki.model.wiki.Chapter;
import org.teamapps.wiki.model.wiki.Page;

import java.util.*;
import java.util.stream.Collectors;

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
        contentView.getPanel().setStretchContent(false);


        ToolbarButtonGroup navigationButtonGroup = navigationView.addLocalButtonGroup(new ToolbarButtonGroup());
        ToolbarButton newPageButton = navigationButtonGroup.addButton(ToolbarButton.createTiny(CompositeIcon.of(EmojiIcon.PAGE_FACING_UP, EmojiIcon.PLUS), "New Page"));
        newPageButton.onClick.addListener(() -> {
            createNewPage(selectedChapter.get());
            // pageTreeModel.setRecords(selectedChapter.get().getPages());
            updateNavigationView();
        });

        navigationButtonGroup = navigationView.addLocalButtonGroup(new ToolbarButtonGroup());
        ToolbarButton upButton = navigationButtonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.UP_ARROW, ""));
        ToolbarButton downButton = navigationButtonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.DOWN_ARROW, ""));
        upButton.onClick.addListener(() -> reorderPage(selectedPage.get(), true));
        downButton.onClick.addListener(() -> reorderPage(selectedPage.get(), false));


        ToolbarButtonGroup buttonGroup = contentView.addLocalButtonGroup(new ToolbarButtonGroup());
        ToolbarButton saveButton = buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.CHECK_MARK_BUTTON, "Save Changes"));
        saveButton.setVisible(false);
        saveButton.onClick.addListener(() -> {
            editingModeEnabled.set(Boolean.FALSE);
            selectedPage.get().setContent(contentEditor.getValue());
            selectedPage.get().save();
            updateContentView(selectedPage.get());
        });

        ToolbarButton cancelButton = buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.CROSS_MARK, "Discard Changes"));
        cancelButton.setVisible(false);
        cancelButton.onClick.addListener(() -> {
            selectedPage.get().clearChanges();
            editingModeEnabled.set(!editingModeEnabled.get()); // switch on/off
            updateContentView();
        });

        ToolbarButton editButton = buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.MEMO, "Edit"));
        editButton.onClick.addListener(() -> {
            editingModeEnabled.set(!editingModeEnabled.get()); // switch on/off
        });

        ToolbarButton pageSettingsButton = buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.WRENCH, "Page Settings"));
        pageSettingsButton.onClick.addListener(() -> {
            showPageSettingsWindow(selectedPage.get());
        });

        selectedPage.onChanged().addListener(page -> {
            if (selectedPage.get() != null) {
                updateContentView(selectedPage.get());
                contentView.getPanel().setTitle(page.getTitle());
                contentView.focus();
            } else {
                selectedPage.set(selectedChapter.get().getPages().stream().findFirst().orElse(null));
                updateNavigationView();
            }
        });
        editingModeEnabled.onChanged().addListener(enabled -> {
            saveButton.setVisible(enabled);
            cancelButton.setVisible(enabled);
            editButton.setVisible(!enabled);
            updateContentView(selectedPage.get());
        });

        selectedBook.set(Book.getAll().stream().findFirst().orElse(null));
        selectedChapter.set(selectedBook.get().getChapters().stream().findFirst().orElse(null));
        selectedPage.set(selectedChapter.get().getPages().stream().findFirst().orElse(null));

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

        ComboBox<EmojiIcon> emojiIconComboBox = new ComboBox<>();
        List<EmojiIcon> iconList = EmojiIcon.getIcons();
        ComboBoxModel<EmojiIcon> iconModel = s -> iconList.stream()
                .filter(emojiIcon -> s == null || StringUtils.containsIgnoreCase(emojiIcon.getIconId(), s))
                .limit(100)
                .collect(Collectors.toList());
        emojiIconComboBox.setModel(iconModel);
        emojiIconComboBox.setTemplate(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
        emojiIconComboBox.setPropertyProvider((emojiIcon, propertyNames) -> {
            Map<String, Object> map = new HashMap<>();
            map.put(BaseTemplate.PROPERTY_ICON, emojiIcon);
            map.put(BaseTemplate.PROPERTY_CAPTION, emojiIcon.getIconId());
            map.put(BaseTemplate.PROPERTY_DESCRIPTION, null);
            return map;
        });
        emojiIconComboBox.setRecordToStringFunction(EmojiIcon::getIconId);
        emojiIconComboBox.setValue((page.getEmoji() != null) ? EmojiIcon.forUnicode(page.getEmoji()) : null);

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
        ListTreeModel<Page> pageListModel = new ListTreeModel<>(getPages(page.getChapter()));
        pageComboBox.setModel(pageListModel);
        pageComboBox.setTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES);
        pageComboBox.setPropertyProvider(getPagePropertyProvider());
        pageComboBox.setShowClearButton(true);
        pageComboBox.setValue(page.getParent());
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
        deleteButton.onClicked.addListener(() -> {
            Dialogue okCancel = Dialogue.createOkCancel(EmojiIcon.WARNING, "Permanently delete page \"" + page.getTitle() + "\"?", "Do you really want to delete this page?");
            okCancel.show();
            okCancel.onResult.addListener(isConfirmed -> {
                if (isConfirmed) {
                    page.delete();
                    selectedPage.set(null);
                    formWindow.close();
                } else { }
            });
        });

        saveButton.onClick.addListener(() -> {
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
            updateContentView();
            updateNavigationView();
            selectedPage.set(page); // update views
            formWindow.close();
        });

        formWindow.show();
    }

    private void updateContentView() {
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
        if (description != null) {
            descriptionField.setValue("<p>" + description + "</p>");
            descriptionField.setShowHtml(true);
            contentVerticalLayout.addComponent(descriptionField);
        }

        if (editingModeEnabled.get()) {
            contentEditor = new RichTextEditor();
            contentEditor.setValue(page.getContent());
            contentEditor.setEditingMode(FieldEditingMode.EDITABLE);
            contentVerticalLayout.addComponent(contentEditor);
            contentEditor.onValueChanged.addListener(page::setContent); // set content, but not saved
            // reset changes: page.clearChanges();

//            page.getContentBlocks().forEach(contentBlock -> {
//                switch (contentBlock.getContentBlockType()) {
//                    case RICH_TEXT -> {
//                        RichTextEditor richTextEditor = new RichTextEditor();
//                        richTextEditor.setValue(contentBlock.getValue());
//                        richTextEditor.setEditingMode(FieldEditingMode.EDITABLE);
//                        richTextEditor.setDebuggingId(String.valueOf(contentBlock.getId()));
//                        contentVerticalLayout.addComponent(richTextEditor);
//                    }
//                }
//            });
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
        chapterComboBox.setValue(selectedChapter.get());
        chapterComboBox.onValueChanged.addListener(selectedChapter::set);
        navigationLayout.addComponent(chapterComboBox);

        ListTreeModel<Page> pageTreeModel = new ListTreeModel<>(Collections.EMPTY_LIST);
        pageTreeModel.setRecords(getPages(selectedChapter.get()));
        Tree<Page> pageTree = new Tree<>(pageTreeModel);
        pageTreeModel.setTreeNodeInfoFunction(page -> new TreeNodeInfoImpl<>(page.getParent(), WikiUtils.getPageLevel(page) == 0, true, false));
        pageTree.setOpenOnSelection(true);
        pageTree.setEntryTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES);
        pageTree.setPropertyProvider(getPagePropertyProvider());
        pageTree.setSelectedNode(selectedPage.get());

        pageTree.onNodeSelected.addListener(selectedPage::set);
        navigationLayout.addComponent(pageTree);

        selectedBook.onChanged().addListener(book -> {
            chapterListTreeModel.setRecords(book.getChapters());
            selectedChapter.set(book.getChapters().stream().findFirst().orElse(null));
        });
        selectedChapter.onChanged().addListener(chapter -> {
            chapterComboBox.setValue(chapter);
            selectedPage.set(getPages(chapter).stream().findFirst().orElse(null));
            pageTreeModel.setRecords(selectedChapter.get().getPages());
        });

        navigationView.setComponent(navigationLayout);
    }

    // List with correct order of children
    private List<Page> getPages(Chapter chapter) {
        List<Page> pageList = new ArrayList<>();
        List<Page> topLevelPages = chapter.getPages().stream().filter(page -> page.getParent() == null).collect(Collectors.toList());
        addPageNodes(topLevelPages, pageList);
        return pageList;
    }
    private void addPageNodes(List<Page> nodes, List<Page> pageNodes) {
        for (Page node : nodes) {
            pageNodes.add(node);
            addPageNodes(node.getChildren(), pageNodes);
        }
    }

    @NotNull
    private PropertyProvider<Page> getPagePropertyProvider() {
        PropertyProvider<Page> pagePropertyProvider = (page, propertyNames) -> {
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
        return pagePropertyProvider;
    }

    private Page createNewPage(Chapter chapter) {
        Page new_page = Page.create()
                .setParent(selectedPage.get().getParent())
                .setTitle("New Page")
                .setDescription("")
                .setChapter(chapter)
                .setContent("<h2>Title</h2><p>Text</p>")
                .save();
        showPageSettingsWindow(new_page);
        selectedPage.set(new_page);
        editingModeEnabled.set(true);
        return new_page;
    }

    private void reorderPage(Page page, boolean up) {
        if (page == null) {
            return;
        }
        if (page.getParent() == null) {
            ArrayList<Page> pageList = new ArrayList<>(getPages(page.getChapter()));
            int pos = 0;
            for (Page node : pageList) {
                if (node.equals(page)) {
                    break;
                }
                pos++;
            }
            if ((up && pos == 0) || (!up && pos + 1 == pageList.size())) {
                return;
            }
            int newPos = up ? pos - 1 : pos + 1;
            Collections.swap(pageList, pos, newPos);
            page.getChapter().setPages(pageList).save();
        } else {
            Page parent = page.getParent();
            ArrayList<Page> pageList = new ArrayList<>(parent.getChildren());
            int pos = 0;
            for (Page node : pageList) {
                if (node.equals(page)) {
                    break;
                }
                pos++;
            }
            if ((up && pos == 0) || (!up && pos + 1 == pageList.size())) {
                return;
            }
            int newPos = up ? pos - 1 : pos + 1;
            Collections.swap(pageList, pos, newPos);
            parent.setChildren(pageList).save();
        }
        // PageTreeModel.setRecords(getPages(selectedPage.get()));
        updateNavigationView();
    }

}
