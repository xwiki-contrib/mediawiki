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

import java.util.Map;

import org.xwiki.rendering.block.AbstractBlock;

import info.bliki.htmlcleaner.BaseToken;
import info.bliki.htmlcleaner.TagNode;

public abstract class AbstractBlockEventGenerator<T extends BaseToken> extends AbstractEventGenerator<T>
{
    protected AbstractBlock block;

    public AbstractBlockEventGenerator(AbstractBlock block)
    {
        this.block = block;
    }

    @Override
    public void init(BaseToken token, EventConverter converter)
    {
        super.init(token, converter);

        if (token instanceof TagNode) {
            for (Map.Entry<String, String> entry : ((TagNode) token).getAttributes().entrySet()) {
                this.block.setParameter(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public AbstractBlockEventGenerator<T> clone() throws CloneNotSupportedException
    {
        AbstractBlockEventGenerator<T> blockEvent = (AbstractBlockEventGenerator<T>) super.clone();

        blockEvent.block = (AbstractBlock) this.block.clone();

        return blockEvent;
    }
}
