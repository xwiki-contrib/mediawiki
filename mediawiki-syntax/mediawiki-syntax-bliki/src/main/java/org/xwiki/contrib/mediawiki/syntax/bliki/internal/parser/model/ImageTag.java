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

import org.xwiki.rendering.listener.reference.ResourceReference;

import info.bliki.htmlcleaner.TagNode;
import info.bliki.wiki.model.ImageFormat;

/**
 * Bypass various useless automatic stuff (like title and other generated parameters).
 * 
 * @version $Id$
 */
public class ImageTag extends TagNode
{
    /**
     * The name of the tag in the stack.
     */
    public static final String NAME = "xwiki.image";

    private static final String ATTRIBUTE_ALT = "alt";

    private final ResourceReference reference;

    private final ImageFormat imageFormat;

    private final boolean freestanding;

    /**
     * @param reference the reference of the image
     * @param freestanding true is the image is freestanding
     * @param imageFormat the image caption and options
     */
    public ImageTag(ResourceReference reference, ImageFormat imageFormat, boolean freestanding)
    {
        super(NAME);

        this.reference = reference;
        this.imageFormat = imageFormat;
        this.freestanding = freestanding;

        if (this.imageFormat.getAlt() != null) {
            addAttribute(ATTRIBUTE_ALT, this.imageFormat.getAlt(), false);
        } else if (this.imageFormat.getCaption() != null) {
            addAttribute(ATTRIBUTE_ALT, this.imageFormat.getCaption(), false);
        }

        if (imageFormat.getWidthStr() != null) {
            addAttribute("width", imageFormat.getWidthStr(), false);
        }

        if (imageFormat.getHeightStr() != null) {
            addAttribute("height", imageFormat.getHeightStr(), false);
        }

        if (this.imageFormat.getHorizontalAlign() != null && !this.imageFormat.getHorizontalAlign().equals("none")) {
            addAttribute("style", "float:" + this.imageFormat.getHorizontalAlign(), false);
        }

        // TODO: vertical alignment
    }

    /**
     * @return the reference of the image
     */
    public ResourceReference getReference()
    {
        return this.reference;
    }

    /**
     * @return true is the image is freestanding
     */
    public boolean isFreestanding()
    {
        return this.freestanding;
    }

    /**
     * @return the image caption and options
     */
    public ImageFormat getImageFormat()
    {
        return this.imageFormat;
    }
}
