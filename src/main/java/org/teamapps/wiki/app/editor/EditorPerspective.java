package org.teamapps.wiki.app.editor;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.perspective.AbstractApplicationPerspective;
import org.teamapps.application.api.user.SessionUser;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.databinding.MutableValue;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.ux.application.perspective.Perspective;
import org.teamapps.ux.model.ListTreeModel;
import org.teamapps.wiki.app.*;
import org.teamapps.wiki.model.wiki.Book;
import org.teamapps.wiki.model.wiki.Chapter;
import org.teamapps.wiki.model.wiki.Page;

import java.util.Collections;
import java.util.Objects;

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

    private Page emptyPage;
    private Page currentEditPage;

    private boolean isCurrentEditPageNew;


    public EditorPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {

        super(applicationInstanceData, perspectiveInfoBadgeValue);
        PerspectiveSessionData perspectiveSessionData = (PerspectiveSessionData) getApplicationInstanceData();
        user = perspectiveSessionData.getUser();
        pageManager = WikiApplicationBuilder.PAGE_MANAGER;
        pageManager.addReleaseUserLockListener(user);
        createUi();
    }

    private void createUi() {

        Perspective perspective = getPerspective();

        createListTreeModel();
        bookNavigationView = new BookNavigationView();
        bookNavigationView.create(perspective,
                bookModel, chapterModel, pageModel,
                selectedBook::set, selectedChapter::set, selectedPage::set,
                this::onNewPageButtonClicked, this::onMovePageUpButtonClicked, this::onMovePageDownButtonClicked,
                this::onMovePageLeftButtonClicked, this::onMovePageRightButtonClicked);
        bookContentView = new BookContentView();
        bookContentView.create(perspective,
                this::onPageContentSaved, this::onPageContentCanceled, this::onEditPageContentClicked,
                this::onEditPageSettingsClicked);
        pageSettingsForm = new PageSettingsForm();
        //noinspection unchecked
        pageSettingsForm.create(getApplicationInstanceData(), new ListTreeModel<Page>(Collections.EMPTY_LIST),
                this::onPageSettingsSaved, this::onPageSettingsCanceled, this::onPageSettingsPageDeleted);

        initializeTwoWayBindables();

        emptyPage = Page.create()
                .setParent(null).setTitle("").setDescription("")
                .setChapter(null).setContent("");
        currentEditPage = null;
        isCurrentEditPageNew = false;

        selectedBook.set(Book.getAll().stream().findFirst().orElse(null));
        selectedChapter.set(selectedBook.get().getChapters().stream().findFirst().orElse(null));
        selectedPage.set(selectedChapter.get().getPages().stream().findFirst().orElse(null));
    }


    private void createListTreeModel() {

        bookModel = new ListTreeModel<>(Book.getAll());
        //noinspection unchecked
        chapterModel = new ListTreeModel<Chapter>(Collections.EMPTY_LIST);
        //noinspection unchecked
        pageModel = new ListTreeModel<Page>(Collections.EMPTY_LIST);
        pageModel.setTreeNodeInfoFunction(WikiUtils.getPageTreeNodeInfoFunction());
    }

    private void initializeTwoWayBindables() {

        selectedBook.onChanged().addListener(book -> {
            if (Objects.nonNull(book)) {
                System.out.println("selectedBook.onChanged : " + book.getTitle());
                bookNavigationView.setSelectedBook(book);

                chapterModel.setRecords(book.getChapters());
                selectedChapter.set(book.getChapters().stream().findFirst().orElse(null));
            } else {
                System.out.println("selectedBook.onChanged : (none)");

                //noinspection unchecked
                chapterModel.setRecords(Collections.EMPTY_LIST);
                selectedChapter.set(null);
            }
        });
        selectedChapter.onChanged().addListener(chapter -> {
            if (Objects.nonNull(chapter)) {
                System.out.println("selectedChapter.onChanged : " + chapter.getTitle());
                bookNavigationView.setSelectedChapter(chapter);

                pageModel.setRecords(selectedChapter.get().getPages());
                selectedPage.set(getFirstPageOfChapter(chapter));
            } else {
                System.out.println("selectedChapter.onChanged : (none)");
                bookNavigationView.setSelectedChapter(null);

                //noinspection unchecked
                pageModel.setRecords(Collections.EMPTY_LIST);
                selectedPage.set(null);
            }
        });
        selectedPage.onChanged().addListener(page -> {

            boolean hasLeftEditingPage = (currentEditPage != null && page != currentEditPage);
            if (hasLeftEditingPage) {
                String errorMessage = "Leaving page editing detected! Editing will be aborted!";
                WikiUtils.showWarning(errorMessage);
                System.err.println(errorMessage);
                abortPageEdit(currentEditPage);
            }

            if (Objects.nonNull(page)) {
                System.out.println("selectedPage.onChanged : id/title [" + page.getId() + " / " + page.getTitle() + "]");
                updateContentView(page);
                logPageList(pageModel);
            } else {
                System.out.println("selectedPage.onChanged : (none)");
                updateContentView(emptyPage);
            }
        });

    }

    private void onNewPageButtonClicked() {

        System.out.println("onNewPageButtonClicked");

        if (currentEditPage != null) {
            WikiUtils.showWarning("Cannot create new page while another is edited!");
            return;
        }

        if (selectedChapter.get() == null) {
            WikiUtils.showWarning("Cannot create a page. No chapter is selected!");
            return;
        }

        currentEditPage = createNewPage(selectedChapter.get());
        isCurrentEditPageNew = true;

        selectedPage.set(currentEditPage);

        showPageSettingsWindow(currentEditPage, isCurrentEditPageNew);
    }

    private void onMovePageUpButtonClicked() {

        System.out.println("onMovePageUpButtonClicked");
        PageTreeUtils.reorderPage(selectedPage.get(), true);
        updatePageTree();
    }

    private void onMovePageDownButtonClicked() {

        System.out.println("onMovePageDownButtonClicked");
        PageTreeUtils.reorderPage(selectedPage.get(), false);
        updatePageTree();
    }

    private void onMovePageLeftButtonClicked() {

        System.out.println("onMovePageLeftButtonClicked");
        PageTreeUtils.movePageLevelUp(selectedPage.get());
        updatePageTree();
    }
    private void onMovePageRightButtonClicked() {

        System.out.println("onMovePageRightButtonClicked");
        PageTreeUtils.movePageLevelDown(selectedPage.get());
        updatePageTree();
    }


    private Void onPageContentSaved(String pageContent) {

        System.out.println("   onPageContentSaved");

        Page page = selectedPage.get();
        page.setContent(pageContent);
        page.save();

        setContentViewEditMode(BookContentView.PAGE_EDIT_MODE.OFF);
        updateContentView(page);

        pageManager.unlockPage(page, user);
        currentEditPage = null;

        return null;
    }


    private void onPageContentCanceled() {

        System.out.println("   onPageEditCanceled");

        Page originalPage = selectedPage.get();
        abortPageEdit(originalPage);
        updateContentView(originalPage);
    }

    private void abortPageEdit(Page page) {

        if (Objects.isNull(page)) {
            System.err.println("   abortPageEdit : page is NULL; Ignore abort!");
            return;
        }

        System.out.println("   abortPageEdit : id/title [" + page.getId() + " / " + page.getTitle() + "]");

        setContentViewEditMode(BookContentView.PAGE_EDIT_MODE.OFF);

        page.clearChanges();

        if (isCurrentEditPageNew) {
            System.out.println("   DELETE page [" + page.getId() + "]");
            page.delete();
            updatePageTree();
        } else {
            pageManager.unlockPage(page, user);
        }
        currentEditPage = null;
        isCurrentEditPageNew = false;
    }

    private void onEditPageContentClicked() {

        currentEditPage = selectedPage.get();

        if (currentEditPage == emptyPage || Objects.isNull(currentEditPage)) {
            System.err.println("   onEditPageContentClicked - no page selected" );
            WikiUtils.showWarning("No page for editing selected!");
            return;
        }
        System.out.println("   onEditPageContentClicked : id/title [" + currentEditPage.getId() + " / " + currentEditPage.getTitle() + "]");

        if (setPageLock(currentEditPage)) {
            setContentViewEditMode(BookContentView.PAGE_EDIT_MODE.CONTENT);
            updateContentView(currentEditPage);
        }
    }

    private void onEditPageSettingsClicked() {

        currentEditPage = selectedPage.get();

        if (currentEditPage == emptyPage || Objects.isNull(currentEditPage)) {
            System.err.println("onEditPageSettingsClicked - no page selected");
            WikiUtils.showWarning("No page for editing selected!");
            return;
        } else {
            System.out.println("onEditPageSettingsClicked : " + currentEditPage.getId());
        }

        if (setPageLock(currentEditPage)) {
            showPageSettingsWindow(currentEditPage, false);
        }
    }


    private Void onPageSettingsSaved(Page modifiedPage) {

//        System.out.println("   onPageSettingSaved");
        boolean isExistingPage = !isCurrentEditPageNew;
        if (isExistingPage) {
            pageManager.unlockPage(currentEditPage, user);
        }
        currentEditPage = null;
        isCurrentEditPageNew = false;

        setContentViewEditMode(BookContentView.PAGE_EDIT_MODE.OFF);

        selectedPage.set(modifiedPage); // update views
        updateContentView();
        updatePageTree();

        return null;
    }

    private void onPageSettingsCanceled() {

//       System.out.println("   onPageSettingCanceled");
        abortPageEdit(currentEditPage);
    }

    private void onPageSettingsPageDeleted(Boolean isCascadingDeleteEnabled) {

        System.out.println("   onPageSettingsPageDeleted");
        PageTreeUtils.delete(currentEditPage, isCascadingDeleteEnabled);

        pageManager.unlockPage(currentEditPage, user);
        currentEditPage = null;
        isCurrentEditPageNew = false;

        setContentViewEditMode(BookContentView.PAGE_EDIT_MODE.OFF);
        selectedPage.set(getFirstPageOfChapter(selectedChapter.get()));
        updatePageTree();
    }

    private void showPageSettingsWindow(Page page, boolean isNewPage) {

        System.out.println("   showPageSettingsWindow : id/title [" + page.getId() + " / " + page.getTitle() + "]");

        ListTreeModel<Page> pageListModel = new ListTreeModel<>(PageTreeUtils.getSortedPagesOfChapter(selectedChapter.get()));
        pageSettingsForm.show(page, pageListModel, isNewPage);
    }

    private void updateContentView() {
        updateContentView(selectedPage.get());
    }

    private void updateContentView(Page page) {

        if (page == null) {
            System.err.println("   updateContentView : page is null; use empty page instead");
            page = emptyPage;
        } else {
            System.out.println("   updateContentView : page [" + page.getId() + " / " + page.getTitle() + "]");
        }

        bookContentView.updateContentView(page);
    }

    private void updatePageTree() {

        System.out.println("   updatePageTree()");
        pageModel.setRecords(PageTreeUtils.getSortedPagesOfChapter(selectedChapter.get()));
        logPageList(pageModel);
        bookNavigationView.setSelectedPage(selectedPage.get());
    }

    private void setContentViewEditMode(BookContentView.PAGE_EDIT_MODE editMode) {
        bookContentView.setPageEditMode(editMode);
    }

    private void logPageList(ListTreeModel<Page> pageTreeModel) {
        System.out.println("   Page list : [chapter id / page id / page title]");
        int hierarchyLevel;
        Chapter chapter;
        for (Page currentPage : pageTreeModel.getRecords()) {
            if (currentPage != null) {
                chapter = currentPage.getChapter();
                hierarchyLevel = PageTreeUtils.getPageLevel(currentPage);
                System.out.println("      " + ((chapter == null) ? "-" : chapter.getId()) + "   "
                                          + "   ".repeat(hierarchyLevel) +  currentPage.getId() + " / '" + currentPage.getTitle() + "'");
            } else {
                System.out.println("      -   - / '(null)'");
            }
        }
    }

    private boolean setPageLock(Page pageToLock) {

        LockSuccessStatus lockSuccessStatus = pageManager.lockPage(pageToLock, user);

        boolean isSuccessful = lockSuccessStatus.hasReceivedLock();
        if (! isSuccessful) {
            switch (lockSuccessStatus.getLockFailReason()) {
                case LOCKED_BY_OTHER_USER -> {
                    SessionUser user = lockSuccessStatus.getLockOwner();
                    WikiUtils.showWarning("Page is already locked by user " + ((user == null) ? "UNKNOWN" : user.getName(false)));
                }

                case INVALID_INPUT -> WikiUtils.showWarning("Cannot lock page! Invalid parameters!");
            }
        }

        return isSuccessful;
    }

    private Page createNewPage(Chapter chapter) {

        System.out.println(   "createNewPage");

        Page newPage = Page.create();

        if (Objects.isNull(selectedPage.get())) {
            newPage.setParent(null);
        } else {
            newPage.setParent(selectedPage.get());
        }
        if (Objects.isNull(chapter)) {
            System.out.println("createNewPage : chapter is null!");
        }

        return newPage
                .setChapter(chapter)
                .setTitle("New Page").setDescription("")
                .setContent("<h2>Title</h2><p>Text</p>")
                .save();
    }

    private Page getFirstPageOfChapter(Chapter chapter) {
        if (Objects.isNull(chapter)) {
            return null;
        } else {
            return PageTreeUtils.getSortedPagesOfChapter(chapter).stream().findFirst().orElse(null);
        }
    }

}
