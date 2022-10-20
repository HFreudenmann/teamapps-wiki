package org.teamapps.wiki.app.editor;

import org.teamapps.common.format.Color;
import org.teamapps.icon.emoji.EmojiIcon;
import org.teamapps.ux.application.layout.ExtendedLayout;
import org.teamapps.ux.application.perspective.Perspective;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.field.DisplayField;
import org.teamapps.ux.component.field.FieldEditingMode;
import org.teamapps.ux.component.field.richtext.RichTextEditor;
import org.teamapps.ux.component.flexcontainer.VerticalLayout;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;
import org.teamapps.wiki.model.wiki.Page;

import java.util.function.Function;
import java.util.function.Supplier;

public class BookContentView {

    private View contentView;
    private VerticalLayout contentVerticalLayout;
    private DisplayField contentTitleField;
    private DisplayField contentDescriptionField;
    private RichTextEditor contentEditor;

    private DisplayField contentDisplay;
    private DisplayField contentBlockField;

    ToolbarButton saveButton;
    ToolbarButton cancelButton;
    ToolbarButton editButton;
    ToolbarButton editPageSettingsButton;

    PAGE_EDIT_MODE pageEditMode = PAGE_EDIT_MODE.OFF;

    public enum PAGE_EDIT_MODE {
        OFF,
        CONTENT,
        SETTINGS
    }

    public void create(Perspective perspective,
                       Function<String, Page> onPageSaved, Supplier<Page> onPageEditCanceled,
                       Runnable onEditPageContentClicked, Runnable onEditPageSettingsClicked) {
        createBookContentLayout();
        createBookContentView(perspective,
                              onPageSaved, onPageEditCanceled, onEditPageContentClicked, onEditPageSettingsClicked);
    }

    public void setPageEditMode(PAGE_EDIT_MODE newEditMode) {

        System.out.println("   BookContentView.setPageEditMode : " + pageEditMode + " -> " + newEditMode);

        if (pageEditMode == PAGE_EDIT_MODE.OFF || newEditMode == PAGE_EDIT_MODE.OFF) {
            this.pageEditMode = newEditMode;
            setToolbarButtonVisibleState();
        } else {
            System.err.println("   It's not allowed to change from edit mode " + pageEditMode + " to " + newEditMode);
        }
    }


    public void setFocus() {
        contentView.focus();
    }

    public void updateContentView(Page page) {

        if (page == null) {
            return;
        }

        System.out.println("   BookContentView.updateContentView : page " + page.getTitle());

        contentView.getPanel().setTitle(page.getTitle());
        contentTitleField.setValue("<h1>" + page.getTitle() + "</h1>");

        String description = page.getDescription();
        if (description != null) {
            contentDescriptionField.setValue("<p>" + description + "</p>");
            contentDescriptionField.setVisible(true);
        } else {
            contentDescriptionField.setVisible(false);
        }

        if (pageEditMode == PAGE_EDIT_MODE.CONTENT) {
            contentEditor.setValue(page.getContent());
            contentEditor.onValueChanged.addListener(page::setContent); // set content, but not saved

            contentEditor.setVisible(true);
            contentDisplay.setVisible(false);
            contentBlockField.setVisible(true);

            contentEditor.setEditingMode(FieldEditingMode.EDITABLE);
        } else {
            contentDisplay.setValue(page.getContent());

            contentEditor.setVisible(false);
            contentDisplay.setVisible(true);
            contentBlockField.setVisible(true);

            contentEditor.setEditingMode(FieldEditingMode.DISABLED);

            StringBuilder contentBlockBuilder = new StringBuilder();
            page.getContentBlocks().forEach(contentBlock -> {
                switch (contentBlock.getContentBlockType()) {
                    case RICH_TEXT -> {
                        contentBlockBuilder.append(contentBlock.getValue());
                    }
                }

            });
            contentBlockField.setValue(contentBlockBuilder.toString());
        }
    }

