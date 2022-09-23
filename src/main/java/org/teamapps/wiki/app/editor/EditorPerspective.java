package org.teamapps.wiki.app.editor;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.perspective.AbstractApplicationPerspective;
import org.teamapps.application.api.user.SessionUser;
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
import org.teamapps.ux.application.view.ViewSize;
import org.teamapps.ux.component.absolutelayout.Length;
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
import org.teamapps.wiki.app.WikiApplicationBuilder;
import org.teamapps.wiki.app.WikiPageManager;
import org.teamapps.wiki.app.WikiUtils;
import org.teamapps.wiki.model.wiki.Book;
import org.teamapps.wiki.model.wiki.Chapter;
import org.teamapps.wiki.model.wiki.Page;

import java.util.*;
import java.util.stream.Collectors;

public class EditorPerspective extends AbstractApplicationPerspective {

    private final WikiPageManager pageManager;
    private final SessionUser user;
    private View navigationView;
    private VerticalLayout navigationLayout;
    ComboBox<Book> bookComboBox;
    ComboBox<Chapter> chapterComboBox;
    Tree<Page> pageTree;

    ListTreeModel<Book> bookModel;
    ListTreeModel<Chapter> chapterModel;
    ListTreeModel<Page> pageModel;

    private View contentView;
    private final TwoWayBindableValue<Book> selectedBook = TwoWayBindableValue.create();
    private final TwoWayBindableValue<Chapter> selectedChapter = TwoWayBindableValue.create();
    private final TwoWayBindableValue<Page> selectedPage = TwoWayBindableValue.create();
    private final TwoWayBindableValue<Boolean> editingModeEnabled = TwoWayBindableValue.create(Boolean.FALSE);
    private RichTextEditor contentEditor;
    private Page emptyPage;

    public EditorPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
        super(applicationInstanceData, perspectiveInfoBadgeValue);
        PerspectiveSessionData perspectiveSessionData = (PerspectiveSessionData) getApplicationInstanceData();
        pageManager = WikiApplicationBuilder.PAGE_MANAGER;
        user = perspectiveSessionData.getUser();
        createUi();
    }

    private void createUi() {
        Perspective perspective = getPerspective();

        createNavigationLayout();
        createBookNavigationView(perspective);
        createBookContentView(perspective);

        initializeTwoWayBindables();

        emptyPage = Page.create()
                        .setParent(null).setTitle("").setDescription("")
                        .setChapter(null).setContent("");

        selectedBook.set(Book.getAll().stream().findFirst().orElse(null));
        selectedChapter.set(selectedBook.get().getChapters().stream().findFirst().orElse(null));
        selectedPage.set(selectedChapter.get().getPages().stream().findFirst().orElse(null));

//        updateNavigationView();
    }

    private void createNavigationLayout() {

        System.out.println("createNavigationLayout()");

        bookModel = new ListTreeModel<>(Book.getAll());
        chapterModel = new ListTreeModel<Chapter>(Collections.EMPTY_LIST);
        pageModel = new ListTreeModel<Page>(Collections.EMPTY_LIST);
        pageModel.setTreeNodeInfoFunction(
                page -> new TreeNodeInfoImpl<>(page.getParent(), WikiUtils.getPageLevel(page) == 0,
                                    true, false));

        navigationLayout = new VerticalLayout();

        bookComboBox = new ComboBox<>();
        bookComboBox.setModel(bookModel);
        bookComboBox.setTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES);
        bookComboBox.setPropertyProvider((book, propertyNames) -> {
            Map<String, Object> map = new HashMap<>();
            map.put(BaseTemplate.PROPERTY_ICON, EmojiIcon.CLOSED_BOOK);
            map.put(BaseTemplate.PROPERTY_CAPTION, book.getTitle());
            map.put(BaseTemplate.PROPERTY_DESCRIPTION, book.getDescription());
            return map;
        });
        bookComboBox.setRecordToStringFunction(book -> book.getTitle() + " - " + book.getDescription());
