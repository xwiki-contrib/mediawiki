package org.xwiki.contrib.mediawiki.syntax.internal.parser.converter;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.filter.FilterException;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.BulletedListBlock;
import org.xwiki.rendering.block.DefinitionListBlock;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.HorizontalLineBlock;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.block.NewLineBlock;
import org.xwiki.rendering.block.NumberedListBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.TableBlock;
import org.xwiki.rendering.block.TableCellBlock;
import org.xwiki.rendering.block.TableHeadCellBlock;
import org.xwiki.rendering.block.TableRowBlock;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.InlineFilterListener;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.StreamParser;

import info.bliki.htmlcleaner.BaseToken;
import info.bliki.htmlcleaner.ContentToken;
import info.bliki.htmlcleaner.TagNode;
import info.bliki.htmlcleaner.TagToken;
import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.filter.WPCell;
import info.bliki.wiki.filter.WPList;
import info.bliki.wiki.filter.WPList.InternalList;
import info.bliki.wiki.filter.WPListElement;
import info.bliki.wiki.filter.WPRow;
import info.bliki.wiki.filter.WPTable;
import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.model.ImageFormat;
import info.bliki.wiki.tags.WPBoldItalicTag;
import info.bliki.wiki.tags.util.INoBodyParsingTag;
import info.bliki.wiki.tags.util.TagStack;

