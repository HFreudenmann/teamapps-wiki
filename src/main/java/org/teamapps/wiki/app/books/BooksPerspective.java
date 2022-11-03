package org.teamapps.wiki.app.books;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.perspective.AbstractApplicationPerspective;
import org.teamapps.common.format.Color;
import org.teamapps.databinding.MutableValue;
import org.teamapps.icon.emoji.EmojiIcon;
import org.teamapps.ux.application.layout.ExtendedLayout;
import org.teamapps.ux.application.perspective.Perspective;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.infiniteitemview.InfiniteItemView2;
import org.teamapps.ux.component.infiniteitemview.ListInfiniteItemViewModel;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.session.CurrentSessionContext;
import org.teamapps.wiki.model.wiki.Book;

import java.util.HashMap;
import java.util.Map;

public class BooksPerspective extends AbstractApplicationPerspective {
    private View navigationView;
    private View booksView;

    public BooksPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
            super(applicationInstanceData, perspectiveInfoBadgeValue);
            createUi();
        }

        private void createUi() {
            Perspective perspective = getPerspective();
            booksView = perspective.addView(View.createView(ExtendedLayout.RIGHT, EmojiIcon.PAGE_FACING_UP, "Content II", null));

            booksView.getPanel().setBodyBackgroundColor(Color.LIGHT_GREEN.withAlpha(0.94f));
            booksView.getPanel().setPadding(20);

            updateBooksView();
    }

    private void updateBooksView() {
        InfiniteItemView2<Book> bookItemView = new InfiniteItemView2<>();
        bookItemView.setModel(new ListInfiniteItemViewModel<>(Book.getAll())); // Todo Model with query
        bookItemView.setItemTemplate(BaseTemplate.LIST_ITEM_VERY_LARGE_ICON_TWO_LINES);
        bookItemView.setItemHeight(170.0f);
        bookItemView.setItemPropertyProvider((book, propertyNames) -> {
                Map<String, Object> map = new HashMap<>();
                map.put(BaseTemplate.PROPERTY_ICON, EmojiIcon.CLOSED_BOOK);
                map.put(BaseTemplate.PROPERTY_CAPTION, book.getTitle());
                map.put(BaseTemplate.PROPERTY_DESCRIPTION, book.getDescription());
                return map;
        });
        booksView.setComponent(bookItemView);
        bookItemView.onItemClicked.addListener(eventData -> {
            Book book = eventData.getRecord();
            CurrentSessionContext.get().showNotification(EmojiIcon.CLOSED_BOOK, book.getTitle());
        });
    }
}