//        bookComboBox.setValue(selectedBook.get());
        bookComboBox.onValueChanged.addListener(selectedBook::set);

        chapterComboBox = new ComboBox<>();
//            ListTreeModel<Chapter> chapterListTreeModel = new ListTreeModel<>(selectedBook.get().getChapters());
        chapterComboBox.setModel(chapterModel);
        chapterComboBox.setTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES);
        chapterComboBox.setPropertyProvider((chapter, propertyNames) -> {
            Map<String, Object> map = new HashMap<>();
            map.put(BaseTemplate.PROPERTY_ICON, EmojiIcon.OPEN_BOOK);
            map.put(BaseTemplate.PROPERTY_CAPTION, chapter.getTitle());
            map.put(BaseTemplate.PROPERTY_DESCRIPTION, chapter.getDescription());
            return map;
        });
        chapterComboBox.setRecordToStringFunction(chapter -> chapter.getTitle() + " - " + chapter.getDescription());
//            chapterComboBox.setValue(selectedChapter.get());
        chapterComboBox.onValueChanged.addListener(chapter -> {
            selectedChapter.set(chapter);
// ToDo update only chapters and pages - but not books
//          updateNavigationView();
        });

//        pageModel = new ListTreeModel<Page>(Collections.EMPTY_LIST);
//            Chapter currentSelectedChapter = selectedChapter.get();
//            System.out.println("currentSelectedChapter = " + currentSelectedChapter.getTitle());
//            pageTreeModel.setRecords(getPages(currentSelectedChapter));
//        pageTreeModel.setTreeNodeInfoFunction(page -> new TreeNodeInfoImpl<>(page.getParent(),
//                WikiUtils.getPageLevel(page) == 0, true, false));
        pageTree = new Tree<>(pageModel);
        pageTree.setOpenOnSelection(true);
        pageTree.setEntryTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES);
        pageTree.setPropertyProvider(getPagePropertyProvider());
//            pageTree.setSelectedNode(selectedPage.get());
        pageTree.onNodeSelected.addListener(selectedPage::set);

        navigationLayout.addComponent(bookComboBox);
        navigationLayout.addComponent(chapterComboBox);
        navigationLayout.addComponent(pageTree);

