package org.teamapps.wiki.app.editor;

import org.jetbrains.annotations.NotNull;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.perspective.AbstractApplicationPerspective;
import org.teamapps.application.api.user.SessionUser;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.databinding.MutableValue;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.icon.emoji.EmojiIcon;
import org.teamapps.icons.Icon;
import org.teamapps.ux.application.perspective.Perspective;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.tree.TreeNodeInfoImpl;
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

    private BookNavigationView bookNavigationView;
    private BookContentView bookContentView;

    private PageSettingsForm pageSettingsForm;

    ListTreeModel<Book> bookModel;
    ListTreeModel<Chapter> chapterModel;
    ListTreeModel<Page> pageModel;


    private final TwoWayBindableValue<Book> selectedBook = TwoWayBindableValue.create();
    private final TwoWayBindableValue<Chapter> selectedChapter = TwoWayBindableValue.create();
    private final TwoWayBindableValue<Page> selectedPage = TwoWayBindableValue.create();

    private BookContentView.PAGE_EDIT_MODE pageEditMode = BookContentView.PAGE_EDIT_MODE.OFF;
    private Page emptyPage;
    private Page currentEditPage;


    public EditorPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {

        super(applicationInstanceData, perspectiveInfoBadgeValue);
        PerspectiveSessionData perspectiveSessionData = (PerspectiveSessionData) getApplicationInstanceData();
        pageManager = WikiApplicationBuilder.PAGE_MANAGER;
        user = perspectiveSessionData.getUser();
        createUi();
    }

    private void createUi() {

        Perspective perspective = getPerspective();

        createListTreeModel();
        bookNavigationView = new BookNavigationView();
        bookNavigationView.create(perspective,
                bookModel, chapterModel, pageModel,
                selectedBook::set, selectedChapter::set, selectedPage::set,
                this::onNewPageButtonClicked, this::onMovePageUpButtonClicked, this::onMovePageDownButtonClicked);
        bookContentView = new BookContentView();
        bookContentView.create(perspective,
                this::onPageContentSaved, this::onPageContentCanceled, this::onEditPageContentClicked,
                this::onEditPageSettingsClicked);
        pageSettingsForm = new PageSettingsForm();
        pageSettingsForm.create(getApplicationInstanceData(), new ListTreeModel<Page>(Collections.EMPTY_LIST),
                this::onPageSettingsSaved, this::onPageSettingsCanceled, this::onPageSettingsPageDeleted);

        initializeTwoWayBindables();

        emptyPage = Page.create()
                .setParent(null).setTitle("").setDescription("")
                .setChapter(null).setContent("");
        currentEditPage = null;

        selectedBook.set(Book.getAll().stream().findFirst().orElse(null));
        selectedChapter.set(selectedBook.get().getChapters().stream().findFirst().orElse(null));
        selectedPage.set(selectedChapter.get().getPages().stream().findFirst().orElse(null));

        if (selectedPage.get() == null) {
            // If the initial loaded book or chapter has no pages, then the content view seems to be in edit mode (wrong background colour).
            // Displaying an empty page changes to the correct background color.
            updateContentView(emptyPage);
        }

    }


    private void createListTreeModel() {

        bookModel = new ListTreeModel<>(Book.getAll());
        chapterModel = new ListTreeModel<Chapter>(Collections.EMPTY_LIST);
        pageModel = new ListTreeModel<Page>(Collections.EMPTY_LIST);
        pageModel.setTreeNodeInfoFunction(
                page -> new TreeNodeInfoImpl<>(page.getParent(), WikiUtils.getPageLevel(page) == 0,
                        true, false));
    }

    private void initializeTwoWayBindables() {

        selectedBook.onChanged().addListener(book -> {
            if (Objects.nonNull(book)) {
                System.out.println("selectedBook.onChanged() : " + book.getTitle());
                bookNavigationView.setSelectedBook(book);

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
                System.out.println("selectedChapter.onChanged() : " + chapter.getTitle());
                bookNavigationView.setSelectedChapter(chapter);

                pageModel.setRecords(selectedChapter.get().getPages());
                selectedPage.set(getReOrderedPages(chapter).stream().findFirst().orElse(null));
            } else {
                System.out.println("selectedChapter.onChanged() : (null)");
                bookNavigationView.setSelectedChapter(null);

                pageModel.setRecords(Collections.EMPTY_LIST);
                selectedPage.set(null);
            }
        });
        selectedPage.onChanged().addListener(page -> {

            boolean hasLeftEditingPage = (currentEditPage != null && page != currentEditPage);
            if (hasLeftEditingPage) {
                System.err.println("WARNING: Leaving edit page detected! --> Force abort editing!");
                abortPageEdit(currentEditPage);
            }

            if (Objects.nonNull(page)) {
                System.out.println("selectedPage.onChanged() : id/title " + page.getId() + "/" + page.getTitle());

                updateContentView(page);
            } else {
                System.out.println("selectedPage.onChanged() : (null)");

                updateContentView(emptyPage);
                pageEditMode = BookContentView.PAGE_EDIT_MODE.OFF;
                bookContentView.setPageEditMode(pageEditMode);
            }
            updatePageTree();
        });

    }

    private void onNewPageButtonClicked() {

        System.out.println("onNewPageButtonClicked()");

        if (currentEditPage != null) {
            CurrentSessionContext.get().showNotification(EmojiIcon.WARNING, "Cannot create new page while another is edited!");
            return;
        }

        if (selectedChapter.get() == null) {
            CurrentSessionContext.get().showNotification(EmojiIcon.WARNING, "Cannot create a page. No chapter is selected!");
            return;
        }

        currentEditPage = createNewPage(selectedChapter.get());
        selectedPage.set(currentEditPage);

        pageEditMode = BookContentView.PAGE_EDIT_MODE.SETTINGS;
        bookContentView.setPageEditMode(pageEditMode);

        showPageSettingsWindow(currentEditPage);
    }

    private void onMovePageUpButtonClicked() {

        System.out.println("onMovePageUpButtonClicked");
        reorderPage(selectedPage.get(), true);
    }

    private void onMovePageDownButtonClicked() {

        System.out.println("onMovePageDownButtonClicked");
        reorderPage(selectedPage.get(), false);
    }


    private Page onPageContentSaved(String pageContent) {

        System.out.println("onPageContentSaved");

        pageEditMode = BookContentView.PAGE_EDIT_MODE.OFF;
        bookContentView.setPageEditMode(pageEditMode);

        Page page = selectedPage.get();
        page.setContent(pageContent);
        page.save();
        pageManager.unlockPage(page, user);
        currentEditPage = null;

        return page;
    }

    private Page onPageContentCanceled() {

        System.out.println("onPageEditCanceled");

        Page page = selectedPage.get();
        abortPageEdit(page);

        return page;
    }

    private void abortPageEdit(Page page) {

        if (Objects.isNull(page)) {
            System.err.println("   abortPageEdit : page is NULL; Ingore abort!");
            return;
        }

        System.out.println("   abortPageEdit : page (id / title) - " + page.getId() + ", " + page.getTitle());

        if (pageEditMode == BookContentView.PAGE_EDIT_MODE.SETTINGS) {
            pageSettingsForm.close();
        }

        pageEditMode = BookContentView.PAGE_EDIT_MODE.OFF;
        bookContentView.setPageEditMode(pageEditMode);

        page.clearChanges();
        pageManager.unlockPage(page, user);
        currentEditPage = null;
    }

    private void onEditPageContentClicked() {

        System.out.println("editButton.onClick");
        currentEditPage = selectedPage.get();
        editPage(currentEditPage);
    }

    private void onEditPageSettingsClicked() {

        pageEditMode = BookContentView.PAGE_EDIT_MODE.SETTINGS;
        bookContentView.setPageEditMode(pageEditMode);

        currentEditPage = selectedPage.get();
        pageManager.lockPage(currentEditPage, user);
        showPageSettingsWindow(currentEditPage);
    }

    private Void onPageSettingsSaved(Page modifiedPage) {

        System.out.println("onPageSettingSaved");
        pageManager.unlockPage(currentEditPage, user);
        currentEditPage = null;

        pageEditMode = BookContentView.PAGE_EDIT_MODE.OFF;
        bookContentView.setPageEditMode(pageEditMode);

        updateContentView();
        updatePageTree();
        selectedPage.set(modifiedPage); // update views

        return null;
    }

    private void onPageSettingsCanceled() {

        System.out.println("onPageSettingCanceled");
        abortPageEdit(currentEditPage);
    }

    private void onPageSettingsPageDeleted() {

        System.out.println("onPageSettingsPageDeleted");
        pageManager.unlockPage(currentEditPage, user);
        currentEditPage.delete();
        currentEditPage = null;

        pageEditMode = BookContentView.PAGE_EDIT_MODE.OFF;
        bookContentView.setPageEditMode(pageEditMode);

        selectedPage.set(null);
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
        System.out.println("editPage : id/title " + page.getId() + "/" + page.getTitle());

        WikiPageManager.PageStatus pageStatus = pageManager.lockPage(page, user);
        if (pageStatus.getEditor().equals(user)) {

            pageEditMode = BookContentView.PAGE_EDIT_MODE.CONTENT;
            bookContentView.setPageEditMode(pageEditMode);

            updateContentView(page);
        } else {
            CurrentSessionContext.get().showNotification(
                    EmojiIcon.NO_ENTRY,
                    "Page locked",
                    "by " + pageStatus.getEditor().getName(false) + " since " + pageStatus.getLockSince().toString());
        }
    }

    private void showPageSettingsWindow(Page page) {

        if (Objects.isNull(page)) {
            CurrentSessionContext.get().showNotification(EmojiIcon.WARNING, "Failed to edit settings of non-existing page!");
            System.err.println("showPageSettingsWindow : page is null!");
            return;
        } else {
            System.out.println("showPageSettingsWindow : (id=" + page.getId() + ", title=" + page.getTitle() + ")");
        }

        ListTreeModel<Page> pageListModel = new ListTreeModel<>(getReOrderedPages(selectedChapter.get()));
        pageSettingsForm.show(page, pageListModel);
    }

    private void updateContentView() {
        updateContentView(selectedPage.get());
    }

    private void updateContentView(Page page) {

        if (page == null) {
            System.err.println("updateContentView : page == null");
            page = emptyPage;
        }

        System.out.println("updateContentView : page " + page.getTitle());

        bookContentView.updateContentView(page);
    }

    private void updatePageTree() {

        System.out.println("updatePageTree()");
        pageModel.setRecords(getReOrderedPages(selectedChapter.get()));
        logPageList(pageModel);
        bookNavigationView.setSelectedPage(selectedPage.get());
    }

    private void logPageList(ListTreeModel<Page> pageTreeModel) {
        System.out.println("  Page list : (id / title)");
        for (Page currentPage : pageTreeModel.getRecords()) {
            if (currentPage != null) {
                System.out.println("     " + currentPage.getId() + " / " + currentPage.getTitle());
            } else {
                System.out.println("     - / - (NULL)");
            }
        }
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
    private List<Page> getReOrderedPages(Chapter chapter) {

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

        if (chapter == null) {
            System.err.println("createNewPage : chapter == null");
            return null;
        }

        System.out.println("createNewPage");

        Page newPage = Page.create();

        if (selectedPage.get() == null) {
            newPage.setParent(null);
        } else {
            newPage.setParent(selectedPage.get().getParent());
        }

        return newPage
                .setChapter(chapter)
                .setTitle("New Page").setDescription("")
                .setContent("<h2>Title</h2><p>Text</p>")
                .save();
    }

    private void reorderPage(Page page, boolean up) {

        if (page == null) {
            System.err.println("reorderPage : page == null");
            CurrentSessionContext.get().showNotification(EmojiIcon.WARNING, "Page is null. Cannot reorder!");
            return;
        }

        System.out.println("reorderPage : id/title = " + page.getId() + "/" + page.getTitle());
        if (page.getParent() == null) {
            System.out.println("reorderPage : page.Parent == null");
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
        updatePageTree();
    }

}
