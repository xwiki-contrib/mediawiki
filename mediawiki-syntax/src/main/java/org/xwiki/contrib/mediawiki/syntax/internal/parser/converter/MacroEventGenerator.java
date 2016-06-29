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

import org.apache.commons.lang3.StringUtils;
import org.xwiki.filter.FilterException;

import info.bliki.htmlcleaner.BaseToken;
import info.bliki.htmlcleaner.TagNode;
import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.model.IWikiModel;

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

    public String getContent()
    {
        if (this.content == null) {
            if (!isInline()) {
                // Remove leading and trailing newline
                this.content = StringUtils.removeStart(content, "\r");
                this.content = StringUtils.removeStart(content, "\n");
                this.content = StringUtils.removeEnd(content, "\n");
                this.content = StringUtils.removeEnd(content, "\r");
            }

            this.content = this.token.getBodyString();
        }

        return this.content;
    }

    public boolean isInline()
    {
        if (this.inline == null) {
            this.inline = this.token.getParents() != Configuration.SPECIAL_BLOCK_TAGS;
        }

        return this.inline == Boolean.TRUE;
    }

    @Override
    public void init(BaseToken token, EventConverter converter)
    {
        super.init(token, converter);

        TagNode node = (TagNode) token;

        if (this.id == null) {
            this.id = node.getName();
        }
    }

    @Override
    public void traverse(IWikiModel model) throws FilterException
    {
        getListener().onMacro(this.id, getParameters(), getContent(), isInline());
    }
}