//        selectedBook.onChanged().addListener(book -> {
//            System.out.println("selectedBook.onChanged() : " + book.getTitle());
//                chapterListTreeModel.setRecords(book.getChapters());
//                selectedChapter.set(book.getChapters().stream().findFirst().orElse(null));
//
//        });
//        selectedChapter.onChanged().addListener(chapter -> {
//            System.out.println("selectedChapter.onChanged() : " + chapter.getTitle());
//                chapterComboBox.setValue(chapter);
//                selectedPage.set(getPages(chapter).stream().findFirst().orElse(null));
//                pageTreeModel.setRecords(selectedChapter.get().getPages());
//        });

        //        navigationView.setComponent(navigationLayout);
    }

    private void initializeTwoWayBindables() {

        selectedBook.onChanged().addListener(book -> {
            if (Objects.nonNull(book)) {
                System.out.println("selectedBook.onChanged() : " + book.getTitle());
                bookComboBox.setValue(book);

                chapterModel.setRecords(book.getChapters());
                selectedChapter.set(book.getChapters().stream().findFirst().orElse(null));
            } else {
                System.out.println("selectedBook.onChanged() : (null)");

                chapterModel.setRecords(Collections.EMPTY_LIST);
                selectedChapter.set(null);
            }
        });
        selectedChapter.onChanged().addListener(chapter -> {
            if (Objects.nonNull(chapter)) {
                System.out.println("selectedChapter.onChanged() : " +  chapter.getTitle());
                chapterComboBox.setValue(chapter);

                pageModel.setRecords(selectedChapter.get().getPages());
                selectedPage.set(getPages(chapter).stream().findFirst().orElse(null));
            } else {
                System.out.println("selectedChapter.onChanged() : (null)");
                chapterComboBox.setValue(null);

                pageModel.setRecords(Collections.EMPTY_LIST);
                selectedPage.set(null);
            }
        });
        selectedPage.onChanged().addListener(page -> {
            if (Objects.nonNull(page)) {
                System.out.println("selectedPage.onChanged() : id/title " + page.getId() + "/" + page.getTitle());

                updateContentView(page);
                contentView.getPanel().setTitle(page.getTitle());
                WikiPageManager.PageStatus pageStatus = pageManager.getPageStatus(page);
                editingModeEnabled.set((pageStatus.isLocked() && pageStatus.getEditor().equals(user)));
                contentView.focus();
            } else {
                System.out.println("selectedPage.onChanged() : (null)");

                updateContentView(emptyPage);
                contentView.getPanel().setTitle(emptyPage.getTitle());
                editingModeEnabled.set(false);
                selectedPage.set(null);
            }
            updatePageTree();
        });

    }

    private void createBookNavigationView(Perspective perspective) {
        ToolbarButtonGroup navigationButtonGroup;

        System.out.println("createBookNavigationView()");

        navigationView = perspective.addView(View.createView(ExtendedLayout.CENTER, EmojiIcon.COMPASS,
                                                             "Book Navigation", navigationLayout));
        navigationView.getPanel().setBodyBackgroundColor(Color.MATERIAL_LIGHT_BLUE_A100.withAlpha(0.54f));
        navigationView.getPanel().setMaximizable(false);
        navigationView.setSize(ViewSize.ofAbsoluteWidth(400));
        navigationView.getPanel().setPadding(10);

        navigationButtonGroup = navigationView.addLocalButtonGroup(new ToolbarButtonGroup());
        ToolbarButton newPageButton = navigationButtonGroup.addButton(
                ToolbarButton.createTiny(CompositeIcon.of(EmojiIcon.PAGE_FACING_UP, EmojiIcon.PLUS), "New Page"));
        newPageButton.onClick.addListener(() -> {
            // zuvor prüfen: ist eine andere Seite in Bearbeitung --> speichern / verwerfen ?
            Page newPage = createNewPage(selectedChapter.get());
            selectedPage.set(newPage);
//            updateNavigationView();
//            updatePageTree();
            editPage(newPage);
            showPageSettingsWindow(newPage);
            // pageTreeModel.setRecords(selectedChapter.get().getPages());
        });

        navigationButtonGroup = navigationView.addLocalButtonGroup(new ToolbarButtonGroup());
        ToolbarButton upButton = navigationButtonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.UP_ARROW, ""));
        ToolbarButton downButton = navigationButtonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.DOWN_ARROW, ""));
        upButton.onClick.addListener(() -> {
            System.out.println("upButton.onClick()");
            reorderPage(selectedPage.get(), true);
        });
        downButton.onClick.addListener(() ->{
            System.out.println("downButton.onClick()");
            reorderPage(selectedPage.get(), false);
        });
    }

    private void createBookContentView(Perspective perspective) {

        System.out.println("createBookContentView");

        contentView = perspective.addView(
                View.createView(ExtendedLayout.RIGHT, EmojiIcon.PAGE_FACING_UP, "Content", null));
        contentView.getPanel().setBodyBackgroundColor(Color.BLUE.withAlpha(0.34f));
        contentView.getPanel().setPadding(30);
        contentView.getPanel().setStretchContent(false); // Enables vertical scrolling!

        ToolbarButtonGroup buttonGroup = contentView.addLocalButtonGroup(new ToolbarButtonGroup());

        ToolbarButton saveButton = buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.CHECK_MARK_BUTTON, "Save Changes"));
        saveButton.setVisible(false);
        saveButton.onClick.addListener(() -> {
            System.out.println("saveButton.onClick");
            editingModeEnabled.set(false);
            Page page = selectedPage.get();
            page.setContent(contentEditor.getValue());
            page.save();
            pageManager.unlockPage(page, user);
            // updateContentView(page);
        });

        ToolbarButton cancelButton = buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.CROSS_MARK, "Discard Changes"));
        cancelButton.setVisible(false);
        cancelButton.onClick.addListener(() -> {
            System.out.println("cancelButton.onClick");

            editingModeEnabled.set(false);
            Page page = selectedPage.get();
            page.clearChanges();
            pageManager.unlockPage(page, user);
            // updateContentView(page);
        });

        ToolbarButton editButton = buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.MEMO, "Edit Page Content"));
        editButton.onClick.addListener(() -> {
            System.out.println("editButton.onClick");

            Page page = selectedPage.get();
            editPage(page);
        });

        ToolbarButton pageSettingsButton = buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.WRENCH, "Edit Page Settings"));
        pageSettingsButton.onClick.addListener(() -> showPageSettingsWindow(selectedPage.get()));

