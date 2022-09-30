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
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.flexcontainer.VerticalLayout;
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

public class BookNavigationView {

    private View navigationView;
    private VerticalLayout navigationLayout;

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
        createBookNavigationView(perspective,
                                 onNewPageClickedListener, onMovePageUpClicked, onMovePageDownClicked);

        return navigationView;
    }

    public void setSelectedBook(Book book) {
        bookComboBox.setValue(book);
    }

    public void setSelectedChapter(Chapter chapter) {
        chapterComboBox.setValue(chapter);
    }

    public void setSelectedPage(Page selectedPage) {
        pageTree.setSelectedNode(selectedPage);
    }


    private void createNavigationLayout(ListTreeModel<Book> bookModel,
                                        ListTreeModel<Chapter> chapterModel,
                                        ListTreeModel<Page> pageModel,
                                        Consumer<Book> onSelectedBookChangedListener,
                                        Consumer<Chapter> onSelectedChapterChangedListener,
                                        Consumer<Page> onSelectedPageChangedListener) {
        System.out.println("createNavigationLayout()");

        navigationLayout = new VerticalLayout();

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

        navigationLayout.addComponent(bookComboBox);
        navigationLayout.addComponent(chapterComboBox);
        navigationLayout.addComponent(pageTree);
    }

    private void createBookNavigationView(Perspective perspective,
                                          Runnable onNewPageClickedListener,
                                          Runnable onMovePageUpClicked,
                                          Runnable onMovePageDownClicked) {

        ToolbarButtonGroup navigationButtonGroup;

        System.out.println("createBookNavigationView()");

        navigationView = perspective.addView(View.createView(
                ExtendedLayout.CENTER, EmojiIcon.COMPASS,"Book Navigation", navigationLayout));
        navigationView.getPanel().setBodyBackgroundColor(Color.MATERIAL_LIGHT_BLUE_A100.withAlpha(0.54f));
        navigationView.getPanel().setMaximizable(false);
        navigationView.setSize(ViewSize.ofAbsoluteWidth(400));
        navigationView.getPanel().setPadding(10);

        navigationButtonGroup = navigationView.addLocalButtonGroup(new ToolbarButtonGroup());
        ToolbarButton newPageButton = navigationButtonGroup.addButton(
                ToolbarButton.createTiny(CompositeIcon.of(EmojiIcon.PAGE_FACING_UP, EmojiIcon.PLUS), "New Page"));
        newPageButton.onClick.addListener(onNewPageClickedListener);

        navigationButtonGroup = navigationView.addLocalButtonGroup(new ToolbarButtonGroup());
        ToolbarButton upButton = navigationButtonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.UP_ARROW, ""));
        ToolbarButton downButton = navigationButtonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.DOWN_ARROW, ""));
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