    private void createBookContentLayout() {

        contentVerticalLayout = new VerticalLayout();

        contentTitleField = new DisplayField();
        contentTitleField.setShowHtml(true);
        contentVerticalLayout.addComponent(contentTitleField);

        contentDescriptionField = new DisplayField();
        contentDescriptionField.setShowHtml(true);
        contentVerticalLayout.addComponent(contentDescriptionField);

        contentEditor = new RichTextEditor();
        contentEditor.setEditingMode(FieldEditingMode.DISABLED);
        contentVerticalLayout.addComponent(contentEditor);

// ToDo Draft for Content Block editing
////            page.getContentBlocks().forEach(contentBlock -> {
////                switch (contentBlock.getContentBlockType()) {
////                    case RICH_TEXT -> {
////                        RichTextEditor richTextEditor = new RichTextEditor();
////                        richTextEditor.setValue(contentBlock.getValue());
////                        richTextEditor.setEditingMode(FieldEditingMode.EDITABLE);
////                        richTextEditor.setDebuggingId(String.valueOf(contentBlock.getId()));
////                        contentVerticalLayout.addComponent(richTextEditor);
////                    }
////                }
////            });

        contentDisplay = new DisplayField();
        contentDisplay.setShowHtml(true);
        contentVerticalLayout.addComponent(contentDisplay);

        contentBlockField = new DisplayField();
        contentBlockField.setShowHtml(true);
        contentVerticalLayout.addComponent(contentBlockField);
    }

    private void createBookContentView(Perspective perspective,
                                       Function<String, Page> onPageSaved, Supplier<Page> onPageEditCanceled,
                                       Runnable onEditPageContentClicked, Runnable onEditPageSettingsClicked) {

        contentView = perspective.addView(
                View.createView(ExtendedLayout.RIGHT, EmojiIcon.PAGE_FACING_UP, "Content", contentVerticalLayout));
        contentView.getPanel().setBodyBackgroundColor(Color.BLUE.withAlpha(0.34f));
        contentView.getPanel().setPadding(30);
        contentView.getPanel().setStretchContent(false); // Enables vertical scrolling!
        contentView.getPanel().setTitle("");

        ToolbarButtonGroup buttonGroup = contentView.addLocalButtonGroup(new ToolbarButtonGroup());

        saveButton = buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.CHECK_MARK_BUTTON, "Save Changes"));
        saveButton.setVisible(false);
        saveButton.onClick.addListener(() -> {
            System.out.println("saveButton.onClick");

            Page savedPage = onPageSaved.apply(contentEditor.getValue());
            updateContentView(savedPage);
        });

        cancelButton = buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.CROSS_MARK, "Discard Changes"));
        cancelButton.setVisible(false);
        cancelButton.onClick.addListener(() -> {
            System.out.println("cancelButton.onClick");

            Page originalPage = onPageEditCanceled.get();
            updateContentView(originalPage);
        });

        editButton = buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.MEMO, "Edit Page Content"));
        editButton.onClick.addListener(onEditPageContentClicked);

        editPageSettingsButton = buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.WRENCH, "Edit Page Settings"));
        editPageSettingsButton.onClick.addListener(onEditPageSettingsClicked);
    }


    private void setToolbarButtonVisibleState() {

        switch (pageEditMode) {
            case OFF:
                saveButton.setVisible(false);
                cancelButton.setVisible(false);
                editButton.setVisible(true);
                editPageSettingsButton.setVisible(true);
                break;

            case CONTENT:
                saveButton.setVisible(true);
                cancelButton.setVisible(true);
                editButton.setVisible(false);
                editPageSettingsButton.setVisible(false);
                break;

            case SETTINGS:
                saveButton.setVisible(false);
                cancelButton.setVisible(false);
                editButton.setVisible(false);
                editPageSettingsButton.setVisible(false);
                break;
        }
    }

}
