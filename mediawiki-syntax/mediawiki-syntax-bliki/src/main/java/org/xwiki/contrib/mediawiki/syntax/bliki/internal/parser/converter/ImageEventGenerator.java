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

import org.xwiki.contrib.mediawiki.syntax.MediaWikiSyntaxInputProperties;
import org.xwiki.contrib.mediawiki.syntax.bliki.internal.parser.model.ImageTag;
import org.xwiki.filter.FilterException;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.reference.ResourceReference;

import info.bliki.wiki.model.IWikiModel;

public class ImageEventGenerator extends AbstractEventGenerator<ImageTag>
{
    public ImageEventGenerator()
    {
    }

    @Override
    public void traverse(IWikiModel model, MediaWikiSyntaxInputProperties properties) throws FilterException
    {
        ResourceReference linkReference = this.token.getLink();

        if (linkReference != null) {
            getListener().beginLink(linkReference, false, Listener.EMPTY_PARAMETERS);
        }

        getListener().onImage(this.token.getReference(), this.token.isFreestanding(), this.token.getAttributes());

        if (linkReference != null) {
            getListener().endLink(linkReference, false, Listener.EMPTY_PARAMETERS);
        }
    }
}
