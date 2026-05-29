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

import org.xwiki.contrib.mediawiki.syntax.MediaWikiSyntaxInputProperties;
import org.xwiki.filter.FilterException;

import info.bliki.htmlcleaner.TagNode;
import info.bliki.wiki.model.IWikiModel;
import org.xwiki.rendering.listener.Listener;

public class ParagraphEventGenerator extends AbstractEventGenerator<TagNode>
{
    @Override
    public void begin(Listener l, boolean inline)
    {
        if (!inline) {
            l.beginParagraph(this.token.getAttributes());
        }
    }

    @Override
    public void end(Listener l, boolean inline)
    {
        if (!inline) {
            l.endParagraph(this.token.getAttributes());
        }
    }

    @Override
    public void traverse(IWikiModel model, MediaWikiSyntaxInputProperties properties, boolean inline, Listener l) throws FilterException
    {
        // FIXME: hack to workaround
        // https://bitbucket.org/axelclk/info.bliki.wiki/issues/32/standalone-generate-an-empty-ptag-followed
        if (!this.token.getChildren().isEmpty()) {
            super.traverse(model, properties, inline, l);
        }
    }
}
