package org.teamapps.wiki.app.editor;

import org.jetbrains.annotations.NotNull;
import org.teamapps.common.format.Color;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.icon.emoji.EmojiIcon;
import org.teamapps.icon.emoji.EmojiIconStyle;
import org.teamapps.icons.Icon;
import org.teamapps.icons.composite.CompositeIcon;
import org.teamapps.ux.application.layout.ExtendedLayout;
import org.teamapps.ux.application.perspective.Perspective;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.application.view.ViewSize;
import org.teamapps.ux.component.field.Label;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.flexcontainer.VerticalLayout;
import org.teamapps.ux.component.panel.Panel;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;
import org.teamapps.ux.component.tree.Tree;
import org.teamapps.ux.model.ListTreeModel;
import org.teamapps.wiki.model.wiki.Book;
import org.teamapps.wiki.model.wiki.Chapter;
import org.teamapps.wiki.model.wiki.Page;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/* This class contains the GUI elements of the book navigation view. It is structured like this:

    view
     |--- Panel    (is an internal component of each View)
     |--- buttonGroup
     |        |--- newPageButton
     |        |--- upButton
     |        |--- downButton
     |
     |-- layout
            |--- Label bookComboBox
            |--- bookComboBox
            |--- Label chapterComboBox
            |--- chapterComboBox
            |--- Label pageTree
            |--- Panel
                   |--- pageTree
 */
public class BookNavigationView {

    private View view;
    private VerticalLayout layout;

    private ComboBox<Book> bookComboBox;
    private ComboBox<Chapter> chapterComboBox;
    private Tree<Page> pageTree;



    public View create(Perspective perspective,
                       ListTreeModel<Book> bookModel,
                       ListTreeModel<Chapter> chapterModel,
                       ListTreeModel<Page> pageModel,
                       Consumer<Book> onSelectedBookChangedListener,
                       Consumer<Chapter> onSelectedChapterChangedListener,
                       Consumer<Page> onSelectedPageChangedListener,
                       Runnable onNewPageClickedListener,
                       Runnable onMovePageUpClicked,
                       Runnable onMovePageDownClicked) {
        createNavigationLayout(bookModel, chapterModel, pageModel,
                               onSelectedBookChangedListener, onSelectedChapterChangedListener, onSelectedPageChangedListener);
        createNavigationView(perspective,
                             onNewPageClickedListener, onMovePageUpClicked, onMovePageDownClicked);

        return view;
    }

    public void setSelectedBook(Book book) {
        bookComboBox.setValue(book);
    }

    public void setSelectedChapter(Chapter chapter) {
        chapterComboBox.setValue(chapter);
    }

    public void setSelectedPage(Page selectedPage) { pageTree.setSelectedNode(selectedPage); }


    private void createNavigationLayout(ListTreeModel<Book> bookModel,
                                        ListTreeModel<Chapter> chapterModel,
                                        ListTreeModel<Page> pageModel,
                                        Consumer<Book> onSelectedBookChangedListener,
                                        Consumer<Chapter> onSelectedChapterChangedListener,
                                        Consumer<Page> onSelectedPageChangedListener) {
        layout = new VerticalLayout();

        bookComboBox = new ComboBox<>();
        bookComboBox.setModel(bookModel);
        bookComboBox.setTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES);
        bookComboBox.setPropertyProvider(getBookPropertyProvider());
        bookComboBox.setRecordToStringFunction(book -> book.getTitle() + " - " + book.getDescription());
        bookComboBox.onValueChanged.addListener(onSelectedBookChangedListener);

        chapterComboBox = new ComboBox<>();
        chapterComboBox.setModel(chapterModel);
        chapterComboBox.setTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES);
        chapterComboBox.setPropertyProvider(getChapterPropertyProvider());
        chapterComboBox.setRecordToStringFunction(chapter -> chapter.getTitle() + " - " + chapter.getDescription());
        chapterComboBox.onValueChanged.addListener(onSelectedChapterChangedListener);

        pageTree = new Tree<>(pageModel);
        pageTree.setOpenOnSelection(true);
        pageTree.setEntryTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES);
        pageTree.setPropertyProvider(getPagePropertyProvider());
        pageTree.onNodeSelected.addListener(onSelectedPageChangedListener);
        pageTree.setShowExpanders(true);

        Panel treePanel = new Panel();
        treePanel.setContent(pageTree);
        treePanel.setHideTitleBar(true);
        treePanel.setStretchContent(true);
        treePanel.setBodyBackgroundColor(Color.MATERIAL_LIGHT_BLUE_A100.withAlpha(0.30f));

        layout.addComponent(new Label("Books:"));
        layout.addComponent(bookComboBox);
        layout.addComponent(new Label("Chapters:"));
        layout.addComponent(chapterComboBox);
        layout.addComponent(new Label("Pages:"));
        layout.addComponentFillRemaining(treePanel);
    }

    private void createNavigationView(Perspective perspective,
                                      Runnable onNewPageClickedListener,
                                      Runnable onMovePageUpClicked,
                                      Runnable onMovePageDownClicked) {

        ToolbarButtonGroup buttonGroup;

        view = perspective.addView(View.createView(
                ExtendedLayout.CENTER, EmojiIcon.COMPASS,"Book Navigation", layout));
        view.getPanel().setBodyBackgroundColor(Color.MATERIAL_LIGHT_BLUE_A100.withAlpha(0.54f));
        view.getPanel().setMaximizable(false);
        view.setSize(ViewSize.ofAbsoluteWidth(400));
        view.getPanel().setPadding(10);

        buttonGroup = view.addLocalButtonGroup(new ToolbarButtonGroup());
        ToolbarButton newPageButton = buttonGroup.addButton(
                ToolbarButton.createTiny(CompositeIcon.of(EmojiIcon.PAGE_FACING_UP, EmojiIcon.PLUS), "New Page"));
        newPageButton.onClick.addListener(onNewPageClickedListener);

        buttonGroup = view.addLocalButtonGroup(new ToolbarButtonGroup());
        ToolbarButton upButton = buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.UP_ARROW, ""));
        ToolbarButton downButton = buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.DOWN_ARROW, ""));
        upButton.onClick.addListener(onMovePageUpClicked);
        downButton.onClick.addListener(onMovePageDownClicked);
    }

    @NotNull
    private PropertyProvider<Book> getBookPropertyProvider() {
        return (book, propertyNames) -> {
            Map<String, Object> map = new HashMap<>();
            map.put(BaseTemplate.PROPERTY_ICON, EmojiIcon.CLOSED_BOOK);
            map.put(BaseTemplate.PROPERTY_CAPTION, book.getTitle());
            map.put(BaseTemplate.PROPERTY_DESCRIPTION, book.getDescription());
            return map;
        };
    }

    @NotNull
    private PropertyProvider<Chapter> getChapterPropertyProvider() {
        return (chapter, propertyNames) -> {
            Map<String, Object> map = new HashMap<>();
            map.put(BaseTemplate.PROPERTY_ICON, EmojiIcon.OPEN_BOOK);
            map.put(BaseTemplate.PROPERTY_CAPTION, chapter.getTitle());
            map.put(BaseTemplate.PROPERTY_DESCRIPTION, chapter.getDescription());
            return map;
        };
    }

    @NotNull
    private PropertyProvider<Page> getPagePropertyProvider() {
        return (page, propertyNames) -> {
            Map<String, Object> map = new HashMap<>();

            Icon<EmojiIcon, EmojiIconStyle> pageIcon;
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
}
