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
    public static List<Page> getReOrderedPages(Chapter chapter) {

        List<Page> pageList = new ArrayList<>();
        if (Objects.nonNull(chapter)) {
            List<Page> topLevelPages = getTopLevelPages(chapter);
            addPageNodes(topLevelPages, pageList);
        }
        return pageList;
    }

    private static void addPageNodes(List<Page> nodes, List<Page> pageNodes) {

        for (Page node : nodes) {
            pageNodes.add(node);
            addPageNodes(node.getChildren(), pageNodes);
        }
    }

    public static void deleteCascading(Page pageToDelete) {

        if (pageToDelete == null) { return; }

        List<Page> childPages = pageToDelete.getChildren();
        System.out.println("deleteCascading : " + pageToDelete.getId() + "   children : " + childPages.size());
        for (Page childPage : childPages) {
            deleteCascading(childPage);
        }
        pageToDelete.delete();
    }

    public static void reorderPage(Page page, boolean up) {

        if (page == null) {
            System.err.println("reorderPage : page == null");
            WikiUtils.showWarning("Page is null. Cannot reorder!");
            return;
        }

        System.out.println("   reorderPage : id/title = " + page.getId() + "/" + page.getTitle());
        if (page.getParent() == null) {
            System.out.println("   reorderPage : page.Parent == null");
            ArrayList<Page> pageList = new ArrayList<>(getTopLevelPages(page.getChapter()));

            int pos = 0;
            for (Page node : pageList) {
                if (node.equals(page)) {
                    break;
                }
                pos++;
            }
            System.out.println("   up=" + up + ", pos=" + pos + ", pageList.size()=" + pageList.size());
            if ((up && pos == 0) || (!up && pos + 1 == pageList.size())) {
                WikiUtils.showWarning("Cannot move page beyond the limits!");
                return;
            }
            int newPos = up ? pos - 1 : pos + 1;
            Collections.swap(pageList, pos, newPos);
            page.getChapter().setPages(pageList).save();
        } else {
            Page parent = page.getParent();

            System.out.println("   reorderPage : page.Parent id/title = " + parent.getId() + "/" + parent.getTitle());

            ArrayList<Page> pageList = new ArrayList<>(parent.getChildren());
            int pos = 0;
            for (Page node : pageList) {
                if (node.equals(page)) {
                    break;
                }
                pos++;
            }
            System.out.println("   up=" + up + ", pos=" + pos + ", pageList.size()=" + pageList.size());
            if ((up && pos == 0) || (!up && pos + 1 == pageList.size())) {
                WikiUtils.showWarning("Cannot move page beyond the limits!");
                return;
            }
            int newPos = up ? pos - 1 : pos + 1;
            Collections.swap(pageList, pos, newPos);
            parent.setChildren(pageList).save();
        }
    }

}