//        selectedPage.onChanged().addListener(page -> {
//            if (selectedPage.get() != null) {
//                updateContentView(selectedPage.get());
//                contentView.getPanel().setTitle(page.getTitle());
//                WikiPageManager.PageStatus pageStatus = pageManager.getPageStatus(page);
//                editingModeEnabled.set((pageStatus.isLocked() && pageStatus.getEditor().equals(user)));
//                contentView.focus();
//            } else {
//                selectedPage.set(selectedChapter.get().getPages().stream().findFirst().orElse(null));
////                updateNavigationView();
//                updatePageTree();
//            }
//        });
        editingModeEnabled.onChanged().addListener(enabled -> {
            System.out.println("editingModeEnabled.onChanged(");

            saveButton.setVisible(enabled);
            cancelButton.setVisible(enabled);
            editButton.setVisible(!enabled);
            updateContentView(selectedPage.get());
        });
    }

    private void editPage(Page page) {

        if (page == emptyPage) {
            System.err.println("   edit Page : emptyPage");
            return;
        }
        if (page == null) {
            System.err.println("   edit Page : page == null");
            return;
        }
        System.out.println("   edit Page : id/title " + page.getId() + "/" + page.getTitle());

        WikiPageManager.PageStatus pageStatus = pageManager.lockPage(page, user);
        if (pageStatus.getEditor().equals(user)){
            editingModeEnabled.set(true); // switch on/off
        } else {
            CurrentSessionContext.get().showNotification(
                    EmojiIcon.NO_ENTRY,
                    "Page locked",
                    "by " + pageStatus.getEditor().getName(false) + " since " + pageStatus.getLockSince().toString());
        }
    }

    private void showPageSettingsWindow(Page page) {

        System.out.println("showPageSettingsWindow : page " + ((page != null) ? page.getTitle() : "(empty)"));

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
        ListTreeModel<Page> pageListModel = new ListTreeModel<>(getPages(selectedChapter.get()));
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
            System.out.println("  PageSettings.deleteButton.onClick");

            // ToDo: Cascading delete; currently children are lifted up one level
            Dialogue okCancel = Dialogue.createOkCancel(EmojiIcon.WARNING, "Permanently delete page \"" + page.getTitle() + "\"?", "Do you really want to delete this page?");
            okCancel.show();
            okCancel.onResult.addListener(isConfirmed -> {
                if (isConfirmed) {
                    page.delete();
                    selectedPage.set(null);
                    formWindow.close();
                }
            });
        });

        saveButton.onClick.addListener(() -> {
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
            updateContentView();
//             updateNavigationView();
            updatePageTree(); // ToDo : aktualisiert noch nicht Knotennamen im sichtbaren NavigationView
                              //        (Model-Daten sind aktuell, nicht die Anzeige)

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

        System.out.println("updateContentView : page " + ((page != null) ? page.getTitle() : "(empty)"));

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

//    private void updateNavigationView() {
//
//        System.out.println("updateNavigationView()");
//
//        VerticalLayout navigationLayout = new VerticalLayout();
//        ComboBox<Book> bookComboBox = new ComboBox<>();
//        bookComboBox.setModel(new ListTreeModel<>(Book.getAll()));
//        bookComboBox.setTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES);
//        bookComboBox.setPropertyProvider((book, propertyNames) -> {
//            Map<String, Object> map = new HashMap<>();
//            map.put(BaseTemplate.PROPERTY_ICON, EmojiIcon.CLOSED_BOOK);
//            map.put(BaseTemplate.PROPERTY_CAPTION, book.getTitle());
//            map.put(BaseTemplate.PROPERTY_DESCRIPTION, book.getDescription());
//            return map;
//        });
//        bookComboBox.setRecordToStringFunction(book -> book.getTitle() + " - " + book.getDescription());
//        bookComboBox.setValue(selectedBook.get());
//        bookComboBox.onValueChanged.addListener(selectedBook::set);
//        navigationLayout.addComponent(bookComboBox);
//
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
//        chapterComboBox.setRecordToStringFunction(chapter -> chapter.getTitle() + " - " + chapter.getDescription());
//        chapterComboBox.setValue(selectedChapter.get());
//        chapterComboBox.onValueChanged.addListener(chapter -> {
//            selectedChapter.set(chapter);
//            updateNavigationView();
//        });
//        navigationLayout.addComponent(chapterComboBox);
//
//        ListTreeModel<Page> pageTreeModel = new ListTreeModel<Page>(Collections.EMPTY_LIST);
//        Chapter currentSelectedChapter = selectedChapter.get();
//        System.out.println("currentSelectedChapter = " + currentSelectedChapter.getTitle());
//        pageTreeModel.setRecords(getPages(currentSelectedChapter));
//        Tree<Page> pageTree = new Tree<>(pageTreeModel);
//        pageTreeModel.setTreeNodeInfoFunction(page -> new TreeNodeInfoImpl<>(page.getParent(),
//                WikiUtils.getPageLevel(page) == 0, true, false));
//        pageTree.setOpenOnSelection(true);
//        pageTree.setEntryTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES);
//        pageTree.setPropertyProvider(getPagePropertyProvider());
//        pageTree.setSelectedNode(selectedPage.get());
//
//        pageTree.onNodeSelected.addListener(selectedPage::set);
//        navigationLayout.addComponent(pageTree);
//
//        selectedBook.onChanged().addListener(book -> {
//            System.out.println("selectedBook.onChanged() : " + book.getTitle());
//            chapterListTreeModel.setRecords(book.getChapters());
//            selectedChapter.set(book.getChapters().stream().findFirst().orElse(null));
//
//        });
//        selectedChapter.onChanged().addListener(chapter -> {
//            System.out.println("selectedChapter.onChanged() : " + chapter.getTitle());
//            chapterComboBox.setValue(chapter);
//            selectedPage.set(getPages(chapter).stream().findFirst().orElse(null));
//            pageTreeModel.setRecords(selectedChapter.get().getPages());
//        });
//
//        navigationView.setComponent(navigationLayout);
//    }

    private void updateBookComboBox() {

        System.out.println("updateBookComboBox()");

        bookModel = new ListTreeModel<>(Book.getAll());
        bookComboBox.setValue(selectedBook.get());
    }

    private void updateChapterComboBox() {

        System.out.println("updateChapterComboBox()");

        ListTreeModel<Chapter> chapterListTreeModel = new ListTreeModel<>(selectedBook.get().getChapters());
        chapterComboBox.setModel(chapterListTreeModel);
        chapterComboBox.setValue(selectedChapter.get());
//        chapterComboBox.onValueChanged.addListener(chapter -> {
//            selectedChapter.set(chapter);
//            updateNavigationView();
//        });

//        selectedChapter.onChanged().addListener(chapter -> {
//            System.out.println("selectedChapter.onChanged() : " + chapter.getTitle());
//            chapterComboBox.setValue(chapter);
//            selectedPage.set(getPages(chapter).stream().findFirst().orElse(null));
//            pageTreeModel.setRecords(selectedChapter.get().getPages());
//        });
    }

    private void updatePageTree() {

        System.out.println("updatePageTree()");

        ListTreeModel<Page> pageTreeModel = new ListTreeModel<Page>(Collections.EMPTY_LIST);
        pageTreeModel.setTreeNodeInfoFunction(page -> new TreeNodeInfoImpl<>(page.getParent(),
                WikiUtils.getPageLevel(page) == 0, true, false));
        pageTreeModel.setRecords(getPages(selectedChapter.get()));
        pageTree.setSelectedNode(selectedPage.get());
    }

    @NotNull
    private List<Page> getTopLevelPages(Chapter chapter) {
        if (Objects.nonNull(chapter)) {
            return chapter.getPages().stream().filter(page -> page.getParent() == null).collect(Collectors.toList());
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    // List with correct order of children
    private List<Page> getPages(Chapter chapter) {
        List<Page> pageList = new ArrayList<>();
        if (Objects.nonNull(chapter)) {
            List<Page> topLevelPages = getTopLevelPages(chapter);
            addPageNodes(topLevelPages, pageList);
        }
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

    private Page createNewPage(Chapter chapter) {
        return Page.create()
                .setParent(selectedPage.get().getParent())
                .setTitle("New Page")
                .setDescription("")
                .setChapter(chapter)
                .setContent("<h2>Title</h2><p>Text</p>")
                .save();
    }

    private void reorderPage(Page page, boolean up) {
        if (page == null) {
            System.out.println("reorderPage : page = null");
            return;
        }

        System.out.println("reorderPage : id/title = " + page.getId() + "/" + page.getTitle());
        if (page.getParent() == null) {
            System.out.println("reorderPage : page.Parent = null");
            ArrayList<Page> pageList = new ArrayList<>(getTopLevelPages(page.getChapter()));

            int pos = 0;
            for (Page node : pageList) {
                if (node.equals(page)) {
                    break;
                }
                pos++;
            }
            System.out.println("  up=" + up + ", pos=" + pos + ", pageList.size()=" + pageList.size());
            if ((up && pos == 0) || (!up && pos + 1 == pageList.size())) {
                CurrentSessionContext.get().showNotification(EmojiIcon.WARNING, "Cannot move page beyond the limits!");
                return;
            }
            int newPos = up ? pos - 1 : pos + 1;
            Collections.swap(pageList, pos, newPos);
            page.getChapter().setPages(pageList).save();
        } else {
            Page parent = page.getParent();

            System.out.println("reorderPage : page.Parent id/title = " + parent.getId() + "/" + parent.getTitle());

            ArrayList<Page> pageList = new ArrayList<>(parent.getChildren());
            int pos = 0;
            for (Page node : pageList) {
                if (node.equals(page)) {
                    break;
                }
                pos++;
            }
            System.out.println("  up=" + up + ", pos=" + pos + ", pageList.size()=" + pageList.size());
            if ((up && pos == 0) || (!up && pos + 1 == pageList.size())) {
                CurrentSessionContext.get().showNotification(EmojiIcon.WARNING, "Cannot move page beyond the limits!");
                return;
            }
            int newPos = up ? pos - 1 : pos + 1;
            Collections.swap(pageList, pos, newPos);
            parent.setChildren(pageList).save();
        }
        // PageTreeModel.setRecords(getPages(selectedPage.get()));
//        updateNavigationView();
        updatePageTree();
    }

}
