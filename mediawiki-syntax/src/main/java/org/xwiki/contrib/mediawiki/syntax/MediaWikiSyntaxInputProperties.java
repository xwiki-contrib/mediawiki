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
import org.xwiki.filter.type.FilterStreamType;
import org.xwiki.filter.type.SystemType;
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
     * The MediaWiki Syntax format.
     */
    public static final FilterStreamType FILTER_STREAM_TYPE = new FilterStreamType(SystemType.MEDIAWIKI, "xdom", "1.6");

    /**
     * The MediaWiki Syntax format as String.
     */
    public static final String FILTER_STREAM_TYPE_STRING = "mediawiki+xdom/1.6";

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
}
