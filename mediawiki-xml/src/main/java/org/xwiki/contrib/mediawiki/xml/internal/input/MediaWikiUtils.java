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
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.filter.FilterException;

/**
 * Various tools for MediaWiki.
 * 
 * @version $Id$
 * @since 1.10
 */
public final class MediaWikiUtils
{
    protected static final Logger LOGGER = LoggerFactory.getLogger(MediaWikiUtils.class);

    private MediaWikiUtils()
    {
        // Utility class
    }

    /**
     * Extract the dimension of an image from a {@link File}.
     * 
     * @param file the image file
     * @return dimensions the dimensions of the image or null if it can't be extracted form some reason
     * @throws FilterException when failing to get the image dimension
     */
    public static Dimension getImageDimension(File file) throws FilterException
    {
        String extension = FilenameUtils.getExtension(file.getName());

        if (extension == null) {
            throw new FilterException("Image file [" + file.getAbsolutePath() + "] does not have any extension");
        }

        Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix(extension);
        while (it.hasNext()) {
            ImageReader imageReader = it.next();

            try {
                imageReader.setInput(new FileImageInputStream(file));

                int width = imageReader.getWidth(imageReader.getMinIndex());
                int height = imageReader.getHeight(imageReader.getMinIndex());

                return new Dimension(width, height);
            } catch (IOException e) {
                throw new FilterException("Failed to read image file [" + file.getAbsolutePath() + "]", e);
            } finally {
                imageReader.dispose();
            }
        }

        throw new FilterException("Unsupported image file [" + file.getAbsolutePath() + "]");
    }
}
