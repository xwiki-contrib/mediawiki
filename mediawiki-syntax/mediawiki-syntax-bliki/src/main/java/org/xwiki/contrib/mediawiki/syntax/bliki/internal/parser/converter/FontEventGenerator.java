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

import org.xwiki.rendering.listener.Format;

import info.bliki.htmlcleaner.BaseToken;
import info.bliki.wiki.tags.HTMLTag;

public class FontEventGenerator extends AbstractEventGenerator<HTMLTag>
{
    private static final Map<String, String> FONT_SIZE_MAP = new HashMap<>();

    static {
        FONT_SIZE_MAP.put("1", "0.63em");
        FONT_SIZE_MAP.put("2", "0.82em");
        // 3 is the default
        // FONT_SIZE_MAP.put("3", "1.0em");
        FONT_SIZE_MAP.put("4", "1.13em");
        FONT_SIZE_MAP.put("5", "1.5em");
        FONT_SIZE_MAP.put("6", "2em");
        FONT_SIZE_MAP.put("7", "3em");
    }

    private Map<String, String> parameters = new HashMap<>();

    public FontEventGenerator()
    {
    }

    @Override
    public void init(BaseToken token, EventConverter converter)
    {
        super.init(token, converter);
    }

    @Override
    public Map<String, String> getParameters()
    {
        return this.parameters;
    }

    @Override
    public void begin()
    {
        StringBuilder builder = new StringBuilder();

        // Font size
        String fontSize = FONT_SIZE_MAP.get(this.token.getAttributes().get("size"));
        if (fontSize != null) {
            builder.append("font-size: ");
            builder.append(fontSize);
            builder.append(';');
        }

        // Font color
        String fontColor = this.token.getAttributes().get("color");
        if (fontSize != null) {
            builder.append("font-color: ");
            builder.append(fontColor);
            builder.append(';');
        }

        // Font color
        String fontFace = this.token.getAttributes().get("face");
        if (fontSize != null) {
            builder.append("font-face: ");
            builder.append(fontFace);
            builder.append(';');
        }

        if (builder.length() > 0) {
            this.parameters.put("style", builder.toString());
        }

        getListener().beginFormat(Format.NONE, getParameters());
    }

    @Override
    public void end()
    {
        getListener().endFormat(Format.NONE, getParameters());
    }
}
