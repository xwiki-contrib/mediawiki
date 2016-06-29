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
package org.xwiki.filter.mediawiki.xml.internal.input;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.filter.mediawiki.input.MediaWikiInputProperties;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.WrappingListener;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;

/**
 * Modify on the fly various events (link reference, macros, etc).
 * 
 * @version $Id$
 */
@Component(roles = MediaWikiContextConverterListener.class)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class MediaWikiContextConverterListener extends WrappingListener
{
    private MediaWikiInputProperties properties;

    private Deque<ResourceReference> currentReference = new LinkedList<>();

    private Set<String> attachments = new HashSet<>();

    void initialize(Listener listener, MediaWikiInputProperties properties)
    {
        setWrappedListener(listener);

        this.properties = properties;
    }

    @Override
    public void beginLink(ResourceReference reference, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        ResourceReference newReference = reference;

        // Convert reference depending on the properties
        if (reference.getType().equals(ResourceType.ATTACHMENT)) {
            // Remember files to attach
            if (this.properties.isFileAttached()) {
                this.attachments.add(reference.getReference());
            }

            // TODO: refactor the reference
        } else if (reference.getType().equals(ResourceType.DOCUMENT)) {
            // TODO: refactor the reference
        }

        super.beginLink(newReference, isFreeStandingURI, parameters);
    }

    @Override
    public void endLink(ResourceReference reference, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        super.endLink(reference, isFreeStandingURI, parameters);
    }

    @Override
    public void onImage(ResourceReference reference, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        // Remember files to attach
        if (this.properties.isFileAttached()) {
            this.attachments.add(reference.getReference());
        }

        // TODO: refactor the reference

        super.onImage(reference, isFreeStandingURI, parameters);
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean isInline)
    {
        // TODO: convert macros

        super.onMacro(id, parameters, content, isInline);
    }
}
