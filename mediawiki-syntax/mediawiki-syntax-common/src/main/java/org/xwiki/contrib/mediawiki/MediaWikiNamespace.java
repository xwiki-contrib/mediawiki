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
package org.xwiki.contrib.mediawiki;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Holds various metadata related to a MediaWiki namespace.
 * 
 * @version $Id$
 * @since 2.1.0
 */
public class MediaWikiNamespace
{
    /**
     * Only he first letter is upper case.
     */
    public static final String CASE_FIRST_LETTER = "first-letter";

    /**
     * Fully case sensitive.
     */
    public static final String CASE_CASE_SENSITIVE = "case-sensitive";

    private final Integer key;

    private String name;

    private String caseValue;

    private final List<String> names = new ArrayList<>();

    /**
     * @param name the name of the namespace
     * @param key the key of the namespace
     * @param caseValue the case configuration of the namespace
     */
    public MediaWikiNamespace(String name, Integer key, String caseValue)
    {
        this.name = name;
        this.key = key;
        this.caseValue = caseValue;
        this.names.add(name);
    }

    /**
     * @param name the name of the namespace
     * @param caseValue the case configuration of the namespace
     */
    public void update(String name, String caseValue)
    {
        this.name = name;
        this.caseValue = caseValue;
        this.names.add(name);
    }

    /**
     * @return the name of the namespace
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return the names
     */
    public List<String> getNames()
    {
        return this.names;
    }

    /**
     * @return the key of the namespace
     */
    public Integer getKey()
    {
        return this.key;
    }

    /**
     * @return the case configuration of the namespace
     */
    public String getCaseValue()
    {
        return this.caseValue;
    }

    /**
     * @return true if the references should be left as is
     */
    public boolean isCaseSensitive()
    {
        return CASE_CASE_SENSITIVE.equals(getCaseValue());
    }

    /**
     * @return true if references should be capitalized
     */
    public boolean isCapitalized()
    {
        return !isCaseSensitive();
    }

    private static String normalizeMediaWikiPageName(boolean capitalize, String pageName)
    {
        // MediaWiki automatically replace white space with underscore in pages or files
        String cleanReference = pageName.replace(' ', '_');
        // ... and also reduces several underscores into a single one
        cleanReference = cleanReference.replaceAll("_{2,}", "_");

        if (capitalize) {
            // MediaWiki automatically capitalize references to pages or files by default
            cleanReference = StringUtils.capitalize(cleanReference);
        }

        return cleanReference;
    }

    /**
     * Normalize a MediaWiki reference according to the namespace configuration.
     * 
     * @param namespace the namespace to use for normalization (if null, the reference will be capitalized by default)
     * @param pageName the reference to normalize
     * @return the normalized reference
     * @since 2.1.2
     */
    public static String normalizeMediaWikiPageName(MediaWikiNamespace namespace, String pageName)
    {
        return normalizeMediaWikiPageName(namespace == null || namespace.isCapitalized(), pageName);
    }

    /**
     * Normalize a MediaWiki reference according to the namespace configuration.
     * 
     * @param reference the reference to normalize
     * @return the normalized reference
     * @since 2.1.2
     */
    public String normalizeMediaWikiReference(String reference)
    {
        return normalizeMediaWikiPageName(isCapitalized(), reference);
    }

    @Override
    public int hashCode()
    {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof MediaWikiNamespace) {
            return getName().equals(((MediaWikiNamespace) obj).getName());
        }

        return false;
    }
}
