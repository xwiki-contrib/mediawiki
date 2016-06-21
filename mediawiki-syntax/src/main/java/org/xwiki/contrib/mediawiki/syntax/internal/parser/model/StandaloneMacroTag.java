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
package org.xwiki.contrib.mediawiki.syntax.internal.parser.model;

import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.tags.HTMLBlockTag;
import info.bliki.wiki.tags.util.INoBodyParsingTag;

/**
 * Make sure gallery macro content is not parsed by standard parser.
 * 
 * @version $Id$
 */
// TODO: a more complete version should probably be contributed upstream
public class StandaloneMacroTag extends HTMLBlockTag implements INoBodyParsingTag
{
    /**
     * @param id the id of the macro
     */
    public StandaloneMacroTag(String id)
    {
        super(id, Configuration.SPECIAL_BLOCK_TAGS);
    }

    @Override
    public Object clone()
    {
        return new StandaloneMacroTag(getName());
    }
}
