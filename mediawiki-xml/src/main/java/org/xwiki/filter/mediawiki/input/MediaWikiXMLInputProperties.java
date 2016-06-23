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
package org.xwiki.filter.mediawiki.input;

import org.xwiki.filter.input.InputSource;
import org.xwiki.filter.type.FilterStreamType;
import org.xwiki.filter.type.SystemType;
import org.xwiki.filter.xml.input.XMLInputProperties;
import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.properties.annotation.PropertyName;

/**
 * MediaWiki XML input properties.
 * 
 * @version $Id: 408d9389abed98e4ab2fa6f528557e5d2c032b24 $
 */
public class MediaWikiXMLInputProperties extends XMLInputProperties
{
    /**
     * The MediaWiki XML format.
     */
    public static final FilterStreamType FILTER_STREAM_TYPE =
        new FilterStreamType(SystemType.MEDIAWIKI, FilterStreamType.DATA_XML);

    /**
     * The MediaWiki XML format as String.
     */
    public static final String FILTER_STREAM_TYPE_STRING = "mediawiki+xml";

    /**
     * @see #getAttachments()
     */
    private InputSource attachments;

    /**
     * @see #isConvertToXWiki()
     */
    private boolean convertToXWiki = true;

    /**
     * @return the folder or package containing attachments
     */
    @PropertyName("Attachments")
    @PropertyDescription("The folder or package containing attachments")
    public InputSource getAttachments()
    {
        return this.attachments;
    }

    /**
     * @param attachments the folder or package containing attachments
     */
    public void setAttachments(InputSource attachments)
    {
        this.attachments = attachments;
    }

    /**
     * @return if true, convert various MediaWiki standards to XWiki standards
     */
    @PropertyName("XWiki conversion")
    @PropertyDescription("Convert various MediaWiki standards to XWiki standards")
    public boolean isConvertToXWiki()
    {
        return this.convertToXWiki;
    }

    /**
     * @param convertToXWiki if true, convert various MediaWiki standards to XWiki standards
     */
    public void setConvertToXWiki(boolean convertToXWiki)
    {
        this.convertToXWiki = convertToXWiki;
    }
}
