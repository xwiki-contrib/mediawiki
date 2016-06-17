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

import java.util.Map;

import org.xwiki.filter.FilterException;

import info.bliki.htmlcleaner.BaseToken;
import info.bliki.htmlcleaner.TagNode;
import info.bliki.wiki.model.IWikiModel;

public class VerbatimEventGenerator extends AbstractEventGenerator
{
    private boolean inline;

    private String content;

    private Map<String, String> parameters;

    public VerbatimEventGenerator(boolean inline)
    {
        this.inline = inline;
    }

    @Override
    public void init(BaseToken token, EventConverter converter)
    {
        super.init(token, converter);

        TagNode node = (TagNode) token;

        this.content = node.getBodyString();
        this.parameters = node.getAttributes();
    }

    @Override
    public void traverse(IWikiModel model) throws FilterException
    {
        getListener().onVerbatim(this.content, this.inline, this.parameters);
    }
}
