import org.teamapps.universaldb.schema.*;

public class Model implements SchemaInfoProvider {

    @Override
    public Schema getSchema() {
        Schema schema = Schema.create("org.teamapps.wiki.model");
        schema.setSchemaName("WikiSchema");
        Database db = schema.addDatabase("wiki");

        Table book = db.addTable("book", TableOption.KEEP_DELETED, TableOption.TRACK_CREATION, TableOption.TRACK_MODIFICATION);
        Table chapter = db.addTable("chapter", TableOption.KEEP_DELETED, TableOption.TRACK_CREATION, TableOption.TRACK_MODIFICATION);
        Table page = db.addTable("page", TableOption.KEEP_DELETED, TableOption.TRACK_CREATION, TableOption.TRACK_MODIFICATION);
        Table contentBlock = db.addTable("contentBlock", TableOption.KEEP_DELETED, TableOption.TRACK_CREATION, TableOption.TRACK_MODIFICATION);

        book
                .addText("title")
                .addText("description")
                .addReference("chapters", chapter, true, "book")
        ;

        chapter
                .addText("title")
                .addText("description")
                .addInteger("priority")
                .addReference("book", book, false,"chapters")
                .addReference("pages", page, true,"chapter")
        ;

        page
                .addText("title")
                .addText("linkTitle")
                .addText("description")
                .addReference("parent", page, false, "children")
                .addReference("children", page, true, "parent")
                .addReference("chapter", chapter, false, "pages")
                .addReference("contentBlocks", contentBlock, true, "page")
        ;

        contentBlock
                .addEnum("contentBlockType", "richText")
                .addText("value")
                .addInteger("priority")
                .addReference("page", page, false, "contentBlocks")
        ;


		return schema;
	}
}
