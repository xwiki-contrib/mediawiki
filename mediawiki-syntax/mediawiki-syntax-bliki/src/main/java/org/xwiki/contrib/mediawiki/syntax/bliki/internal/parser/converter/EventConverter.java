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
package org.xwiki.contrib.mediawiki.syntax.bliki.internal.parser.converter;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.mediawiki.syntax.MediaWikiSyntaxInputProperties;
import org.xwiki.contrib.mediawiki.syntax.bliki.internal.parser.BlikiMediaWikiStreamParser;
import org.xwiki.contrib.mediawiki.syntax.bliki.internal.parser.model.GalleryXMacroTag;
import org.xwiki.contrib.mediawiki.syntax.bliki.internal.parser.model.ImageTag;
import org.xwiki.contrib.mediawiki.syntax.bliki.internal.parser.model.LinkTag;
import org.xwiki.contrib.mediawiki.syntax.bliki.internal.parser.model.XMacroTag;
import org.xwiki.filter.FilterException;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.BulletedListBlock;
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.rendering.block.FigureCaptionBlock;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.GroupBlock;
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
import info.bliki.htmlcleaner.Utils;
import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.filter.WPList;
import info.bliki.wiki.filter.WPTable;
import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.model.ImageFormat;
import info.bliki.wiki.tags.ATag;
import info.bliki.wiki.tags.BrTag;
import info.bliki.wiki.tags.HTMLTag;
import info.bliki.wiki.tags.HrTag;
import info.bliki.wiki.tags.MathTag;
import info.bliki.wiki.tags.NowikiTag;
import info.bliki.wiki.tags.PreTag;
import info.bliki.wiki.tags.RefTag;
import info.bliki.wiki.tags.ReferencesTag;
import info.bliki.wiki.tags.SourceTag;
import info.bliki.wiki.tags.TableOfContentTag;
import info.bliki.wiki.tags.TemplateTag;
import info.bliki.wiki.tags.WPBoldItalicTag;
import info.bliki.wiki.tags.util.INoBodyParsingTag;
import info.bliki.wiki.tags.util.TagStack;

