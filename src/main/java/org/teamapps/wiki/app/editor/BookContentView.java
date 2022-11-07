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
import org.teamapps.wiki.app.WikiUtils;
import org.teamapps.wiki.model.wiki.Page;

import java.util.function.Function;

public class BookContentView {

    private View contentView;
    private VerticalLayout contentVerticalLayout;
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
        CONTENT
    }

    public void create(Perspective perspective,
                       Function<String, Void> onPageContentSaveClicked,
                       Runnable onPageContentCancelClicked,
                       Runnable onPageContentEditClicked,
                       Runnable onPageSettingsEditClicked) {
        createBookContentLayout();
        createBookContentView(perspective,
                              onPageContentSaveClicked, onPageContentCancelClicked, onPageContentEditClicked,
                              onPageSettingsEditClicked);
    }

    public void setPageEditMode(PAGE_EDIT_MODE newEditMode) {

        System.out.println("   BCV.setPageEditMode : " + pageEditMode + " -> " + newEditMode);
        this.pageEditMode = newEditMode;
        setToolbarButtonVisibleState();
    }


    public void setFocus() {
        contentView.focus();
    }

    public void updateContentView(Page page) {

        if (page == null) {
            return;
        }

        // System.out.println("   BCV.updateContentView");

        contentView.getPanel().setTitle(page.getTitle());
        contentView.getPanel().setIcon(WikiUtils.getIconFromName(page.getEmoji()));

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
                    case RICH_TEXT -> //noinspection RedundantLabeledSwitchRuleCodeBlock
                    {
                        contentBlockBuilder.append(contentBlock.getValue());
                    }
                }

            });
            contentBlockField.setValue(contentBlockBuilder.toString());
        }
    }

    private void createBookContentLayout() {

        contentVerticalLayout = new VerticalLayout();

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
                                       Function<String, Void> onPageContentSave,
                                       Runnable onPageContentCancel,
                                       Runnable onPageContentEdit,
                                       Runnable onPageSettingsEdit) {

        contentView = perspective.addView(
                View.createView(ExtendedLayout.RIGHT, EmojiIcon.PAGE_FACING_UP, "Content", contentVerticalLayout));
        contentView.getPanel().setBodyBackgroundColor(Color.BLUE.withAlpha(0.34f));
        contentView.getPanel().setPadding(30);
        contentView.getPanel().setStretchContent(false); // Enables vertical scrolling!
        contentView.getPanel().setTitle("");

        ToolbarButtonGroup buttonGroup = contentView.addLocalButtonGroup(new ToolbarButtonGroup());

        saveButton = buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.CHECK_MARK_BUTTON, "Save Changes"));
        saveButton.onClick.addListener(() -> {
            System.out.println("BookContentView.saveButton.onClick");
            onPageContentSave.apply(contentEditor.getValue());
        });

        cancelButton = buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.CROSS_MARK, "Discard Changes"));
        cancelButton.onClick.addListener(() -> {
            System.out.println("BookContentView.cancelButton.onClick");
            onPageContentCancel.run();
        });

        editButton = buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.MEMO, "Edit Page Content"));
        editButton.onClick.addListener(() -> {
            System.out.println("BookContentView.editButton.onClick");
            onPageContentEdit.run();
        });

        editPageSettingsButton = buttonGroup.addButton(ToolbarButton.createTiny(EmojiIcon.WRENCH, "Edit Page Settings"));
        editPageSettingsButton.onClick.addListener(() -> {
            System.out.println("BookContentView.editButton.onClick");
            onPageSettingsEdit.run();
        });

        setPageEditMode(PAGE_EDIT_MODE.OFF);
    }


    private void setToolbarButtonVisibleState() {

        switch (pageEditMode) {
            case OFF -> {
                saveButton.setVisible(false);
                cancelButton.setVisible(false);
                editButton.setVisible(true);
                editPageSettingsButton.setVisible(true);
            }
            case CONTENT -> {
                saveButton.setVisible(true);
                cancelButton.setVisible(true);
                editButton.setVisible(false);
                editPageSettingsButton.setVisible(false);
            }
        }
    }

}
