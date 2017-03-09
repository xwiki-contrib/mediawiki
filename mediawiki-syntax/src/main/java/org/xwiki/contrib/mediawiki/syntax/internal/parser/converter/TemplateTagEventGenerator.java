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

import java.util.LinkedHashMap;
import java.util.Map;

import org.xwiki.contrib.mediawiki.syntax.MediaWikiSyntaxInputProperties;
import org.xwiki.filter.FilterException;

import info.bliki.htmlcleaner.BaseToken;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.tags.TemplateTag;

/**
 * @version $Id$
 * @since 1.8
 */
public class TemplateTagEventGenerator extends AbstractEventGenerator<TemplateTag>
{
    /**
     * Default constructor.
     */
    public TemplateTagEventGenerator()
    {
    }

    @Override
    public void init(BaseToken token, EventConverter converter)
    {
        super.init(token, converter);
    }

    @Override
    public void traverse(IWikiModel model, MediaWikiSyntaxInputProperties properties) throws FilterException
    {
        String prefix = properties.getTemplateMacroPrefix();

        String macroName = prefix + this.token.getName();

        Map<String, String> macroParameters = new LinkedHashMap<>();

        // Use first parameter as macro content
        String content = this.token.getAttributes().get("1");

        // Skip parameter "0" and "1" which is the content which is the macro name
        for (int i = 2; i < this.token.getAttributes().size(); ++i) {
            String key = String.valueOf(i);
            String value = this.token.getAttributes().get(key);
            if (value == null) {
                break;
            }

            macroParameters.put(key, value);
        }

        getListener().onMacro(macroName, macroParameters, content, false);
    }
}
