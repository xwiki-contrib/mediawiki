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

import info.bliki.wiki.tags.HTMLTag;
import org.apache.commons.lang3.StringUtils;
import org.xwiki.contrib.mediawiki.syntax.MediaWikiSyntaxInputProperties;
import org.xwiki.contrib.mediawiki.syntax.bliki.internal.parser.model.XMacroTag;
import org.xwiki.filter.FilterException;

import info.bliki.htmlcleaner.TagNode;
import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.model.IWikiModel;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.renderer.PrintRenderer;

public class MacroEventGenerator extends AbstractEventGenerator<TagNode>
{
    private String id;

    private String content;

    private Boolean inline;

    public MacroEventGenerator()
    {
    }

    public MacroEventGenerator(String id)
    {
        this.id = id;
    }

    public MacroEventGenerator(String id, boolean inline)
    {
        this.id = id;
        this.inline = inline;
    }

    protected String getId()
    {
        if (this.id == null) {
            if (this.token instanceof XMacroTag) {
                return ((XMacroTag) this.token).getMacroId();
            } else {
                this.id = this.token.getName();
            }
        }

        return this.id;
    }

    protected String createContent(IWikiModel model) throws FilterException
    {
        if (this.token instanceof XMacroTag) {
            return ((XMacroTag) this.token).getMacroContent();
        }

        PrintRenderer printRenderer = this.converter.createPrintRenderer();
        if (printRenderer == null) {
            String content = this.token.getBodyString();

            content = maybeRemoveTrailingNewLines(content);

            return content;
        }
        // The print renderer apparently doesn't like receiving events outside a document. Some stuff is missing if we
        // don't wrap them between a "begin document" and an "end document" events
        printRenderer.beginDocument(MetaData.EMPTY);
        this.converter.traverse(this.token.getChildren(), model, isInline(), printRenderer);
        printRenderer.endDocument(MetaData.EMPTY);
        return maybeRemoveTrailingNewLines(printRenderer.getPrinter().toString());
    }

    String maybeRemoveTrailingNewLines(String s)
    {
        if (!isInline()) {
            return s;
        }
        // Remove leading and trailing newline
        String content = s;
        content = StringUtils.removeStart(content, "\r");
        content = StringUtils.removeStart(content, "\n");
        content = StringUtils.removeEnd(content, "\n");
        content = StringUtils.removeEnd(content, "\r");
        return content;
    }

    public String getContent(IWikiModel model) throws FilterException
    {
        if (this.content == null) {
            this.content = createContent(model);
        }

        return this.content;
    }

    public boolean isInline()
    {
        if (this.inline == null) {
            if (this.token instanceof XMacroTag) {
                this.inline = ((XMacroTag) this.token).isInline();
            } else {
                this.inline = this.token.getParents() != Configuration.SPECIAL_BLOCK_TAGS;
            }
        }

        return this.inline == Boolean.TRUE;
    }

    @Override
    public void traverse(IWikiModel model, MediaWikiSyntaxInputProperties properties, boolean inline, Listener l)
            throws FilterException
    {
        l.onMacro(getId(), getParameters(), getContent(model), isInline());
    }
}
