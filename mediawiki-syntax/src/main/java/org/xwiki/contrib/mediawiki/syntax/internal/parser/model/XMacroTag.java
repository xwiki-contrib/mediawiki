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

import info.bliki.wiki.tags.util.INoBodyParsingTag;

/**
 * A macro.
 * 
 * @version $Id$
 */
public interface XMacroTag extends INoBodyParsingTag
{
    /**
     * The name of the macros tags in the stack.
     */
    String TAGNAME = "xmacro";

    /**
     * Prefix of the tag name inside mediawiki content.
     */
    String TAGPREFIX = "macro:";

    /**
     * @return the id of the macro
     */
    String getMacroId();

    /**
     * @return the parameters of the macro
     */
    Map<String, String> getMacroParameters();

    /**
     * @return the content of the macro
     */
    String getMacroContent();

    /**
     * @return true of the macro is inline
     */
    boolean isInline();
}
