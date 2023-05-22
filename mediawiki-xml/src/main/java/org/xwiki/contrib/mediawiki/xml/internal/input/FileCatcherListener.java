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
package org.xwiki.contrib.mediawiki.xml.internal.input;

import java.awt.Dimension;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.contrib.mediawiki.syntax.MediaWikiSyntaxInputProperties;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.input.BeanInputFilterStream;
import org.xwiki.rendering.listener.WrappingListener;
import org.xwiki.rendering.listener.reference.AttachmentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;

/**
 * Find all files referenced in a wiki content.
 * 
 * @version $Id$
 */
@Component(roles = FileCatcherListener.class)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class FileCatcherListener extends WrappingListener
{
    private static final String WIDTH = "width";

    private static final String HEIGHT = "height";

    private static final String PX_SUFFIX = "px";

    private static final Pattern PXSIZE = Pattern.compile("(\\d+)" + PX_SUFFIX);

    @Inject
    private Logger logger;

    private MediaWikiInputFilterStream stream;

    private Set<String> files = new HashSet<>();

    void initialize(MediaWikiInputFilterStream stream)
    {
        this.stream = stream;
    }

    /**
     * @return the found referenced files
     */
    public Set<String> getFiles()
    {
        return this.files;
    }

    @Override
    public void beginLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        if (reference instanceof AttachmentResourceReference) {
            this.files.add(reference.getReference());
        }

        super.beginLink(reference, freestanding, parameters);
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        if (reference instanceof AttachmentResourceReference) {
            String fileName = reference.getReference();

            this.files.add(fileName);

            // Make sure the image keep aspect ratio no matter what (since that's what happen in mediawiki)
            String widthString = parameters.get(WIDTH);
            String heightString = parameters.get(HEIGHT);
            if (StringUtils.isNoneEmpty(widthString) && StringUtils.isNoneEmpty(heightString)) {
                keepAspectRatio(widthString, heightString, fileName, parameters);
            }
        }

        super.onImage(reference, freestanding, parameters);
    }

    private void keepAspectRatio(String widthString, String heightString, String fileName,
        Map<String, String> parameters)
    {
        // If we can't find the dimension, arbitrary remove the height to be sure the aspect ratio will be kept
        parameters.remove(HEIGHT);

        Matcher widthMatcher = PXSIZE.matcher(widthString);
        Matcher heightMatcher = PXSIZE.matcher(heightString);

        if (widthMatcher.matches() && heightMatcher.matches()) {
            int width = NumberUtils.toInt(widthMatcher.group(1), -1);
            int height = NumberUtils.toInt(heightMatcher.group(1), -1);

            if (width != -1 && height != -1) {
                try {
                    File file = this.stream.getFile(fileName);
                    if (file != null) {
                        Dimension dimension = MediaWikiUtils.getImageDimension(file);

                        double widthRatio = width / dimension.getWidth();
                        double heightRatio = height / dimension.getHeight();
                        double ratio = Math.min(widthRatio, heightRatio);

                        width = (int) (dimension.width * ratio);
                        height = (int) (dimension.height * ratio);

                        parameters.put(WIDTH, String.valueOf(width) + PX_SUFFIX);
                        parameters.put(HEIGHT, String.valueOf(height) + PX_SUFFIX);
                    }
                } catch (FilterException e) {
                    this.logger.error("Failed to extract dimension for image [{}]", fileName);
                }
            }
        }
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean isInline)
    {
        // Extract attachments from macro with wiki content
        // TODO: make it configurable
        if (id.equals("gallery") || id.equals("blockquote")) {
            MediaWikiSyntaxInputProperties parserProperties = stream.createMediaWikiSyntaxInputProperties(content);

            // Generate events
            try (BeanInputFilterStream<MediaWikiSyntaxInputProperties> contentStream =
                this.stream.getInputFilterStreamFactory().createInputFilterStream(parserProperties)) {
                contentStream.read(this);
            } catch (Exception e) {
                // TODO log something ?
            }
        }

        super.onMacro(id, parameters, content, isInline);
    }
}
