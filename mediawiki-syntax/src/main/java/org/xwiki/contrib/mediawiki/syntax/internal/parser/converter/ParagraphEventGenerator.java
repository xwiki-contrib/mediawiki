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

import org.xwiki.filter.FilterException;

import info.bliki.htmlcleaner.TagNode;
import info.bliki.wiki.model.IWikiModel;

public class ParagraphEventGenerator extends AbstractEventGenerator<TagNode>
{
    @Override
    public void begin()
    {
        getListener().beginParagraph(this.token.getAttributes());
    }

    @Override
    public void end()
    {
        getListener().endParagraph(this.token.getAttributes());
    }

    @Override
    public void traverse(IWikiModel model) throws FilterException
    {
        // FIXME: hack to workaround
        // https://bitbucket.org/axelclk/info.bliki.wiki/issues/32/standalone-generate-an-empty-ptag-followed
        if (!((TagNode) this.token).getChildren().isEmpty()) {
            super.traverse(model);
        }
    }
}
