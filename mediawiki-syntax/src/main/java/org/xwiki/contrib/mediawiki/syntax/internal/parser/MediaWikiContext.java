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
package org.xwiki.contrib.mediawiki.syntax.internal.parser;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

/**
 * Mostly use to let MediaWiki XML filter modify syntax parser behavior.
 * 
 * @version $Id$
 */
@Component(roles = MediaWikiContext.class)
@Singleton
public class MediaWikiContext
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

    private static final String KEY_REFERENCE_TYPE = "mediawiki.referencetype";

    @Inject
    private Execution execution;

    private ExecutionContext getExecutionContext()
    {
        return this.execution.getContext();
    }

    /**
     * @return the way link/image references should be understood
     */
    public ReferenceType getReferenceType()
    {
        ExecutionContext context = getExecutionContext();

        ReferenceType referenceType;
        if (context != null) {
            referenceType = (ReferenceType) getExecutionContext().getProperty(KEY_REFERENCE_TYPE);
            if (referenceType == null) {
                referenceType = ReferenceType.XWIKI;
            }
        } else {
            referenceType = ReferenceType.XWIKI;
        }

        return referenceType;
    }

    /**
     * @param referenceType the way link/image references should be understood
     */
    public void setReferenceType(ReferenceType referenceType)
    {
        ExecutionContext context = getExecutionContext();

        if (context != null) {
            if (referenceType == null) {
                getExecutionContext().removeProperty(KEY_REFERENCE_TYPE);
            } else {
                getExecutionContext().setProperty(KEY_REFERENCE_TYPE, referenceType);
            }
        }
    }
}
