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
package org.xwiki.contrib.mediawiki.syntax.mylyn.internal.input;

import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.mylyn.wikitext.parser.Attributes;
import org.eclipse.mylyn.wikitext.parser.DocumentBuilder;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.mediawiki.syntax.MediaWikiSyntaxInputProperties;
import org.xwiki.contrib.mediawiki.syntax.mylyn.internal.parser.MylynMediaWikiStreamParser;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.InlineFilterListener;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.chaining.EventType;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.StreamParser;

/**
 * Convert Mylyn events into XWiki Rendering events.
 * 
 * @version $Id$
 * @since 2.0.0
 */
@Component(roles = XWikiDocumentBuilder.class)
@Singleton
public class XWikiDocumentBuilder extends DocumentBuilder
{
    private static final MetaData DOCUMENT_METADATA =
        new MetaData(Map.of(MetaData.SYNTAX, MylynMediaWikiStreamParser.SYNTAX));

    @Inject
    @Named("plain/1.0")
    private StreamParser plainParser;

    @Inject
    private Logger logger;

    private Listener listener;

    private MediaWikiSyntaxInputProperties properties;

    private Deque<Event> stack = new ArrayDeque<>();

    private class Event
    {
        private EventType beginType;

        private EventType endType;

        private Object[] arguments = ArrayUtils.EMPTY_OBJECT_ARRAY;

        void set(EventType beginType, EventType endType, Object... arguments)
        {
            this.beginType = beginType;
            this.endType = endType;
            this.arguments = arguments;
        }

        void begin(Listener listener)
        {
            if (this.beginType != null) {
                this.beginType.fireEvent(listener, this.arguments);
            }
        }

        void end(Listener listener)
        {
            if (this.endType != null) {
                this.endType.fireEvent(listener, this.arguments);
            }
        }
    }

    /**
     * @param listener the listener
     * @param properties the properties
     */
    public void init(Listener listener, MediaWikiSyntaxInputProperties properties)
    {
        this.listener = listener;
        this.properties = properties;
    }

    @Override
    public void beginDocument()
    {
        this.listener.beginDocument(DOCUMENT_METADATA);
    }

    @Override
    public void endDocument()
    {
        this.listener.endDocument(DOCUMENT_METADATA);
    }

    @Override
    public void beginBlock(BlockType type, Attributes attributes)
    {
        Event event = new Event();
        Map<String, String> parameters = Listener.EMPTY_PARAMETERS;

        switch (type) {
            case BULLETED_LIST:
                event.set(EventType.BEGIN_LIST, EventType.END_LIST, ListType.BULLETED, parameters);
                break;
            case CODE:
                break;
            case DEFINITION_ITEM:
                event.set(EventType.BEGIN_DEFINITION_DESCRIPTION, EventType.END_DEFINITION_DESCRIPTION);
                break;
            case DEFINITION_LIST:
                event.set(EventType.BEGIN_DEFINITION_LIST, EventType.END_DEFINITION_LIST, parameters);
                break;
            case DEFINITION_TERM:
                event.set(EventType.BEGIN_DEFINITION_TERM, EventType.END_DEFINITION_TERM);
                break;
            case DIV:
                event.set(EventType.BEGIN_GROUP, EventType.END_GROUP, parameters);
                break;
            case FOOTNOTE:
                break;
            case INFORMATION:
                break;
            case LIST_ITEM:
                event.set(EventType.BEGIN_LIST_ITEM, EventType.END_LIST_ITEM);
                break;
            case NOTE:
                break;
            case NUMERIC_LIST:
                event.set(EventType.BEGIN_LIST, EventType.END_LIST, ListType.NUMBERED, parameters);
                break;
            case PANEL:
                break;
            case PARAGRAPH:
                event.set(EventType.BEGIN_PARAGRAPH, EventType.END_PARAGRAPH, parameters);
                break;
            case PREFORMATTED:
                break;
            case QUOTE:
                break;
            case TABLE:
                event.set(EventType.BEGIN_TABLE, EventType.END_TABLE, parameters);
                break;
            case TABLE_CELL_HEADER:
                event.set(EventType.BEGIN_TABLE_HEAD_CELL, EventType.END_TABLE_HEAD_CELL, parameters);
                break;
            case TABLE_CELL_NORMAL:
                event.set(EventType.BEGIN_TABLE_CELL, EventType.END_TABLE_CELL, parameters);
                break;
            case TABLE_ROW:
                event.set(EventType.BEGIN_TABLE_ROW, EventType.END_TABLE_ROW, parameters);
                break;
            case TIP:
                break;
            case WARNING:
                break;
            default:
                break;
        }

        // Call begin event
        event.begin(this.listener);

        // Remember the event in the stack
        this.stack.push(event);
    }

    @Override
    public void endBlock()
    {
        // Remove the event from the stack
        Event event = this.stack.pop();

        // Call the end event
        event.end(this.listener);
    }

    @Override
    public void beginSpan(SpanType type, Attributes attributes)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void endSpan()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void beginHeading(int level, Attributes attributes)
    {
        Event event = new Event();
        Map<String, String> parameters = Listener.EMPTY_PARAMETERS;
        event.set(EventType.BEGIN_HEADER, EventType.END_HEADER, HeaderLevel.parseInt(level), null, parameters);
        event.begin(this.listener);
        this.stack.push(event);
    }

    @Override
    public void endHeading()
    {
        Event event = this.stack.pop();
        event.end(this.listener);
    }

    @Override
    public void characters(String text)
    {
        try {
            InlineFilterListener inlineListener = new InlineFilterListener();
            inlineListener.setWrappedListener(this.listener);

            this.plainParser.parse(new StringReader(text), inlineListener);
        } catch (ParseException e) {
            throw new IllegalStateException("Failed to parse plain test content", e);
        }
    }

    @Override
    public void entityReference(String entity)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void image(Attributes attributes, String url)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void link(Attributes attributes, String hrefOrHashName, String text)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void imageLink(Attributes linkAttributes, Attributes imageAttributes, String href, String imageUrl)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void acronym(String text, String definition)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void lineBreak()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void charactersUnescaped(String literal)
    {
        // TODO Auto-generated method stub

    }
}
