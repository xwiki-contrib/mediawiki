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
package org.xwiki.contrib.mediawiki.syntax.internal.parser.converter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.lang3.StringUtils;

public class GalleryEventGenerator extends MacroEventGenerator
{
    @Override
    protected String createContent()
    {
        String source = super.createContent();

        // Convert to proper wiki content
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new StringReader(source));

        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                String cleanLine = line.trim();

                if (StringUtils.isNotEmpty(cleanLine)) {
                    if (!StringUtils.startsWithIgnoreCase(cleanLine, "image:")
                        && !StringUtils.startsWithIgnoreCase(cleanLine, "file:")) {
                        cleanLine = "File:" + cleanLine;
                    }

                    if (builder.length() > 0) {
                        builder.append("\n");
                    }
                    builder.append("[[");
                    builder.append(cleanLine);
                    builder.append("]]");
                }
            }
        } catch (IOException e) {
            // Should never happen
        }

        return builder.toString();
    }
}
