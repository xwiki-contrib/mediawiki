/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.mediawiki.syntax.internal.parser.converter;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.contrib.mediawiki.syntax.internal.parser.model.ImageTag;
import org.xwiki.contrib.mediawiki.syntax.internal.parser.model.LinkTag;
import org.xwiki.contrib.mediawiki.syntax.internal.parser.model.XMacroTag;
import org.xwiki.filter.FilterException;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.BulletedListBlock;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.HorizontalLineBlock;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.block.NewLineBlock;
import org.xwiki.rendering.block.NumberedListBlock;
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
import info.bliki.wiki.filter.WPList;
import info.bliki.wiki.filter.WPTable;
import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.model.ImageFormat;
import info.bliki.wiki.tags.ATag;
import info.bliki.wiki.tags.BrTag;
import info.bliki.wiki.tags.HrTag;
import info.bliki.wiki.tags.NowikiTag;
import info.bliki.wiki.tags.PreTag;
import info.bliki.wiki.tags.SourceTag;
import info.bliki.wiki.tags.TableOfContentTag;
import info.bliki.wiki.tags.WPBoldItalicTag;
import info.bliki.wiki.tags.util.INoBodyParsingTag;
import info.bliki.wiki.tags.util.TagStack;

@Component(roles = EventConverter.class)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class EventConverter implements ITextConverter
{
    private static final Map<String, EventGenerator> GENERATOR_MAP = new HashMap<>();

    static {
        GENERATOR_MAP.put(new BrTag().getName(), new OnBlockEventGenerator(new NewLineBlock()));
        GENERATOR_MAP.put(new HrTag().getName(), new OnBlockEventGenerator(new HorizontalLineBlock()));

        GENERATOR_MAP.put(new NowikiTag().getName(), new VerbatimEventGenerator(true));
        GENERATOR_MAP.put(new PreTag().getName(), new VerbatimEventGenerator(false));
        // TODO: BLOCK_MAP.put("math", new MathTag());
        // TODO: BLOCK_MAP.put("embed", new EmbedTag());
        // TODO: BLOCK_MAP.put("ref", new RefTag());
        // TODO: BLOCK_MAP.put("references", new ReferencesTag());

        // see https://www.mediawiki.org/wiki/Extension:SyntaxHighlight
        GENERATOR_MAP.put("syntaxhighlight", new SourceEventGenerator());
        GENERATOR_MAP.put(new SourceTag().getName(), new SourceEventGenerator());
        GENERATOR_MAP.put(Configuration.HTML_CODE_OPEN.getName(), new SourceEventGenerator());

        GENERATOR_MAP.put(new ATag().getName(), new AEventGenerator());

        GENERATOR_MAP.put(Configuration.HTML_H1_OPEN.getName(),
            new BeginEndBlockEventGenerator(new HeaderBlock(Collections.<Block>emptyList(), HeaderLevel.LEVEL1)));
        GENERATOR_MAP.put(Configuration.HTML_H2_OPEN.getName(),
            new BeginEndBlockEventGenerator(new HeaderBlock(Collections.<Block>emptyList(), HeaderLevel.LEVEL2)));
        GENERATOR_MAP.put(Configuration.HTML_H3_OPEN.getName(),
            new BeginEndBlockEventGenerator(new HeaderBlock(Collections.<Block>emptyList(), HeaderLevel.LEVEL3)));
        GENERATOR_MAP.put(Configuration.HTML_H4_OPEN.getName(),
            new BeginEndBlockEventGenerator(new HeaderBlock(Collections.<Block>emptyList(), HeaderLevel.LEVEL4)));
        GENERATOR_MAP.put(Configuration.HTML_H5_OPEN.getName(),
            new BeginEndBlockEventGenerator(new HeaderBlock(Collections.<Block>emptyList(), HeaderLevel.LEVEL5)));
        GENERATOR_MAP.put(Configuration.HTML_H6_OPEN.getName(),
            new BeginEndBlockEventGenerator(new HeaderBlock(Collections.<Block>emptyList(), HeaderLevel.LEVEL6)));

        GENERATOR_MAP.put(Configuration.HTML_EM_OPEN.getName(),
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.BOLD)));
        GENERATOR_MAP.put(Configuration.HTML_ITALIC_OPEN.getName(),
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.ITALIC)));
        GENERATOR_MAP.put(Configuration.HTML_BOLD_OPEN.getName(),
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.BOLD)));
        GENERATOR_MAP.put(new WPBoldItalicTag().getName(), new BoldItalicEventGenerator());
        GENERATOR_MAP.put(Configuration.HTML_STRONG_OPEN.getName(),
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.BOLD)));
        GENERATOR_MAP.put(Configuration.HTML_UNDERLINE_OPEN.getName(),
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.UNDERLINED)));
        GENERATOR_MAP.put(Configuration.HTML_TT_OPEN.getName(),
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.MONOSPACE)));
        GENERATOR_MAP.put(Configuration.HTML_VAR_OPEN.getName(),
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.ITALIC)));
        GENERATOR_MAP.put(Configuration.HTML_SMALL_OPEN.getName(),
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.NONE,
                Collections.singletonMap("style", "font-size:small"))));
        GENERATOR_MAP.put(Configuration.HTML_BIG_OPEN.getName(),
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.NONE,
                Collections.singletonMap("style", "font-size:small"))));

        GENERATOR_MAP.put(Configuration.HTML_PARAGRAPH_OPEN.getName(), new ParagraphEventGenerator());

        GENERATOR_MAP.put(Configuration.HTML_SUB_OPEN.getName(),
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.SUBSCRIPT)));
        GENERATOR_MAP.put(Configuration.HTML_SUP_OPEN.getName(),
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.SUPERSCRIPT)));
        GENERATOR_MAP.put(Configuration.HTML_STRIKE_OPEN.getName(),
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.STRIKEDOUT)));
        GENERATOR_MAP.put(Configuration.HTML_S_OPEN.getName(),
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.STRIKEDOUT)));
        GENERATOR_MAP.put(Configuration.HTML_DEL_OPEN.getName(),
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.STRIKEDOUT)));

        GENERATOR_MAP.put(new WPTable(null).getName(), new WPTableBlockEventGenerator());

        GENERATOR_MAP.put(Configuration.HTML_TABLE_OPEN.getName(),
            new BeginEndBlockEventGenerator(new TableBlock(Collections.<Block>emptyList())));
        GENERATOR_MAP.put(Configuration.HTML_TH_OPEN.getName(),
            new BeginEndBlockEventGenerator(new TableHeadCellBlock(Collections.<Block>emptyList())));
        GENERATOR_MAP.put(Configuration.HTML_TR_OPEN.getName(),
            new BeginEndBlockEventGenerator(new TableRowBlock(Collections.<Block>emptyList())));
        GENERATOR_MAP.put(Configuration.HTML_TD_OPEN.getName(),
            new BeginEndBlockEventGenerator(new TableCellBlock(Collections.<Block>emptyList())));
        // TODO: BLOCK_MAP.put("caption", HTML_CAPTION_OPEN);

        GENERATOR_MAP.put(Configuration.HTML_UL_OPEN.getName(),
            new BeginEndBlockEventGenerator(new BulletedListBlock(Collections.<Block>emptyList())));
        GENERATOR_MAP.put(Configuration.HTML_OL_OPEN.getName(),
            new BeginEndBlockEventGenerator(new NumberedListBlock(Collections.<Block>emptyList())));
        GENERATOR_MAP.put(Configuration.HTML_LI_OPEN.getName(),
            new BeginEndBlockEventGenerator(new ListItemBlock(Collections.<Block>emptyList())));
        GENERATOR_MAP.put(new WPList().getName(),
            new WPListBlockEventGenerator(new ListItemBlock(Collections.<Block>emptyList())));

        // TODO: BLOCK_MAP.put("font", HTML_FONT_OPEN);
        // TODO: BLOCK_MAP.put("center", HTML_CENTER_OPEN);
        GENERATOR_MAP.put(Configuration.HTML_DIV_OPEN.getName(), new BeginEndBlockEventGenerator(new GroupBlock()));
        GENERATOR_MAP.put(Configuration.HTML_SPAN_OPEN.getName(),
            new BeginEndBlockEventGenerator(new FormatBlock(Collections.<Block>emptyList(), Format.NONE)));

        // TODO: BLOCK_MAP.put("abbr", HTML_ABBR_OPEN);
        // TODO: BLOCK_MAP.put("cite", HTML_CITE_OPEN);

        GENERATOR_MAP.put(new LinkTag(null, false).getName(), new LinkEventGenerator());
        GENERATOR_MAP.put(ImageTag.NAME, new ImageEventGenerator());

        GENERATOR_MAP.put(XMacroTag.TAGNAME, new XMacroEventGenerator());
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

    Listener getListener()
    {
        return this.listener;
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
                    traverse(child, model);
                }
            } finally {
                model.decrementRecursionLevel();
            }
        }
    }

    void traverse(Object node, IWikiModel model) throws FilterException
    {
        if (node instanceof BaseToken) {
            traverse((BaseToken) node, model);
        }
    }

    void traverse(TagStack tagStack, IWikiModel model) throws FilterException
    {
        if (tagStack != null) {
            traverse(tagStack.getNodeList(), model);
        }

    }

    @Override
    public void imageNodeToText(TagNode imageTagNode, ImageFormat imageFormat, Appendable resultBuffer,
        IWikiModel model) throws IOException
    {
        // Don't care
    }

    void traverse(BaseToken token, IWikiModel model) throws FilterException
    {
        if (token instanceof TagToken) {
            traverse((TagToken) token, model);
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

    private void traverse(TagToken token, IWikiModel model) throws FilterException
    {
        EventGenerator blockEvent = createEventGenerator(token);

        if (blockEvent != null) {
            blockEvent.traverse(model);
        } else {
            if (token instanceof TagNode) {
                traverse(((TagNode) token).getChildren(), model);
            }
        }
    }

    public EventGenerator createEventGenerator(TagToken token)
    {
        EventGenerator event;
        if (token instanceof TableOfContentTag) {
            event = new TableOfContentEventGenerator();
        } else {
            event = GENERATOR_MAP.get(token.getName());

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
