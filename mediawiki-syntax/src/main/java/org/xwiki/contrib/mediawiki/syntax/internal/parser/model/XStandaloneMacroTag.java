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

import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.tags.HTMLBlockTag;

/**
 * A standalone macro.
 * 
 * @version $Id$
 */
public class XStandaloneMacroTag extends HTMLBlockTag implements XMacroTag
{
    private String macroId;

    private boolean nocontent;

    /**
     * Default constructor.
     */
    public XStandaloneMacroTag()
    {
        this(null);
    }

    /**
     * @param macroId the id of the macro
     */
    public XStandaloneMacroTag(String macroId)
    {
        super(TAGNAME, Configuration.SPECIAL_BLOCK_TAGS);

        this.macroId = macroId;
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

    @Override
    public Object clone()
    {
        return new XStandaloneMacroTag(getMacroId());
    }

    @Override
    public boolean isInline()
    {
        return false;
    }

    @Override
    public String getMacroId()
    {
        return this.macroId;
    }

    @Override
    public Map<String, String> getMacroParameters()
    {
        return getAttributes();
    }

    @Override
    public String getMacroContent()
    {
        return this.nocontent ? null : getBodyString();
    }
}
