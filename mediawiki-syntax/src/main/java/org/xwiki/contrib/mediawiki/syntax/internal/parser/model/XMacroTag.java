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

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import info.bliki.htmlcleaner.TagNode;
import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.tags.util.INoBodyParsingTag;

/**
 * A standalone macro.
 * 
 * @version $Id$
 */
public class XMacroTag extends TagNode implements INoBodyParsingTag
{
    public static final String TAG_NAME = "xmacro";

    public static final String TAGPREFIX = "macro:";

    private EventWikiModel eventWikiModel;

    private String macroId;

    private Boolean inline;

    private boolean nocontent;

    public XMacroTag(EventWikiModel eventWikiModel)
    {
        this(null, eventWikiModel);
    }

    public XMacroTag(String macroId, EventWikiModel eventWikiModel)
    {
        this(macroId, null, eventWikiModel);
    }

    public XMacroTag(String macroId, Boolean inline, EventWikiModel eventWikiModel)
    {
        super(TAG_NAME);

        this.macroId = macroId;
        this.inline = inline;
        this.eventWikiModel = eventWikiModel;
    }

    @Override
    public String getParents()
    {
        if (this.inline == null) {
            this.inline = this.eventWikiModel.isInline();
        }

        return this.inline ? null : Configuration.SPECIAL_BLOCK_TAGS;
    }

    @Override
    public boolean addAttribute(String attName, String attValue, boolean checkXSS)
    {
        // FIXME: this "/" parameter is most probably a bug on bliki side, contribute something nicer to make the
        // difference between empty content and no content
        if (attName.equals("/")) {
            this.nocontent = true;
        } else {
            getAttributes().put(attName, attValue);
        }

        return true;
    }

    /**
     * @return indicate of the macro is inline
     */
    public boolean isInline()
    {
        return this.inline;
    }

    /**
     * @return the id of the macro
     */
    public String getMacroId()
    {
        return this.macroId;
    }

    /**
     * @return the parameters of the macro
     */
    public Map<String, String> getMacroParameters()
    {
        return getAttributes();
    }

    /**
     * @return the content of the macro
     */
    public String getMacroContent()
    {
        String content = this.nocontent ? null : getBodyString();

        // Remove leading and trailing newline
        content = StringUtils.removeStart(content, "\r");
        content = StringUtils.removeStart(content, "\n");
        content = StringUtils.removeEnd(content, "\n");
        content = StringUtils.removeEnd(content, "\r");

        return content;
    }
}
