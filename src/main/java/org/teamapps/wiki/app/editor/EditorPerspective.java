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
import org.teamapps.ux.model.ListTreeModel;
import org.teamapps.ux.session.CurrentSessionContext;
import org.teamapps.wiki.app.PageTreeUtils;
import org.teamapps.wiki.app.WikiApplicationBuilder;
import org.teamapps.wiki.app.WikiPageManager;
import org.teamapps.wiki.app.WikiUtils;
import org.teamapps.wiki.model.wiki.Book;
import org.teamapps.wiki.model.wiki.Chapter;
import org.teamapps.wiki.model.wiki.Page;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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

        // ToDo Ursache finden und beseitigen
        if (selectedPage.get() == null) {
            // If the initial loaded book or chapter has no pages, then the content view seems to be in edit mode (wrong background colour).
            // Displaying an empty page changes to the correct background color.
            updateContentView(emptyPage);
        }

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
                selectedPage.set(PageTreeUtils.getReOrderedPages(chapter).stream().findFirst().orElse(null));
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
        // ToDo Page Lock ist bei neuen Seiten überflüssig
        selectedPage.set(currentEditPage);
        pageManager.lockPage(currentEditPage, user);

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
            System.err.println("   abortPageEdit : page is NULL; Ingore abort!");
            return;
        }

        System.out.println("   abortPageEdit : id/title [" + page.getId() + " / " + page.getTitle() + "]");

        setContentViewEditMode(BookContentView.PAGE_EDIT_MODE.OFF);

        page.clearChanges();
        pageManager.unlockPage(page, user);
        if (isCurrentEditPageNew) {
            System.out.println("   DELETE page [" + page.getId() + "]");
            page.delete();
            updatePageTree();
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

        editPage(currentEditPage);
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

        pageManager.lockPage(currentEditPage, user);
        showPageSettingsWindow(currentEditPage, false);
    }

    private Void onPageSettingsSaved(Page modifiedPage) {

//        System.out.println("   onPageSettingSaved");
        pageManager.unlockPage(currentEditPage, user);
        currentEditPage = null;
        isCurrentEditPageNew = false;//

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

    private void onPageSettingsPageDeleted() {

        System.out.println("   onPageSettingsPageDeleted");
        pageManager.unlockPage(currentEditPage, user);
        // ToDo: Cascading delete; currently children are lifted up one level
        currentEditPage.delete();
        currentEditPage = null;
        isCurrentEditPageNew = false;

        setContentViewEditMode(BookContentView.PAGE_EDIT_MODE.OFF);
        // ToDo erste Seite auswählen, und ContenView aktualisieren
        selectedPage.set(null);
        updatePageTree();
    }

    private void editPage(Page page) {

        WikiPageManager.PageStatus pageStatus = pageManager.lockPage(page, user);
        if (pageStatus.getEditor().equals(user)) {

            setContentViewEditMode(BookContentView.PAGE_EDIT_MODE.CONTENT);

            updateContentView(page);
        } else {
            CurrentSessionContext.get().showNotification(
                    EmojiIcon.NO_ENTRY,
                    "Page locked",
                    "by " + pageStatus.getEditor().getName(false) + " since " + pageStatus.getLockSince().toString());
        }
    }

    private void showPageSettingsWindow(Page page, boolean isNewPage) {

        System.out.println("   showPageSettingsWindow : id/title [" + page.getId() + " / " + page.getTitle() + "]");

        ListTreeModel<Page> pageListModel = new ListTreeModel<>(PageTreeUtils.getReOrderedPages(selectedChapter.get()));
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
        pageModel.setRecords(PageTreeUtils.getReOrderedPages(selectedChapter.get()));
        logPageList(pageModel);
        bookNavigationView.setSelectedPage(selectedPage.get());
    }

    private void setContentViewEditMode(BookContentView.PAGE_EDIT_MODE editMode) {
        bookContentView.setPageEditMode(editMode);
    }

    private void logPageList(ListTreeModel<Page> pageTreeModel) {
        System.out.println("   Page list : id/title");
        for (Page currentPage : pageTreeModel.getRecords()) {
            if (currentPage != null) {
                System.out.println("     " + currentPage.getId() + " / " + currentPage.getTitle());
            } else {
                System.out.println("     - / - (NULL)");
            }
        }
    }


    @NotNull
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

    private Page createNewPage(Chapter chapter) {

        System.out.println(   "createNewPage");

        Page newPage = Page.create();

        if (Objects.isNull(selectedPage.get())) {
            newPage.setParent(null);
        } else {
            newPage.setParent(selectedPage.get());
        }

        return newPage
                .setChapter(chapter)
                .setTitle("New Page").setDescription("")
                .setContent("<h2>Title</h2><p>Text</p>")
                .save();
    }

}
