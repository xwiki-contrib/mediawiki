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
package org.xwiki.contrib.mediawiki.syntax;

import java.util.Collection;
import java.util.Map;

import org.xwiki.filter.DefaultFilterStreamProperties;
import org.xwiki.filter.input.InputSource;
import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.properties.annotation.PropertyMandatory;
import org.xwiki.properties.annotation.PropertyName;

/**
 * MediaWiki Syntax input properties.
 * 
 * @version $Id: 408d9389abed98e4ab2fa6f528557e5d2c032b24 $
 */
public class MediaWikiSyntaxInputProperties extends DefaultFilterStreamProperties
{
    /**
     * The way link/image references should be understood.
     * 
     * @version $Id$
     */
    public enum ReferenceType
    {
        /**
         * Don't parse references.
         */
        NONE,

        /**
         * Parse as standard XWiki resource reference.
         */
        XWIKI,

        /**
         * Parse as standard MediaWiki references.
         */
        MEDIAWIKI
    }

    /**
     * How to handle image caption.
     * 
     * @version $Id$
     * @since 2.0.0
     */
    public enum FigureSupport
    {
        /**
         * Force produce native figure events in case an image have a caption. This is the default for XWiki runtime
         * 14.1 and more.
         */
        FIGURE,

        /**
         * Force produce group events mimicking the structure of a figure. This is the default for XWiki runtime lower
         * than 14.1.
         */
        DIV
    }

    /**
     * @see #getSource()
     */
    private InputSource source;

    /**
     * @see #getReferenceType()
     */
    private ReferenceType referenceType = ReferenceType.XWIKI;

    /**
     * @see #getCustomNamespaces()
     */
    private Map<Integer, Collection<String>> customNamespaces;

    /**
     * @see #isNoToc()
     */
    private boolean noToc;

    private String templateMacroPrefix = "";

    // TODO: change the default to FIGURE when when moving to a version of XWiki will full support for image figure with
    // link around it
    private FigureSupport figureSupport = FigureSupport.DIV;

    private boolean forceFramedCaption;

    /**
     * @return The source to load the wiki from
     */
    @PropertyName("Source")
    @PropertyDescription("The source to load the wiki from")
    @PropertyMandatory
    public InputSource getSource()
    {
        return this.source;
    }

    /**
     * @param source The source to load the wiki from
     */
    public void setSource(InputSource source)
    {
        this.source = source;
    }

    /**
     * @return the way link/image references should be understood
     */
    @PropertyName("Reference type")
    @PropertyDescription("The way link/image references should be understood")
    public ReferenceType getReferenceType()
    {
        return this.referenceType;
    }

    /**
     * @param referenceType the way link/image references should be understood
     */
    public void setReferenceType(ReferenceType referenceType)
    {
        this.referenceType = referenceType;
    }

    /**
     * @return the custom namespaces
     * @since 1.8
     */
    @PropertyName("Custom namespace")
    @PropertyDescription("Allows customizing the namespaces (usually to translate them)")
    public Map<Integer, Collection<String>> getCustomNamespaces()
    {
        return this.customNamespaces;
    }

    /**
     * @param namespaces the custom namespaces
     * @since 1.8
     */
    public void setCustomNamespaces(Map<Integer, Collection<String>> namespaces)
    {
        this.customNamespaces = namespaces;
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
     * @return the prefix to use when creating new macro name to translate templates calls
     * @since 1.8
     */
    @PropertyName("Template macro name prefix")
    @PropertyDescription("The prefix to use when creating new macro name to translate templates calls.")
    public String getTemplateMacroPrefix()
    {
        return this.templateMacroPrefix;
    }

    /**
     * @param templateMacroPrefix the prefix to use when creating new macro name to translate templates calls
     * @since 1.8
     */
    public void setTemplateMacroPrefix(String templateMacroPrefix)
    {
        this.templateMacroPrefix = templateMacroPrefix;
    }

    /**
     * @return Indicate what to do when a figure is supposed to be generated according to MediaWiki specifications
     * @since 2.0.0
     */
    @PropertyName("Framed image/figure caption support")
    @PropertyDescription("Indicate what to do when a figure is supposed to be generated according to"
        + " MediaWiki specifications.")
    public FigureSupport getFigureSupport()
    {
        return this.figureSupport;
    }

    /**
     * @param figureSupport indicate what to do when a figure is supposed to be generated according to MediaWiki
     *            specifications
     * @since 2.0.0
     */
    public void setImageCaptionSupport(FigureSupport figureSupport)
    {
        this.figureSupport = figureSupport;
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
