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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;

import info.bliki.htmlcleaner.BaseToken;
import info.bliki.htmlcleaner.TagNode;
import info.bliki.wiki.tags.ATag;

public class AEventGenerator extends AbstractEventGenerator<ATag>
{
    private static final Pattern URL_SCHEME_PATTERN = Pattern.compile("[a-zA-Z0-9+.-]*://");

    private ResourceReference reference;

    private Map<String, String> parameters;

    public AEventGenerator()
    {
    }

    @Override
    public void init(BaseToken token, EventConverter converter)
    {
        super.init(token, converter);

        if (token instanceof TagNode) {
            this.parameters = new HashMap<>(((TagNode) token).getAttributes());

            String hrefName = getAttributeKey(this.parameters, "href");
            if (hrefName != null) {
                String href = this.parameters.remove(hrefName);
                if (href != null) {
                    computeResourceReference(href);
                }
            }
        }
    }

    private void computeResourceReference(String rawReference)
    {
        // Do we have a valid URL?
        Matcher matcher = URL_SCHEME_PATTERN.matcher(rawReference);
        if (matcher.lookingAt()) {
            // We have UC1
            this.reference = new ResourceReference(rawReference, ResourceType.URL);
        } else {
            // We have UC2
            this.reference = new ResourceReference(rawReference, ResourceType.PATH);
        }
    }

    @Override
    public void begin()
    {
        if (this.reference != null) {
            getListener().beginLink(this.reference, false, this.parameters);
        }
    }

    @Override
    public void end()
    {
        if (this.reference != null) {
            getListener().endLink(this.reference, false, this.parameters);
        }
    }
}
