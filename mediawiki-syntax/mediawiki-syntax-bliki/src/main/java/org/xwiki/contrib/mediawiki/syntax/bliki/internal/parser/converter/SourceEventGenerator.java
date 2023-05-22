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

import java.util.LinkedHashMap;
import java.util.Map;

import org.xwiki.contrib.mediawiki.syntax.MediaWikiSyntaxInputProperties;
import org.xwiki.filter.FilterException;

import info.bliki.htmlcleaner.BaseToken;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.tags.SourceTag;

public class SourceEventGenerator extends AbstractEventGenerator<SourceTag>
{
    private boolean inline;

    private String content;

    private Map<String, String> parameters;

    public SourceEventGenerator()
    {
    }

    @Override
    public void init(BaseToken token, EventConverter converter)
    {
        super.init(token, converter);

        this.content = this.token.getBodyString();

        Map<String, String> attributes = this.token.getAttributes();

        this.parameters = new LinkedHashMap<>();
        String language = attributes.get("lang");
        if (language != null) {
            this.parameters.put("language", language);
        }

        this.inline = attributes.containsKey("inline");
    }

    @Override
    public void traverse(IWikiModel model, MediaWikiSyntaxInputProperties properties) throws FilterException
    {
        getListener().onMacro("code", this.parameters, this.content, this.inline);
    }

    @Override
    public SourceEventGenerator clone() throws CloneNotSupportedException
    {
        return (SourceEventGenerator) super.clone();
    }
}
