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

import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.UnknownFilter;

import info.bliki.htmlcleaner.BaseToken;
import info.bliki.htmlcleaner.TagNode;
import info.bliki.htmlcleaner.TagToken;

public class UnknownEventGenerator extends AbstractEventGenerator<TagToken>
{
    private String id;

    private FilterEventParameters parameters;

    private boolean on = true;

    public UnknownEventGenerator()
    {
    }

    @Override
    public void init(BaseToken token, EventConverter converter)
    {
        super.init(token, converter);

        this.id = this.token.getName();

        if (token instanceof TagNode) {
            this.parameters = new FilterEventParameters();
            this.parameters.putAll(((TagNode) token).getAttributes());

            this.on = !((TagNode) token).getChildren().isEmpty();
        }
    }

    @Override
    protected void begin() throws FilterException
    {
        if (getListener() instanceof UnknownFilter) {
            if (!this.on) {
                ((UnknownFilter) getListener()).beginUnknwon(this.id, this.parameters);
            }
        }
    }

    @Override
    protected void end() throws FilterException
    {
        if (getListener() instanceof UnknownFilter) {
            if (!this.on) {
                ((UnknownFilter) getListener()).endUnknwon(this.id, this.parameters);
            } else {
                ((UnknownFilter) getListener()).onUnknwon(this.id, this.parameters);
            }
        }
    }
}
