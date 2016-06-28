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

import java.util.List;
import java.util.Map;

import org.xwiki.filter.FilterException;
import org.xwiki.rendering.listener.HeaderLevel;

import info.bliki.htmlcleaner.TagNode;
import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.tags.HTMLBlockTag;

public class HeaderEventGenerator extends AbstractEventGenerator<HTMLBlockTag>
{
    private HeaderLevel level;

    public HeaderEventGenerator(HeaderLevel level)
    {
        this.level = level;
    }

    public Map<String, String> getParameters()
    {
        return this.token.getAttributes();
    }

    @Override
    public void traverse(IWikiModel model) throws FilterException
    {
        List<Object> children = this.token.getChildren();

        if (!children.isEmpty()) {
            Object child = children.get(0);

            String id = null;
            if (child instanceof TagNode) {
                TagNode childNode = (TagNode) child;
                if (childNode.getName().equals(Configuration.HTML_SPAN_OPEN.getName())) {
                    id = childNode.getAttributes().get("id");
                    children = childNode.getChildren();
                }
            }

            getListener().beginHeader(this.level, id, this.token.getAttributes());

            this.converter.traverse(children, model);

            getListener().endHeader(this.level, id, this.token.getAttributes());
        }
    }
}
