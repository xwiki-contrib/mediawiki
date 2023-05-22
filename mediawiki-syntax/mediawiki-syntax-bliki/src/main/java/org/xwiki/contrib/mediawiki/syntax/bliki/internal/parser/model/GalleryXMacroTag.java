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
package org.xwiki.contrib.mediawiki.syntax.bliki.internal.parser.model;

/**
 * Special gallery standalone macro.
 * 
 * @version $Id$
 */
public class GalleryXMacroTag extends XMacroTag
{
    /**
     * Default constructor.
     */
    public GalleryXMacroTag()
    {
        super("gallery", false, null);
    }

    @Override
    public String getName()
    {
        return getMacroId();
    }

    @Override
    public Object clone()
    {
        return new GalleryXMacroTag();
    }
}
