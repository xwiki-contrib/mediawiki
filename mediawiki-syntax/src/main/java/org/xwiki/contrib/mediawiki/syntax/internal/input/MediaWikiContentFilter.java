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
package org.xwiki.contrib.mediawiki.syntax.internal.input;

import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.annotation.Default;
import org.xwiki.rendering.listener.Listener;

/**
 * Extend standard rendering {@link Listener} with a few MediWiki content specific events.
 * 
 * @version $Id$
 * @since 1.8
 */
public interface MediaWikiContentFilter extends Listener
{
    /**
     * @param name the name of the category
     * @param parameters the properties of the document
     * @throws FilterException when failing to send event
     */
    void onCategory(String name, @Default(FilterEventParameters.DEFAULT) FilterEventParameters parameters)
        throws FilterException;
}
