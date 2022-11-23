package org.teamapps.wiki.app;

import org.teamapps.wiki.model.wiki.Chapter;
import org.teamapps.wiki.model.wiki.Page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    public static List<Page> getTopLevelPages(Chapter chapter) {

        if (chapter != null) {
            return chapter.getPages().stream().filter(page -> page.getParent() == null).collect(Collectors.toList());
        } else {
            //noinspection unchecked
            return Collections.EMPTY_LIST;
        }
    }

    // List with correct order of children
    public static List<Page> getSortedPagesOfChapter(Chapter chapter) {

        List<Page> pageList = new ArrayList<>();
        if (chapter != null) {
            List<Page> topLevelPages = getTopLevelPages(chapter);
            addSubPagesToList(topLevelPages, pageList);
        } else {
            System.out.println("getSortedPagesOfChapter: chapter is null");
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

    public static void movePageLevelUp(Page pageToMove, Chapter currentChapter) {

        if (pageToMove == null) { return; }

        System.out.println("   movePageLevelUp : id [" + pageToMove.getId() + "]");
        Page parent = pageToMove.getParent();
        if (parent == null) {
            WikiUtils.showWarning("Page is on topmost level. Cannot move page!");
        } else {
            Page newParent = parent.getParent();
            pageToMove.setParent(newParent)
                      .setChapter(currentChapter)
                      .save();
//            if (newParent == null) {
//                if (currentChapter == null) {
//                    System.err.println("   movePageLevelUp : current chapter is null!");
//                } else {
//                    ArrayList<Page> pageList = new ArrayList<>(getTopLevelPages(currentChapter));
//                    logList("       (Top level pages) : ", pageList);
//                }
//            }
        }
    }

    public static void movePageLevelDown(Page pageToMove, Chapter currentChapter) {

        if (pageToMove == null) { return; }

        System.out.println("   movePageLevelDown : id [" + pageToMove.getId() + "]");
        List<Page> childPages = pageToMove.getChildren();
        if (childPages.size() == 0) {
            WikiUtils.showWarning("Page is on lowest level. Cannot move page!");
        } else {
            Page newParentOfPageToMove = childPages.stream().findFirst().get();
            Page newParentOfChildren = pageToMove.getParent();
//            if (newParentOfChildren == null) {
//                if (currentChapter == null) {
//                    System.err.println("   movePageLevelDown : chapter is null!");
//                } else {
//                    ArrayList<Page> pageList = new ArrayList<>(getTopLevelPages(currentChapter));
//                    logList("      Before (Top level pages) : ", pageList);
//                    logList("              add child pages  : ", new ArrayList<>(childPages));
//                    System.out.println("              remove page      : " + pageToMove.getId());
//                    pageList.addAll(childPages);
//                    pageList.remove(pageToMove);
//                    logList("      After  (Top level + child pages) : ", pageList);
//                    currentChapter.setPages(pageList).save();
//                }
//            }
            for (Page childPage : childPages) {
                childPage.setParent(newParentOfChildren);
                childPage.save();
            }
            pageToMove.setParent(newParentOfPageToMove);
            pageToMove.save();
        }
    }

    private static void logList(String s, ArrayList<Page> pageList) {
        System.out.print(s + "#" + pageList.size() + "  [");
        for (Page page : pageList) {
            System.out.print(page.getId() + "  ");
        }
        System.out.println("]");
    }

    public static void reorderPage(Page page, boolean up) {

        if (page == null) {
            System.err.println("reorderPage : page == null");
            WikiUtils.showWarning("Page is null. Cannot reorder!");
            return;
        }
        
        System.out.println("   reorderPage : id/title = " + page.getId() + "/" + page.getTitle());
        ArrayList<Page> pageList;
        Page parent = page.getParent();
        if (parent == null) {
            System.out.println("               : page.Parent == null");

            ArrayList<Page> topLevelPages = new ArrayList<>(getTopLevelPages(page.getChapter()));
            logList("      Before (Top level pages) : ", topLevelPages);
            movePage(page, up, topLevelPages);
            logList("      After  (Top level pages) : ", topLevelPages);

            pageList = new ArrayList<>();
            addSubPagesToList(topLevelPages, pageList);

            page.getChapter().setPages(pageList).save();
        } else {
            System.out.println("               : page.Parent = " + parent.getId() + "/'" + parent.getTitle() + "'");

            pageList = new ArrayList<>(parent.getChildren());
            logList("      Before (children) : ", pageList);
            movePage(page, up, pageList);
            logList("      After  (children) : ", pageList);
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

//    private static void saveChapter(Page page, String methodName) {
//        Chapter chapter = page.getChapter();
//        if (chapter != null) {
//            chapter.save();
//        } else {
//            System.err.println(methodName + ": Saving chapter failed, because chapter is null!");
//        }
//    }

}