@Component(roles = EventConverter.class)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class EventConverter implements ITextConverter, Initializable
{
    private final Map<String, EventGenerator> generatorMap = new HashMap<>();

    @Inject
    @Named("plain/1.0")
    private StreamParser plainParser;

    @Inject
    @Named(BlikiMediaWikiStreamParser.SYNTAX_STRING)
    private StreamParser mediaWikiParser;

    @Inject
    private ComponentManager componentManager;

    @Inject
    private Logger logger;

    private Listener listener;

    private MediaWikiSyntaxInputProperties properties;

    @Override
    public void initialize() throws InitializationException
    {
        this.generatorMap.put(new BrTag().getName(), new OnBlockEventGenerator<BrTag>(new NewLineBlock()));
        this.generatorMap.put(new HrTag().getName(), new OnBlockEventGenerator<HrTag>(new HorizontalLineBlock()));

        this.generatorMap.put(new NowikiTag().getName(), new VerbatimEventGenerator(true));
        this.generatorMap.put(new PreTag().getName(), new VerbatimEventGenerator(false));
        this.generatorMap.put(new MathTag().getName(), new FormulaMacroEventGenerator());

        this.generatorMap.put(new GalleryXMacroTag().getName(), new GalleryEventGenerator());

        this.generatorMap.put(new RefTag().getName(), new MacroEventGenerator("footnote", true));
        this.generatorMap.put(new ReferencesTag().getName(), new MacroEventGenerator("putFootnotes", false));

        // see https://www.mediawiki.org/wiki/Extension:SyntaxHighlight
        this.generatorMap.put("syntaxhighlight", new SourceEventGenerator());
        this.generatorMap.put(new SourceTag().getName(), new SourceEventGenerator());
        this.generatorMap.put(Configuration.HTML_CODE_OPEN.getName(), new SourceEventGenerator());

        this.generatorMap.put(new ATag().getName(), new AEventGenerator());

        this.generatorMap.put(Configuration.HTML_H1_OPEN.getName(), new HeaderEventGenerator(HeaderLevel.LEVEL1));
        this.generatorMap.put(Configuration.HTML_H2_OPEN.getName(), new HeaderEventGenerator(HeaderLevel.LEVEL2));
        this.generatorMap.put(Configuration.HTML_H3_OPEN.getName(), new HeaderEventGenerator(HeaderLevel.LEVEL3));
        this.generatorMap.put(Configuration.HTML_H4_OPEN.getName(), new HeaderEventGenerator(HeaderLevel.LEVEL4));
        this.generatorMap.put(Configuration.HTML_H5_OPEN.getName(), new HeaderEventGenerator(HeaderLevel.LEVEL5));
        this.generatorMap.put(Configuration.HTML_H6_OPEN.getName(), new HeaderEventGenerator(HeaderLevel.LEVEL6));

        this.generatorMap.put(Configuration.HTML_EM_OPEN.getName(),
            new BeginEndBlockEventGenerator<HTMLTag>(new FormatBlock(Collections.<Block>emptyList(), Format.BOLD)));
        this.generatorMap.put(Configuration.HTML_ITALIC_OPEN.getName(),
            new BeginEndBlockEventGenerator<HTMLTag>(new FormatBlock(Collections.<Block>emptyList(), Format.ITALIC)));
        this.generatorMap.put(Configuration.HTML_BOLD_OPEN.getName(),
            new BeginEndBlockEventGenerator<HTMLTag>(new FormatBlock(Collections.<Block>emptyList(), Format.BOLD)));
        this.generatorMap.put(new WPBoldItalicTag().getName(), new BoldItalicEventGenerator());
        this.generatorMap.put(Configuration.HTML_STRONG_OPEN.getName(),
            new BeginEndBlockEventGenerator<HTMLTag>(new FormatBlock(Collections.<Block>emptyList(), Format.BOLD)));
        this.generatorMap.put(Configuration.HTML_UNDERLINE_OPEN.getName(), new BeginEndBlockEventGenerator<HTMLTag>(
            new FormatBlock(Collections.<Block>emptyList(), Format.UNDERLINED)));
        this.generatorMap.put(Configuration.HTML_TT_OPEN.getName(), new BeginEndBlockEventGenerator<HTMLTag>(
            new FormatBlock(Collections.<Block>emptyList(), Format.MONOSPACE)));
        this.generatorMap.put(Configuration.HTML_VAR_OPEN.getName(),
            new BeginEndBlockEventGenerator<HTMLTag>(new FormatBlock(Collections.<Block>emptyList(), Format.ITALIC)));
        this.generatorMap.put(Configuration.HTML_SMALL_OPEN.getName(),
            new BeginEndBlockEventGenerator<HTMLTag>(new FormatBlock(Collections.<Block>emptyList(), Format.NONE,
                Collections.singletonMap("style", "font-size:small"))));
        this.generatorMap.put(Configuration.HTML_BIG_OPEN.getName(),
            new BeginEndBlockEventGenerator<HTMLTag>(new FormatBlock(Collections.<Block>emptyList(), Format.NONE,
                Collections.singletonMap("style", "font-size:small"))));
        this.generatorMap.put(Configuration.HTML_CITE_OPEN.getName(),
            new BeginEndBlockEventGenerator<HTMLTag>(new FormatBlock(Collections.<Block>emptyList(), Format.ITALIC)));
        this.generatorMap.put(Configuration.HTML_ABBR_OPEN.getName(),
            new BeginEndBlockEventGenerator<HTMLTag>(new FormatBlock(Collections.<Block>emptyList(), Format.ITALIC)));
        this.generatorMap.put(Configuration.HTML_FONT_OPEN.getName(), new FontEventGenerator());
        this.generatorMap.put(Configuration.HTML_SPAN_OPEN.getName(),
            new BeginEndBlockEventGenerator<HTMLTag>(new FormatBlock(Collections.<Block>emptyList(), Format.NONE)));

        this.generatorMap.put(Configuration.HTML_PARAGRAPH_OPEN.getName(), new ParagraphEventGenerator());

        this.generatorMap.put(Configuration.HTML_SUB_OPEN.getName(), new BeginEndBlockEventGenerator<HTMLTag>(
            new FormatBlock(Collections.<Block>emptyList(), Format.SUBSCRIPT)));
        this.generatorMap.put(Configuration.HTML_SUP_OPEN.getName(), new BeginEndBlockEventGenerator<HTMLTag>(
            new FormatBlock(Collections.<Block>emptyList(), Format.SUPERSCRIPT)));
        this.generatorMap.put(Configuration.HTML_STRIKE_OPEN.getName(), new BeginEndBlockEventGenerator<HTMLTag>(
            new FormatBlock(Collections.<Block>emptyList(), Format.STRIKEDOUT)));
        this.generatorMap.put(Configuration.HTML_S_OPEN.getName(), new BeginEndBlockEventGenerator<HTMLTag>(
            new FormatBlock(Collections.<Block>emptyList(), Format.STRIKEDOUT)));
        this.generatorMap.put(Configuration.HTML_DEL_OPEN.getName(), new BeginEndBlockEventGenerator<HTMLTag>(
            new FormatBlock(Collections.<Block>emptyList(), Format.STRIKEDOUT)));

        this.generatorMap.put(new WPTable(null).getName(), new WPTableBlockEventGenerator());

        this.generatorMap.put(Configuration.HTML_TABLE_OPEN.getName(),
            new BeginEndBlockEventGenerator<HTMLTag>(new TableBlock(Collections.<Block>emptyList())));
        this.generatorMap.put(Configuration.HTML_TH_OPEN.getName(),
            new BeginEndBlockEventGenerator<HTMLTag>(new TableHeadCellBlock(Collections.<Block>emptyList())));
        this.generatorMap.put(Configuration.HTML_TR_OPEN.getName(),
            new BeginEndBlockEventGenerator<HTMLTag>(new TableRowBlock(Collections.<Block>emptyList())));
        this.generatorMap.put(Configuration.HTML_TD_OPEN.getName(),
            new BeginEndBlockEventGenerator<HTMLTag>(new TableCellBlock(Collections.<Block>emptyList())));
        // TODO: BLOCK_MAP.put("caption", HTML_CAPTION_OPEN);

        this.generatorMap.put(Configuration.HTML_UL_OPEN.getName(),
            new BeginEndBlockEventGenerator<HTMLTag>(new BulletedListBlock(Collections.<Block>emptyList())));
        this.generatorMap.put(Configuration.HTML_OL_OPEN.getName(),
            new BeginEndBlockEventGenerator<HTMLTag>(new NumberedListBlock(Collections.<Block>emptyList())));
        this.generatorMap.put(Configuration.HTML_LI_OPEN.getName(),
            new BeginEndBlockEventGenerator<HTMLTag>(new ListItemBlock(Collections.<Block>emptyList())));
        this.generatorMap.put(new WPList().getName(),
            new WPListBlockEventGenerator(new ListItemBlock(Collections.<Block>emptyList())));

        this.generatorMap.put(Configuration.HTML_CENTER_OPEN.getName(),
            new BeginEndBlockEventGenerator<HTMLTag>(new GroupBlock(Collections.<Block>emptyList(),
                Collections.singletonMap("style", "margin-right: auto; margin-left: auto;text-align: center"))));
        this.generatorMap.put(Configuration.HTML_DIV_OPEN.getName(),
            new BeginEndBlockEventGenerator<HTMLTag>(new GroupBlock()));

        this.generatorMap.put(new LinkTag(null, false).getName(), new LinkEventGenerator());

        this.generatorMap.put(XMacroTag.TAG_NAME, new MacroEventGenerator());

        this.generatorMap.put(ImageTag.NAME, new ImageEventGenerator());
        this.generatorMap.put("figure",
            new BeginEndBlockEventGenerator<HTMLTag>(new FigureBlock(Collections.<Block>emptyList())));
        this.generatorMap.put("figurecaption",
            new BeginEndBlockEventGenerator<HTMLTag>(new FigureCaptionBlock(Collections.<Block>emptyList())));
    }

    public void init(Listener listener, MediaWikiSyntaxInputProperties properties)
    {
        this.listener = listener;
        this.properties = properties;
    }

    Listener getListener()
    {
        return this.listener;
    }

    @Override
    public void nodesToText(List<? extends Object> nodes, Appendable resultBuffer, IWikiModel model) throws IOException
    {
        // Refactor a few things
        cleanup((List<Object>) nodes, model);

        // Produce events
        try {
            traverse(nodes, model);
        } catch (FilterException e) {
            new IOException("Failed to send event", e);
        }
    }

    private Object cleanLeadingWhiteSpace(Object obj, boolean leading)
    {
        if (obj instanceof TagNode) {
            cleanLeadingAndTrailingWhiteSpaces(((TagNode) obj).getChildren(), leading);
        } else if (obj instanceof ContentToken) {
            String content = ((ContentToken) obj).getContent();

            if (leading) {
                content = StringUtils.stripStart(content, null);
            } else {
                content = StringUtils.stripEnd(content, null);
            }

            return new ContentToken(content);
        }

        return obj;
    }

    private void cleanLeadingAndTrailingWhiteSpaces(List<Object> children, boolean leading)
    {
        if (!children.isEmpty()) {
            int index;
            if (leading) {
                index = 0;
            } else {
                index = children.size() - 1;
            }

            Object child = children.get(index);

            Object cleanChild = cleanLeadingWhiteSpace(child, leading);

            if (child != cleanChild) {
                children.set(index, cleanChild);
            }
        }
    }

    private TagToken cleanup(TagToken token, IWikiModel model)
    {
        if (token instanceof TagNode) {
            cleanup((TagNode) token, model);
        }

        return token;
    }

    private void cleanup(TagNode node, IWikiModel model)
    {
        if (node.getParents() != null) {
            // Clean leading and trailing white spaces
            cleanLeadingAndTrailingWhiteSpaces(node.getChildren(), true);
            cleanLeadingAndTrailingWhiteSpaces(node.getChildren(), false);
        }
    }

    private void cleanup(List<Object> nodes, IWikiModel model)
    {
        for (int i = 0; i < nodes.size(); ++i) {
            Object child = nodes.get(i);
            Object cleanChild = cleanup(child, model);

            if (cleanChild != child) {
                nodes.set(i, cleanChild);
            }
        }
    }

    private Object cleanup(Object node, IWikiModel model)
    {
        if (node instanceof BaseToken) {
            return cleanup((BaseToken) node, model);
        }

        return node;
    }

    private BaseToken cleanup(BaseToken token, IWikiModel model)
    {
        BaseToken cleanToken = token;

        if (token instanceof TagToken) {
            cleanToken = cleanup((TagToken) token, model);
        } else if (token instanceof ContentToken) {
            String content = ((ContentToken) token).getContent();

            // White spaces are not meaningful in mediawiki
            content = content.replaceAll("[\t ]+", " ");

            // FIXME: workaround a weird handling of entities in the Bliki parser
            // See https://bitbucket.org/axelclk/info.bliki.wiki/issues/33/weird-handling-of-html-entities
            content = Utils.escapeXml(content, true, true, true);
            content = StringEscapeUtils.unescapeXml(content);

            // Convert non-breaking space to white space
            content = content.replace((char) 160, ' ');

            cleanToken = new ContentToken(content);
        }

        return cleanToken;
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

                String content = ((ContentToken) token).getContent();

                // White spaces are not meaningful in mediawiki
                content = content.replaceAll("[\t ]+", " ");

                // FIXME: workaround a weird handling of entities in the Bliki parser
                // See https://bitbucket.org/axelclk/info.bliki.wiki/issues/33/weird-handling-of-html-entities
                content = Utils.escapeXml(content, true, true, true);
                content = StringEscapeUtils.unescapeXml(content);

                // Convert non-breaking space to white space
                content = content.replace((char) 160, ' ');

                this.plainParser.parse(new StringReader(content), inlineListener);
            } catch (ParseException e) {
                // TODO: log something ?
            }

        }
    }

    private void traverse(TagToken token, IWikiModel model) throws FilterException
    {
        EventGenerator blockEvent = createEventGenerator(token);

        if (blockEvent != null) {
            blockEvent.traverse(model, this.properties);
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
        } else if (token instanceof TemplateTag) {
            event = new TemplateTagEventGenerator();
        } else {
            event = this.generatorMap.get(token.getName());

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