@Component(roles = EventConverter.class)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class EventConverter implements ITextConverter
{
    private static final Map<String, EventGenerator> BLOCK_MAP = new HashMap<>();

    static {
        BLOCK_MAP.put("br", new OnBlockEventGenerator(new NewLineBlock()));
        BLOCK_MAP.put("hr", new OnBlockEventGenerator(new HorizontalLineBlock()));

        BLOCK_MAP.put("nowiki", new VerbatimEventGenerator(true));
        BLOCK_MAP.put("pre", new VerbatimEventGenerator(false));
        // TODO: BLOCK_MAP.put("math", new MathTag());
        // TODO: BLOCK_MAP.put("embed", new EmbedTag());
        // TODO: BLOCK_MAP.put("ref", new RefTag());
        // TODO: BLOCK_MAP.put("references", new ReferencesTag());

        // see https://www.mediawiki.org/wiki/Extension:SyntaxHighlight
        BLOCK_MAP.put("syntaxhighlight", new SourceEventGenerator());
        BLOCK_MAP.put("source", new SourceEventGenerator());
        BLOCK_MAP.put("code", new SourceEventGenerator());

        // TODO: BLOCK_MAP.put("a", HTML_A_OPEN);
        BLOCK_MAP.put("h1",
            new BeginEndBlockEventGenerator(new HeaderBlock(Collections.<Block>emptyList(), HeaderLevel.LEVEL1)));
        BLOCK_MAP.put("h2",
            new BeginEndBlockEventGenerator(new HeaderBlock(Collections.<Block>emptyList(), HeaderLevel.LEVEL2)));
        BLOCK_MAP.put("h3",
            new BeginEndBlockEventGenerator(new HeaderBlock(Collections.<Block>emptyList(), HeaderLevel.LEVEL3)));
        BLOCK_MAP.put("h4",
            new BeginEndBlockEventGenerator(new HeaderBlock(Collections.<Block>emptyList(), HeaderLevel.LEVEL4)));
        BLOCK_MAP.put("h5",
            new BeginEndBlockEventGenerator(new HeaderBlock(Collections.<Block>emptyList(), HeaderLevel.LEVEL5)));
        BLOCK_MAP.put("h6",
            new BeginEndBlockEventGenerator(new HeaderBlock(Collections.<Block>emptyList(), HeaderLevel.LEVEL6)));

        BLOCK_MAP.put("em",
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.BOLD)));
        BLOCK_MAP.put("i",
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.ITALIC)));
        BLOCK_MAP.put("b",
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.BOLD)));
        BLOCK_MAP.put("bi", new BoldItalicEventGenerator());
        BLOCK_MAP.put("strong",
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.BOLD)));
        BLOCK_MAP.put("u",
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.UNDERLINED)));
        BLOCK_MAP.put("tt",
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.MONOSPACE)));

        BLOCK_MAP.put("p", new BeginEndBlockEventGenerator(new ParagraphBlock(Collections.<Block>emptyList())));

        // TODO: BLOCK_MAP.put("blockquote", HTML_BLOCKQUOTE_OPEN);

        // TODO: BLOCK_MAP.put("var", HTML_VAR_OPEN);
        // TODO: BLOCK_MAP.put("s", HTML_S_OPEN);
        // TODO: BLOCK_MAP.put("small", HTML_SMALL_OPEN);
        // TODO: BLOCK_MAP.put("big", HTML_BIG_OPEN);
        // TODO: BLOCK_MAP.put("del", HTML_DEL_OPEN);

        BLOCK_MAP.put("sub",
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.SUBSCRIPT)));
        BLOCK_MAP.put("sup",
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.SUPERSCRIPT)));
        BLOCK_MAP.put("strike",
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.STRIKEDOUT)));

        BLOCK_MAP.put("{||}", new BeginEndBlockEventGenerator(new TableBlock(Collections.<Block>emptyList())));

        BLOCK_MAP.put("table", new BeginEndBlockEventGenerator(new TableBlock(Collections.<Block>emptyList())));
        BLOCK_MAP.put("th", new BeginEndBlockEventGenerator(new TableHeadCellBlock(Collections.<Block>emptyList())));
        BLOCK_MAP.put("tr", new BeginEndBlockEventGenerator(new TableRowBlock(Collections.<Block>emptyList())));
        BLOCK_MAP.put("td", new BeginEndBlockEventGenerator(new TableCellBlock(Collections.<Block>emptyList())));
        // TODO: BLOCK_MAP.put("caption", HTML_CAPTION_OPEN);

        BLOCK_MAP.put("ul", new BeginEndBlockEventGenerator(new BulletedListBlock(Collections.<Block>emptyList())));
        BLOCK_MAP.put("ol", new BeginEndBlockEventGenerator(new NumberedListBlock(Collections.<Block>emptyList())));
        BLOCK_MAP.put("li", new BeginEndBlockEventGenerator(new ListItemBlock(Collections.<Block>emptyList())));

        BLOCK_MAP.put("dl", new BeginEndBlockEventGenerator(new DefinitionListBlock(Collections.<Block>emptyList())));

        // TODO: BLOCK_MAP.put("font", HTML_FONT_OPEN);
        // TODO: BLOCK_MAP.put("center", HTML_CENTER_OPEN);
        BLOCK_MAP.put("div", new BeginEndBlockEventGenerator(new GroupBlock()));
        BLOCK_MAP.put("span",
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.NONE)));

        // TODO: BLOCK_MAP.put("abbr", HTML_ABBR_OPEN);
        // TODO: BLOCK_MAP.put("cite", HTML_CITE_OPEN);
    }

    @Inject
    @Named("plain/1.0")
    private StreamParser plainParser;

    @Inject
    private Logger logger;

    private Listener listener;

    public EventConverter()
    {
    }

    public void init(Listener listener)
    {
        this.listener = listener;
    }

    @Override
    public void nodesToText(List<? extends Object> nodes, Appendable resultBuffer, IWikiModel model) throws IOException
    {
        try {
            traverse(nodes, model);
        } catch (FilterException e) {
            new IOException("Failed to send event", e);
        }
    }

    void traverse(List<? extends Object> nodes, IWikiModel model) throws FilterException
    {
        if (nodes != null && !nodes.isEmpty()) {
            try {
                int level = model.incrementRecursionLevel();

                if (level > Configuration.RENDERER_RECURSION_LIMIT) {
                    // resultBuffer.append(
                    // "<span class=\"error\">Error - recursion limit exceeded</span>");
                    // TODO: throw an error ?
                    return;
                }

                for (Object child : nodes) {
                    if (child instanceof BaseToken) {
                        onBaseToken((BaseToken) child, model);
                    }
                }
            } finally {
                model.decrementRecursionLevel();
            }
        }
    }

    private void traverse(TagStack tagStack, IWikiModel model) throws FilterException
    {
        if (tagStack != null) {
            traverse(tagStack.getNodeList(), model);
        }

    }

    @Override
    public void imageNodeToText(TagNode imageTagNode, ImageFormat imageFormat, Appendable resultBuffer,
        IWikiModel model) throws IOException
    {

    }

    private void onWPList(WPList tag, IWikiModel model) throws FilterException
    {
        traverse(tag.getNestedElements(), model);
    }

    private BeginEndBlockEventGenerator createListBlockEvent(InternalList list)
    {
        switch (list.getChar()) {
            case WPList.UL_CHAR:
                return new BeginEndBlockEventGenerator(new BulletedListBlock(Collections.<Block>emptyList()));
            case WPList.OL_CHAR:
                return new BeginEndBlockEventGenerator(new NumberedListBlock(Collections.<Block>emptyList()));
            default:
                return new BeginEndBlockEventGenerator(new DefinitionListBlock(Collections.<Block>emptyList()));
        }
    }

    private BeginEndBlockEventGenerator createListElementBlockEvent(WPListElement item)
    {
        return new BeginEndBlockEventGenerator(new ListItemBlock(Collections.<Block>emptyList()));
    }

    private void onInternalList(InternalList list, IWikiModel model) throws FilterException
    {
        BeginEndBlockEventGenerator blockEvent = createListBlockEvent(list);

        blockEvent.begin(this.listener);

        traverse(list, model);

        blockEvent.end(this.listener);
    }

    private void traverse(InternalList list, IWikiModel model) throws FilterException
    {
        for (Object element : list) {
            if (element instanceof InternalList) {
                onInternalList((InternalList) element, model);
            } else {
                onWPListElement((WPListElement) element, model);
            }
        }
    }

    private void onWPListElement(WPListElement listeElement, IWikiModel model) throws FilterException
    {
        this.listener.beginListItem();

        traverse(listeElement.getTagStack(), model);

        this.listener.endListItem();
    }

    private void onWPTable(WPTable table, IWikiModel model) throws FilterException
    {
        this.listener.beginTable(table.getAttributes());

        for (int i = 0; i < table.getRowsSize(); ++i) {
            onWPRow(table.get(i), model);
        }

        this.listener.endTable(table.getAttributes());
    }

    private void onWPRow(WPRow row, IWikiModel model) throws FilterException
    {
        Map<String, String> attributes = getWPRowAttributes(row);

        this.listener.beginTableRow(attributes);

        for (int i = 0; i < row.getNumColumns(); ++i) {
            onWPCell(row.get(i), model);
        }

        this.listener.endTableRow(attributes);
    }

    private void onWPCell(WPCell cell, IWikiModel model) throws FilterException
    {
        Map<String, String> attributes = cell.getNodeAttributes();

        switch (cell.getType()) {
            case WPCell.CAPTION:
                // TODO: add support for table caption in Rendering API
                break;

            case WPCell.TH:
                this.listener.beginTableHeadCell(attributes);
                traverse(cell.getTagStack(), model);
                this.listener.endTableHeadCell(attributes);
                break;

            default:
                this.listener.beginTableCell(attributes);
                traverse(cell.getTagStack(), model);
                this.listener.endTableCell(attributes);
                break;
        }
    }

    // FIXME: getrid of this hack when
    // https://bitbucket.org/axelclk/info.bliki.wiki/pull-requests/3/allow-accessing-wprow-attributes is applied
    private Map<String, String> getWPRowAttributes(WPRow row)
    {
        try {
            return (Map<String, String>) FieldUtils.readDeclaredField(row, "fAttributes", true);
        } catch (Exception e) {
            return Listener.EMPTY_PARAMETERS;
        }
    }

    private void onWPBoldItalicTag(WPBoldItalicTag tag, IWikiModel model)
    {

    }

    private void onBaseToken(BaseToken token, IWikiModel model) throws FilterException
    {
        if (token instanceof TagToken) {
            onTagToken((TagToken) token, model);
        } else if (token instanceof ContentToken) {
            try {
                InlineFilterListener inlineListener = new InlineFilterListener();
                inlineListener.setWrappedListener(this.listener);

                this.plainParser.parse(new StringReader(((ContentToken) token).getContent()), inlineListener);
            } catch (ParseException e) {
                // TODO: log something ?
            }

        }
    }

    private void onTagToken(TagToken token, IWikiModel model) throws FilterException
    {
        if (token instanceof WPList) {
            onWPList((WPList) token, model);
        } else if (token instanceof WPTable) {
            onWPTable((WPTable) token, model);
        } else {
            EventGenerator blockEvent = createBlockEvent(token);

            if (blockEvent != null) {
                blockEvent.traverse(this.listener, model);
            } else {
                if (token instanceof TagNode) {
                    traverse(((TagNode) token).getChildren(), model);
                }
            }
        }
    }

    public EventGenerator createBlockEvent(TagToken token)
    {
        EventGenerator event = BLOCK_MAP.get(token.getName());

        if (event != null) {
            try {
                event = event.clone();
            } catch (CloneNotSupportedException e) {
                this.logger.error("Failed to clone block event [{}]", event);

                return null;
            }
        } else if (token instanceof INoBodyParsingTag) {
            event = new MacroEventGenerator();
        } else {
            event = new UnknownEventGenerator();
        }

        // Initialize the block event
        event.init(token, this);

        return event;
    }

    @Override
    public boolean renderLinks()
    {
        return true;
    }
}
