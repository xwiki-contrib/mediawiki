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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Helper to manipulate MediaWiki namespaces.
 * <p>
 * See https://www.mediawiki.org/wiki/Manual:Namespace for more details.
 * 
 * @version $Id$
 * @since 2.1.0
 */
public class MediaWikiNamespaces
{
    /**
     * The default name of the file namespace.
     */
    public static final String NAMESPACE_FILE_DEFAULT = "File";

    /**
     * The index of the file namespace.
     */
    public static final int NAMESPACE_FILE_IDX = 6;

    /**
     * The default name of the user namespace.
     */
    public static final String NAMESPACE_USER_DEFAULT = "User";

    /**
     * The index of the user namespace.
     */
    public static final int NAMESPACE_USER_IDX = 2;

    /**
     * The default name of the spacial namespace.
     */
    public static final String NAMESPACE_SPECIAL_DEFAULT = "Special";

    /**
     * The index of the special namespace.
     */
    public static final int NAMESPACE_SPECIAL_IDX = -1;

    private final Map<Integer, MediaWikiNamespace> keyToNamespace = new HashMap<>();

    private final Map<String, MediaWikiNamespace> nameToNamespace = new HashMap<>();

    /**
     * Default constructor.
     */
    public MediaWikiNamespaces()
    {
        // Initialize with standard namespaces.
        // See https://www.mediawiki.org/wiki/Manual:Namespace#Built-in_namespaces
        addNamespace(0, "", MediaWikiNamespace.CASE_FIRST_LETTER);
        addNamespace(NAMESPACE_USER_IDX, NAMESPACE_USER_DEFAULT, MediaWikiNamespace.CASE_FIRST_LETTER);
        addNamespace(4, "Project", MediaWikiNamespace.CASE_FIRST_LETTER);
        addNamespace(NAMESPACE_FILE_IDX, NAMESPACE_FILE_DEFAULT, MediaWikiNamespace.CASE_FIRST_LETTER);
        addNamespace(NAMESPACE_FILE_IDX, "Image", MediaWikiNamespace.CASE_FIRST_LETTER);
        addNamespace(8, "MediaWiki", MediaWikiNamespace.CASE_FIRST_LETTER);
        addNamespace(10, "Template", MediaWikiNamespace.CASE_FIRST_LETTER);
        addNamespace(12, "Help", MediaWikiNamespace.CASE_FIRST_LETTER);
        addNamespace(14, "Category", MediaWikiNamespace.CASE_FIRST_LETTER);

        addNamespace(NAMESPACE_SPECIAL_IDX, NAMESPACE_SPECIAL_DEFAULT, MediaWikiNamespace.CASE_FIRST_LETTER);
        addNamespace(-2, "Media", MediaWikiNamespace.CASE_FIRST_LETTER);
    }

    /**
     * @param key the key associated to the namespace
     * @param namespace the namespace
     * @param caseValue the namespace case setup
     */
    public void addNamespace(String key, String namespace, String caseValue)
    {
        addNamespace(Integer.valueOf(key), namespace, caseValue);
    }

    /**
     * @param key the key associated to the namespace
     * @param name the namespace
     * @param caseValue the namespace case setup
     */
    public void addNamespace(int key, String name, String caseValue)
    {
        // Get the previous namespace
        MediaWikiNamespace namespace =
            this.keyToNamespace.computeIfAbsent(key, k -> new MediaWikiNamespace(name, key, caseValue));

        // Update the previous namespace
        namespace.update(name, caseValue);

        // Add the new name to the mapping
        this.nameToNamespace.put(name.toLowerCase(), namespace);
    }

    /**
     * @param name the name to test
     * @return if the passed namespace is a registered namespace
     */
    public boolean isNamespace(String name)
    {
        return this.nameToNamespace.containsKey(name.toLowerCase());
    }

    /**
     * @param key the namespace key
     * @param name the namespace name
     * @return true if the passed namespace name is associated to the passed namespace key
     */
    public boolean isNamespace(int key, String name)
    {
        if (name == null) {
            return false;
        }

        MediaWikiNamespace namespace = this.nameToNamespace.get(name.toLowerCase());

        return namespace != null && namespace.getKey() != null && namespace.getKey().intValue() == key;
    }

    /**
     * @param namespace the namespace to test
     * @return true if the passed namespace is an alias of file namespace
     */
    public boolean isFileNamespace(String namespace)
    {
        return isNamespace(NAMESPACE_FILE_IDX, namespace);
    }

    /**
     * @param name the name of the namespace
     * @return the namespace corresponding to the passed name
     */
    public MediaWikiNamespace getNamespace(String name)
    {
        return this.nameToNamespace.get(name.toLowerCase());
    }

    /**
     * @return the default namespace used as file namespace alias
     */
    public String getFileNamespace()
    {
        MediaWikiNamespace namespace = this.keyToNamespace.get(NAMESPACE_FILE_IDX);

        return namespace != null ? namespace.getName() : null;
    }

    /**
     * @param name the name to resolve
     * @return the registered default name corresponding to the passed namespace
     */
    public String resolve(String name)
    {
        MediaWikiNamespace namespace = this.nameToNamespace.get(name.toLowerCase());
        if (namespace != null) {
            return namespace.getName();
        }

        return name;
    }

    /**
     * @param key the key of the namespace
     * @return the defualt name for the passed namespace
     */
    public String getDefaultNamespace(int key)
    {
        MediaWikiNamespace namespace = this.keyToNamespace.get(key);

        return namespace != null ? namespace.getName() : null;
    }

    /**
     * @param title the page title to parse
     * @return the file name
     */
    public String getFileName(String title)
    {
        for (String name : this.keyToNamespace.get(NAMESPACE_FILE_IDX).getNames()) {
            if (StringUtils.startsWithIgnoreCase(title, name + ':')) {
                return title.substring(name.length() + 1).replace(' ', '_');
            }
        }

        return null;
    }

    /**
     * @param title the reference to parse
     * @return true if the passed reference belong to a file
     */
    public boolean isInFileNamespace(String title)
    {
        return getFileName(title) != null;
    }

    /**
     * @param namespace the namespace to test
     * @return true if the passed namespace is an alias of special namespace
     */
    public boolean isSpecialNamespace(String namespace)
    {
        return isNamespace(NAMESPACE_SPECIAL_IDX, namespace);
    }

    /**
     * @return the namespaces
     */
    public Map<Integer, MediaWikiNamespace> getNamespaces()
    {
        return this.keyToNamespace;
    }
}
