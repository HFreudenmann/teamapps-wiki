package org.teamapps.wiki.app.books;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.perspective.AbstractApplicationPerspective;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
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
            PerspectiveSessionData perspectiveSessionData = (PerspectiveSessionData) getApplicationInstanceData();

            createUi();
        }

        private void createUi() {
            Perspective perspective = getPerspective();
            navigationView = perspective.addView(View.createView(ExtendedLayout.LEFT, EmojiIcon.COMPASS, "Book Navigation", null));
            booksView = perspective.addView(View.createView(ExtendedLayout.CENTER, EmojiIcon.PAGE_FACING_UP, "Inhalt", null));

            navigationView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.84f));
            booksView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.84f));
            booksView.getPanel().setPadding(10);

            updateBooksView();

    }

    private void updateBooksView() {
        InfiniteItemView2<Book> bookItemView = new InfiniteItemView2<>();
        bookItemView.setModel(new ListInfiniteItemViewModel<>(Book.getAll())); // Todo Model with query
        bookItemView.setItemTemplate(BaseTemplate.BUTTON_XLARGE);
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
