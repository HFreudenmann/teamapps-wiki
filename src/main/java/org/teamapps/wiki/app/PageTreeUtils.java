package org.teamapps.wiki.app;

import org.jetbrains.annotations.NotNull;
import org.teamapps.wiki.model.wiki.Chapter;
import org.teamapps.wiki.model.wiki.Page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PageTreeUtils {

    public static int getPageLevel(Page page) {
        int level = 0;
        Page parent = page.getParent();
        while (parent != null) {
            level++;
            parent = parent.getParent();
        }
        return level;
    }
    public static boolean isChildPage(Page potentialChildPage, Page page) {
        if (potentialChildPage == null) {
            return false;
        }
        Page parent = potentialChildPage.getParent();
        while (parent != null) {
            if (parent.equals(page)){ return true ; }
            parent = parent.getParent();
        }
        return false;
    }

    @NotNull
    public static List<Page> getTopLevelPages(Chapter chapter) {

        if (Objects.nonNull(chapter)) {
            return chapter.getPages().stream().filter(page -> page.getParent() == null).collect(Collectors.toList());
        } else {
            //noinspection unchecked
            return Collections.EMPTY_LIST;
        }
    }

    // List with correct order of children
    public static List<Page> getSortedPagesOfChapter(Chapter chapter) {

        List<Page> pageList = new ArrayList<>();
        if (Objects.nonNull(chapter)) {
            List<Page> topLevelPages = getTopLevelPages(chapter);
            addSubPagesToList(topLevelPages, pageList);
        }
        return pageList;
    }

    private static void addSubPagesToList(List<Page> nodes, List<Page> pageList) {

        for (Page node : nodes) {
            pageList.add(node);
            addSubPagesToList(node.getChildren(), pageList);
        }
    }

    public static void delete(Page pageToDelete, boolean isCascadingDelete) {

        if (pageToDelete == null) { return; }

        List<Page> childPages = pageToDelete.getChildren();
        System.out.println("   delete : " + pageToDelete.getId() + "   children # : " + childPages.size());
        for (Page childPage : childPages) {
            if (isCascadingDelete) {
                delete(childPage, isCascadingDelete);
            } else {
                childPage.setParent(pageToDelete.getParent());
                childPage.save();
            }
        }
        pageToDelete.delete();
    }

    public static void movePageLevelUp(Page pageToMove) {

        if (pageToMove == null) { return; }

        System.out.println("   movePageLevelUp : id [" + pageToMove.getId() + "]");
        Page parent = pageToMove.getParent();
        if (parent == null) {
            WikiUtils.showWarning("Page is on topmost level. Cannot move page!");
        } else {
            Page newParent = parent.getParent();
            pageToMove.setParent(newParent);
            pageToMove.save();
        }
    }

    public static void movePageLevelDown(Page pageToMove) {

        if (pageToMove == null) { return; }

        System.out.println("   movePageLevelDown : id [" + pageToMove.getId() + "]");
        List<Page> childPages = pageToMove.getChildren();
        if (childPages.size() == 0) {
            WikiUtils.showWarning("Page is on lowest level. Cannot move page!");
        } else {
            Page newParentOfPageToMove = childPages.stream().findFirst().get();
            Page newParentOfChildren = pageToMove.getParent();
            for (Page childPage : childPages) {
                childPage.setParent(newParentOfChildren);
                childPage.save();
            }
            pageToMove.setParent(newParentOfPageToMove);
            pageToMove.save();
        }
    }

    public static void reorderPage(Page page, boolean up) {

        if (page == null) {
            System.err.println("reorderPage : page == null");
            WikiUtils.showWarning("Page is null. Cannot reorder!");
            return;
        }
        
        System.out.println("   reorderPage : id/title = " + page.getId() + "/" + page.getTitle());
        Page parent = page.getParent();
        if (parent == null) {
            System.out.println("   reorderPage : page.Parent == null");
            List<Page> pageList = getTopLevelPages(page.getChapter());
            movePage(page, up, pageList);
            page.getChapter().setPages(pageList).save();
        } else {
            System.out.println("   reorderPage : page.Parent id/title = " + parent.getId() + "/" + parent.getTitle());

            List<Page> pageList = parent.getChildren();
            movePage(page, up, pageList);
            parent.setChildren(pageList).save();
        }
    }

    private static void movePage(Page pageToMove, boolean backwards, List<Page> pageList) {

        if (pageToMove == null || pageList == null) { return; }

        final int lowerBound = 0;
        final int upperBound = pageList.size() - 1;
        int position = 0;
        for (Page page : pageList) {
            if (page.equals(pageToMove)) {
                break;
            }
            position++;
        }
        System.out.println("   movePage : " + (backwards ? "backwards" : "ahead") + ", position=" + position + ", pageList.size()=" + pageList.size());
        if ((backwards && position == lowerBound) || (!backwards && position == upperBound)) {
            WikiUtils.showWarning("Cannot move page beyond the limits!");
            return;
        }
        int newPosition = backwards ? position - 1 : position + 1;
        Collections.swap(pageList, position, newPosition);
    }
    
}
