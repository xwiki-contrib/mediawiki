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
package org.xwiki.contrib.mediawiki.syntax.bliki.internal.parser.converter;

import java.util.Map;

import org.xwiki.contrib.mediawiki.syntax.MediaWikiSyntaxInputProperties;
import org.xwiki.filter.FilterException;
import org.xwiki.rendering.listener.Listener;

import info.bliki.wiki.filter.WPCell;
import info.bliki.wiki.filter.WPRow;
import info.bliki.wiki.filter.WPTable;
import info.bliki.wiki.model.IWikiModel;

public class WPTableBlockEventGenerator extends AbstractEventGenerator<WPTable>
{
    @Override
    public void traverse(IWikiModel model, MediaWikiSyntaxInputProperties properties) throws FilterException
    {
        // Caption
        // Since there is no support for table caption in XWiki Rendering we have to hack it some other way
        // TODO: modify when https://jira.xwiki.org/browse/XRENDERING-269 is implemented
        boolean caption = false;
        if (this.token.getRowsSize() > 0) {
            WPRow firstRow = this.token.get(0);
            if (firstRow.getNumColumns() > 0 && firstRow.getType() == WPCell.CAPTION) {
                caption = true;

                getListener().beginGroup(Listener.EMPTY_PARAMETERS);

                getListener().beginGroup(Listener.EMPTY_PARAMETERS);
                this.converter.traverse(firstRow.get(0).getTagStack(), model);
                getListener().endGroup(Listener.EMPTY_PARAMETERS);
            }
        }

        // Table
        getListener().beginTable(this.token.getAttributes());

        for (int i = 0; i < this.token.getRowsSize(); ++i) {
            traverse(this.token.get(i), model);
        }

        getListener().endTable(this.token.getAttributes());

        if (caption) {
            getListener().endGroup(Listener.EMPTY_PARAMETERS);
        }
    }

    private void traverse(WPRow row, IWikiModel model) throws FilterException
    {
        Map<String, String> attributes = row.getAttributes();

        getListener().beginTableRow(attributes);

        for (int i = 0; i < row.getNumColumns(); ++i) {
            if (row.getType() != WPCell.CAPTION) {
                // Ignored because it's handled before the table
                traverse(row.get(i), model);
            }
        }

        getListener().endTableRow(attributes);
    }

    private void traverse(WPCell cell, IWikiModel model) throws FilterException
    {
        Map<String, String> attributes =
            cell.getNodeAttributes() != null ? cell.getNodeAttributes() : Listener.EMPTY_PARAMETERS;

        switch (cell.getType()) {
            case WPCell.TH:
                getListener().beginTableHeadCell(attributes);
                this.converter.traverse(cell.getTagStack(), model);
                getListener().endTableHeadCell(attributes);
                break;

            default:
                getListener().beginTableCell(attributes);
                this.converter.traverse(cell.getTagStack(), model);
                getListener().endTableCell(attributes);
                break;
        }
    }
}
