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
package org.xwiki.contrib.mediawiki.xml.input;

import org.xwiki.filter.input.InputSource;
import org.xwiki.filter.type.FilterStreamType;
import org.xwiki.filter.type.SystemType;
import org.xwiki.filter.xml.input.XMLInputProperties;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.properties.annotation.PropertyName;

/**
 * MediaWiki XML input properties.
 * 
 * @version $Id: 408d9389abed98e4ab2fa6f528557e5d2c032b24 $
 * @since 1.8
 */
public class MediaWikiInputProperties extends XMLInputProperties
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
     * @see #getFiles()
     */
    private InputSource files;

    /**
     * @see #getParent()
     */
    private EntityReference parent;

    /**
     * @see #getFileSpace()
     */
    private EntityReference fileSpace;

    /**
     * @see #isFileAttached()
     */
    private boolean fileAttached = true;

    /**
     * @see #isContentEvents()
     */
    private boolean contentEvents;

    /**
     * @see #isConvertToXWiki()
     */
    private boolean convertToXWiki = true;

    /**
     * @see #isTerminalPages()
     */
    private boolean terminalPages;

    /**
     * @see #isAbsoluteReferences()
     */
    private boolean absoluteReferences;

    /**
     * @see #isOnlyRegisteredNamespaces()
     */
    private boolean onlyRegisteredNamespaces = true;

    /**
     * @see #isNoToc()
     */
    private boolean noToc;

    /**
     * @see #getSpaceSeparator()
     */
    private String spaceSeparator;

    /**
     * @see #getForbiddenCharacters()
     */
    private String forbiddenCharacters;

    private boolean forceFramedCaption;

    /**
     * @return the folder or package containing files
     */
    @PropertyName("Files")
    @PropertyDescription("The folder or package containing files")
    public InputSource getFiles()
    {
        return this.files;
    }

    /**
     * @param files the folder or package containing the files
     */
    public void setFiles(InputSource files)
    {
        this.files = files;
    }

    /**
     * @return the reference of the parent of all pages
     */
    @PropertyName("Parent")
    @PropertyDescription("The reference of the parent of all pages")
    public EntityReference getParent()
    {
        return this.parent;
    }

    /**
     * @param parent the reference of the parent of all pages
     */
    public void setParent(EntityReference parent)
    {
        // Since DOCUMENT is the default type in EntityReference parser and that DOCUMENT does not make any sense as
        // parent we convert it to space
        if (parent != null && parent.getType() == EntityType.DOCUMENT) {
            this.parent = new EntityReference(parent.getName(), EntityType.SPACE, parent.getParent());
        } else {
            this.parent = parent;
        }
    }

    /**
     * @return the space where to store the files
     */
    @PropertyName("Files space")
    @PropertyDescription("The space where to store the files."
        + " By default the namespace defined in the MediaWiki (usually \"File\").")
    public EntityReference getFileSpace()
    {
        return this.fileSpace;
    }

    /**
     * @param fileSpace the space where to store the files
     */
    public void setFileSpace(EntityReference fileSpace)
    {
        this.fileSpace = fileSpace;
    }

    /**
     * @return true if the files should be attached to each page linking it
     */
    @PropertyName("Attach files")
    @PropertyDescription("True if the files should be attached to each page linking it")
    public boolean isFileAttached()
    {
        return this.fileAttached;
    }

    /**
     * @param fileAttached true if the files should be attached to each page linking it
     */
    public void setFileAttached(boolean fileAttached)
    {
        this.fileAttached = fileAttached;
    }

    /**
     * @return if true, the content will be parsed to produce rendering events
     */
    @PropertyName("Produce rendering events for the content")
    @PropertyDescription("Parse the content to produce rendering events (if the output filter supports them)")
    public boolean isContentEvents()
    {
        return this.contentEvents;
    }

    /**
     * @param contentEvents if true, the content will be parsed to produce rendering events
     */
    public void setContentEvents(boolean contentEvents)
    {
        this.contentEvents = contentEvents;
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

    /**
     * @return if true, final pages will be produces (only if #isConvertToXWiki() is true)
     */
    @PropertyName("Terminal Page")
    @PropertyDescription("Produce terminal pages (only if \"XWiki conversion\" is enabled)")
    public boolean isTerminalPages()
    {
        return this.terminalPages;
    }

    /**
     * @param terminalPages if true, final pages will be produces (only if #isConvertToXWiki() is true)
     */
    public void setTerminalPages(boolean terminalPages)
    {
        this.terminalPages = terminalPages;
    }

    /**
     * @return if true, reference are forced absolute in links and images (without the wiki)
     */
    @PropertyName("Absolute reference")
    @PropertyDescription("Force generating absolute reference in links and images (but without the wiki)."
        + " Otherwise the importer try to generate relative reference as much as possible.")
    public boolean isAbsoluteReferences()
    {
        return this.absoluteReferences;
    }

    /**
     * @param absoluteReferences if true, reference are forced absolute in links and images (without the wiki)
     */
    public void setAbsoluteReferences(boolean absoluteReferences)
    {
        this.absoluteReferences = absoluteReferences;
    }

    /**
     * @return if true, only take into account registered namespaces
     * @since 1.8
     */
    @PropertyName("Only take into account registered namespace")
    @PropertyDescription("The importer automatically generate spaces when it find a namespace."
        + " By default only officially registered MediaWiki namespace are taken into account."
        + " If this option is disabled all \":\" bases page title prefixes will be seen as namespace separators.")
    public boolean isOnlyRegisteredNamespaces()
    {
        return this.onlyRegisteredNamespaces;
    }

    /**
     * @param onlyRegisteredNamespaces if true, only take into account registered namespaces
     * @since 1.8
     */
    public void setOnlyRegisteredNamespaces(boolean onlyRegisteredNamespaces)
    {
        this.onlyRegisteredNamespaces = onlyRegisteredNamespaces;
    }

    /**
     * @return true if no toc should be automatically generated
     * @since 1.8
     */
    @PropertyName("No table of content")
    @PropertyDescription("Disable automatically generated tables of content (unless _TOC_ is explicitely used)")
    public boolean isNoToc()
    {
        return this.noToc;
    }

    /**
     * @param noToc true if no toc should be automatically generated
     * @since 1.8
     */
    public void setNoToc(boolean noToc)
    {
        this.noToc = noToc;
    }

    /**
     * @return a regular expression used to cut the page name into a hierarchy
     * @since 1.8
     */
    @PropertyName("Space separator")
    @PropertyDescription("A regular expression used to cut the page name into a hierarchy")
    public String getSpaceSeparator()
    {
        return this.spaceSeparator;
    }

    /**
     * @param spaceSeparator a regular expression used to cut the page name into a hierarchy
     * @since 1.8
     */
    public void setSpaceSeparator(String spaceSeparator)
    {
        this.spaceSeparator = spaceSeparator;
    }

    /**
     * @return the characters that should not end up in page names
     * @since 1.8
     */
    @PropertyName("Forbidden page name characters")
    @PropertyDescription("The characters that should not end up in page names")
    public String getForbiddenCharacters()
    {
        return this.forbiddenCharacters;
    }

    /**
     * @param forbiddenCharacters the characters that should not end up in page names
     * @since 1.8
     */
    public void setForbiddenCharacters(String forbiddenCharacters)
    {
        this.forbiddenCharacters = forbiddenCharacters;
    }

    /**
     * @return indicate if the caption should always be forced below the image
     * @since 2.0.0
     */
    @PropertyName("Force framed caption")
    @PropertyDescription("Indicate if the caption should always be forced below the image.")
    public boolean isForceFramedCaption()
    {
        return this.forceFramedCaption;
    }

    /**
     * @param forceFramedCaption indicate if the caption should always be forced below the image
     * @since 2.0.0
     */
    public void setForceFramedCaption(boolean forceFramedCaption)
    {
        this.forceFramedCaption = forceFramedCaption;
    }
}
