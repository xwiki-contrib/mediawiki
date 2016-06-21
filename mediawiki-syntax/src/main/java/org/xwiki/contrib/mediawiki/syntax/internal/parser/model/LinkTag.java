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

import org.xwiki.rendering.listener.reference.ResourceReference;

import info.bliki.htmlcleaner.TagNode;

/**
 * Bypass various useless automatic stuff (like label creation from the reference).
 * 
 * @version $Id$
 */
public class LinkTag extends TagNode
{
    private ResourceReference reference;

    private boolean freestanding;

    /**
     * @param reference the reference of the link
     * @param freestanding true is the link is freestanding
     */
    public LinkTag(ResourceReference reference, boolean freestanding)
    {
        super("xwiki.link");

        this.reference = reference;
        this.freestanding = freestanding;
    }

    /**
     * @return the reference of the link
     */
    public ResourceReference getReference()
    {
        return this.reference;
    }

    /**
     * @return true is the link is freestanding
     */
    public boolean isFreestanding()
    {
        return this.freestanding;
    }

    @Override
    public boolean isReduceTokenStack()
    {
        return false;
    }
}
