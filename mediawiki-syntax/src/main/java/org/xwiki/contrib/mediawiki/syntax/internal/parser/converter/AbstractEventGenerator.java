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
import org.xwiki.rendering.listener.Listener;

import info.bliki.htmlcleaner.BaseToken;
import info.bliki.htmlcleaner.TagNode;
import info.bliki.wiki.model.IWikiModel;

public abstract class AbstractEventGenerator<T extends BaseToken> implements EventGenerator
{
    protected T token;

    protected EventConverter converter;

    public AbstractEventGenerator()
    {
    }

    @Override
    public void init(BaseToken token, EventConverter converter)
    {
        this.token = (T) token;
        this.converter = converter;
    }

    @Override
    public T getToken()
    {
        return this.token;
    }

    @Override
    public AbstractEventGenerator clone() throws CloneNotSupportedException
    {
        return (AbstractEventGenerator) super.clone();
    }

    protected Listener getListener()
    {
        return this.converter.getListener();
    }

    public Map<String, String> getParameters()
    {
        if (this.token instanceof TagNode) {
            return ((TagNode) this.token).getAttributes();
        }

        return Listener.EMPTY_PARAMETERS;
    }

    protected void begin() throws FilterException
    {
        // To overwrite
    }

    protected void end() throws FilterException
    {
        // To overwrite
    }

    @Override
    public void traverse(IWikiModel model) throws FilterException
    {
        begin();

        if (this.token instanceof TagNode) {
            this.converter.traverse(((TagNode) this.token).getChildren(), model);
        }

        end();
    }

    public String getAttributeKey(Map<String, String> attributes, String targetKey)
    {
        for (String key : attributes.keySet()) {
            if (key.equalsIgnoreCase(targetKey)) {
                return key;
            }
        }

        return null;
    }
}
