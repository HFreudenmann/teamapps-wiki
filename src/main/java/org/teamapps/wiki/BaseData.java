package org.teamapps.wiki;

import org.teamapps.wiki.model.wiki.Book;
import org.teamapps.wiki.model.wiki.Chapter;
import org.teamapps.wiki.model.wiki.ContentBlock;
import org.teamapps.wiki.model.wiki.ContentBlockType;
import org.teamapps.wiki.model.wiki.Page;

import java.util.List;

public class BaseData {


    public static void createBaseData() {

        if (Book.getCount() > 0) {
            return;
        }

        Book demoBook1 = Book.create().setTitle("Demo Book")
                .setDescription("Automatically created Demo Wiki Book")
                .save();

        Chapter chapter1 = Chapter.create()
                .setTitle("Introduction")
                .setDescription("Introduction into the world of Wiki books")
                .setBook(demoBook1)
                .save();

        Page page1 = Page.create()
                .setTitle("Welcome")
                .setDescription("Welcome to the world of Wiki books")
                .setChapter(chapter1)
                .setContent("<h2>Title</h2>" +
                        "<p>" +
                        "Lorem <i>ipsum</i> dolor sit <b>amet</b>, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum." +
                        "</p>" +
                        "<p>" +
                        "Neque egestas congue quisque egestas. Et pharetra pharetra massa massa. Enim sit amet venenatis urna cursus eget. Curabitur gravida arcu ac tortor dignissim convallis aenean. In vitae turpis massa sed elementum tempus egestas sed sed. Augue eget arcu dictum varius duis at. Iaculis nunc sed augue lacus viverra vitae congue. Malesuada fames ac turpis egestas sed. Arcu non sodales neque sodales ut etiam sit. Nisl condimentum id venenatis a condimentum. Amet nisl purus in mollis nunc sed id semper. Sed blandit libero volutpat sed. Elit eget gravida cum sociis. Cursus eget nunc scelerisque viverra mauris in. Enim sed faucibus turpis in. Purus semper eget duis at tellus. Diam vel quam elementum pulvinar etiam non. Donec pretium vulputate sapien nec sagittis aliquam malesuada bibendum arcu. Feugiat sed lectus vestibulum mattis." +
                        "</p>" +
                        "<h2>Title 2</h2>" +
                        "<p>" +
                        "More Content with <i>ipsum</i> dolor sit <b>amet</b>, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum." +
                        "</p>"

                )
//                .setContentBlocks(List.of(
//                        ContentBlock.create()
//                                .setContentBlockType(ContentBlockType.RICH_TEXT)
//                                .setValue("<h2>Title</h2>" +
//                                        "<p>" +
//                                            "Lorem <i>ipsum</i> dolor sit <b>amet</b>, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum." +
//                                        "</p>" +
//                                        "<p>" +
//                                            "Neque egestas congue quisque egestas. Et pharetra pharetra massa massa. Enim sit amet venenatis urna cursus eget. Curabitur gravida arcu ac tortor dignissim convallis aenean. In vitae turpis massa sed elementum tempus egestas sed sed. Augue eget arcu dictum varius duis at. Iaculis nunc sed augue lacus viverra vitae congue. Malesuada fames ac turpis egestas sed. Arcu non sodales neque sodales ut etiam sit. Nisl condimentum id venenatis a condimentum. Amet nisl purus in mollis nunc sed id semper. Sed blandit libero volutpat sed. Elit eget gravida cum sociis. Cursus eget nunc scelerisque viverra mauris in. Enim sed faucibus turpis in. Purus semper eget duis at tellus. Diam vel quam elementum pulvinar etiam non. Donec pretium vulputate sapien nec sagittis aliquam malesuada bibendum arcu. Feugiat sed lectus vestibulum mattis." +
//                                        "</p>"
//                                ),
//                        ContentBlock.create()
//                                .setContentBlockType(ContentBlockType.RICH_TEXT)
//                                .setValue("<h2>Title of Block 2</h2>" +
//                                        "<p>" +
//                                        "Different Block with <i>ipsum</i> dolor sit <b>amet</b>, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum." +
//                                        "</p>"
//                                )
//                ))
                .save();
        Page page2 = Page.create()
                .setTitle("Basics")
                .setDescription("Basic Structure of a Wiki Book")
                .setChapter(chapter1)
                .setContent(
                        "<h2>Structure</h2>" +
                                "<p>" +
                                "Sapien et ligula ullamcorper malesuada proin libero nunc consequat. Lobortis feugiat vivamus at augue eget arcu dictum varius duis. Nunc aliquet bibendum enim facilisis gravida neque convallis. Blandit turpis cursus in hac habitasse platea dictumst quisque. Tortor dignissim convallis aenean et tortor. Morbi enim nunc faucibus a pellentesque. Eu facilisis sed odio morbi quis commodo odio aenean sed. Nibh cras pulvinar mattis nunc sed. Odio ut enim blandit volutpat maecenas volutpat blandit aliquam. Sit amet nisl purus in mollis nunc sed. Luctus accumsan tortor posuere ac ut consequat semper viverra nam. Magna fringilla urna porttitor rhoncus. Morbi tempus iaculis urna id. Auctor urna nunc id cursus metus aliquam." +
                                "</p>" +
                                "<p>" +
                                "Eget duis at tellus at urna condimentum. Aliquet bibendum enim facilisis gravida neque convallis a. Vestibulum mattis ullamcorper velit sed ullamcorper morbi tincidunt ornare massa. Massa tempor nec feugiat nisl pretium fusce id. A scelerisque purus semper eget. Arcu non sodales neque sodales. Magna etiam tempor orci eu lobortis elementum. Arcu felis bibendum ut tristique. Ac orci phasellus egestas tellus rutrum tellus. Ipsum suspendisse ultrices gravida dictum fusce ut placerat orci nulla." +
                                "</p>"
                )
//                .setContentBlocks(List.of(
//                        ContentBlock.create()
//                                .setContentBlockType(ContentBlockType.RICH_TEXT)
//                                .setValue("<h2>Structure</h2>" +
//                                        "<p>" +
//                                        "Sapien et ligula ullamcorper malesuada proin libero nunc consequat. Lobortis feugiat vivamus at augue eget arcu dictum varius duis. Nunc aliquet bibendum enim facilisis gravida neque convallis. Blandit turpis cursus in hac habitasse platea dictumst quisque. Tortor dignissim convallis aenean et tortor. Morbi enim nunc faucibus a pellentesque. Eu facilisis sed odio morbi quis commodo odio aenean sed. Nibh cras pulvinar mattis nunc sed. Odio ut enim blandit volutpat maecenas volutpat blandit aliquam. Sit amet nisl purus in mollis nunc sed. Luctus accumsan tortor posuere ac ut consequat semper viverra nam. Magna fringilla urna porttitor rhoncus. Morbi tempus iaculis urna id. Auctor urna nunc id cursus metus aliquam." +
//                                        "</p>" +
//                                        "<p>" +
//                                        "Eget duis at tellus at urna condimentum. Aliquet bibendum enim facilisis gravida neque convallis a. Vestibulum mattis ullamcorper velit sed ullamcorper morbi tincidunt ornare massa. Massa tempor nec feugiat nisl pretium fusce id. A scelerisque purus semper eget. Arcu non sodales neque sodales. Magna etiam tempor orci eu lobortis elementum. Arcu felis bibendum ut tristique. Ac orci phasellus egestas tellus rutrum tellus. Ipsum suspendisse ultrices gravida dictum fusce ut placerat orci nulla." +
//                                        "</p>"
//                                )
//                ))
                .save();
        Page page2subpage1 = Page.create()
                .setParent(page2)
                .setTitle("Editing")
                .setDescription("Editing a Wiki Book")
                .setChapter(chapter1)
                .setContent(
                        "<h2>Editing</h2>" +
                        "<p>" +
                        "Quis <b>hendrerit</b> dolor magna eget est lorem. Metus vulputate eu scelerisque felis. Eu facilisis sed odio morbi quis commodo odio aenean sed. Condimentum vitae sapien pellentesque habitant morbi tristique senectus. Sit amet nulla facilisi morbi tempus. Pharetra diam sit amet nisl suscipit adipiscing bibendum est ultricies. Donec massa sapien faucibus et molestie ac. Porttitor eget dolor morbi non arcu risus quis. Diam vel quam elementum pulvinar. Erat pellentesque adipiscing commodo elit at imperdiet. Mauris in aliquam sem fringilla ut morbi tincidunt. Vitae suscipit tellus mauris a diam maecenas sed enim ut. Nisi vitae suscipit tellus mauris a diam. Eget aliquet nibh praesent tristique magna sit amet purus. Lectus vestibulum mattis ullamcorper velit. Purus in mollis nunc sed." +
                        "</p>"
                )
                .save();


        Chapter chapter2 = Chapter.create()
                .setTitle("Features")
                .setDescription("Features of a Wiki Book")
                .setBook(demoBook1)
                .save();
        Page c2page1 = Page.create()
                .setTitle("Rich Text")
                .setDescription("Text Editing and Formatting")
                .setChapter(chapter2)
                .setContent(
                        "<h2>How To Format a Text</h2>" +
                        "<p>" +
                        "Sapien et ligula ullamcorper malesuada proin libero nunc consequat. Lobortis feugiat vivamus at augue eget arcu dictum varius duis. Nunc aliquet bibendum enim facilisis gravida neque convallis. Blandit turpis cursus in hac habitasse platea dictumst quisque. Tortor dignissim convallis aenean et tortor. Morbi enim nunc faucibus a pellentesque. Eu facilisis sed odio morbi quis commodo odio aenean sed. Nibh cras pulvinar mattis nunc sed. Odio ut enim blandit volutpat maecenas volutpat blandit aliquam. Sit amet nisl purus in mollis nunc sed. Luctus accumsan tortor posuere ac ut consequat semper viverra nam. Magna fringilla urna porttitor rhoncus. Morbi tempus iaculis urna id. Auctor urna nunc id cursus metus aliquam." +
                        "</p>" +
                        "<p>" +
                        "Eget duis at tellus at urna condimentum. Aliquet bibendum enim facilisis gravida neque convallis a. Vestibulum mattis ullamcorper velit sed ullamcorper morbi tincidunt ornare massa. Massa tempor nec feugiat nisl pretium fusce id. A scelerisque purus semper eget. Arcu non sodales neque sodales. Magna etiam tempor orci eu lobortis elementum. Arcu felis bibendum ut tristique. Ac orci phasellus egestas tellus rutrum tellus. Ipsum suspendisse ultrices gravida dictum fusce ut placerat orci nulla." +
                        "</p>"
                )
                .save();

        Book book2 = Book.create().setTitle("Book Number Two")
                .setDescription("Another Book")
                .save();

        Chapter b2chapter1 = Chapter.create()
                .setTitle("Chapter One")
                .setDescription("Not Chapter Two")
                .setBook(book2)
                .save();

        Page b2page1 = Page.create()
                .setTitle("Hello Wiki")
                .setDescription("Description")
                .setChapter(b2chapter1)
                .setContent(
                        "<h2>Title</h2>" +
                        "<p>" +
                        "Lorem <i>ipsum</i> dolor sit <b>amet</b>, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum." +
                        "</p>" +
                        "<p>" +
                        "Neque egestas congue quisque egestas. Et pharetra pharetra massa massa. Enim sit amet venenatis urna cursus eget. Curabitur gravida arcu ac tortor dignissim convallis aenean. In vitae turpis massa sed elementum tempus egestas sed sed. Augue eget arcu dictum varius duis at. Iaculis nunc sed augue lacus viverra vitae congue. Malesuada fames ac turpis egestas sed. Arcu non sodales neque sodales ut etiam sit. Nisl condimentum id venenatis a condimentum. Amet nisl purus in mollis nunc sed id semper. Sed blandit libero volutpat sed. Elit eget gravida cum sociis. Cursus eget nunc scelerisque viverra mauris in. Enim sed faucibus turpis in. Purus semper eget duis at tellus. Diam vel quam elementum pulvinar etiam non. Donec pretium vulputate sapien nec sagittis aliquam malesuada bibendum arcu. Feugiat sed lectus vestibulum mattis." +
                        "</p>" +
                        "<h2>Title 2</h2>" +
                        "<p>" +
                        "More Content with <i>ipsum</i> dolor sit <b>amet</b>, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum." +
                        "</p>"
                )
                .save();
    }
}
