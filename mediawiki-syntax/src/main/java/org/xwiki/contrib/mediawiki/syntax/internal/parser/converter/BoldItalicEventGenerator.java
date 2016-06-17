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

import org.xwiki.rendering.listener.Format;

import info.bliki.htmlcleaner.BaseToken;
import info.bliki.htmlcleaner.TagNode;

public class BoldItalicEventGenerator extends AbstractEventGenerator
{
    private Map<String, String> parameters;

    public BoldItalicEventGenerator()
    {
    }

    @Override
    public void init(BaseToken token, EventConverter converter)
    {
        super.init(token, converter);

        if (token instanceof TagNode) {
            this.parameters = ((TagNode) token).getAttributes();
        }
    }

    @Override
    public void begin()
    {
        getListener().beginFormat(Format.BOLD, this.parameters);
        getListener().beginFormat(Format.ITALIC, this.parameters);
    }

    @Override
    public void end()
    {
        getListener().endFormat(Format.ITALIC, this.parameters);
        getListener().endFormat(Format.BOLD, this.parameters);
    }
}
