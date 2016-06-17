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
package org.xwiki.contrib.mediawiki.syntax.internal.parser.converter;

import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.xwiki.filter.FilterException;
import org.xwiki.rendering.listener.Listener;

import info.bliki.wiki.filter.WPCell;
import info.bliki.wiki.filter.WPRow;
import info.bliki.wiki.filter.WPTable;
import info.bliki.wiki.model.IWikiModel;

public class WPTableBlockEventGenerator extends AbstractEventGenerator
{
    public WPTableBlockEventGenerator()
    {
    }

    @Override
    public void traverse(IWikiModel model) throws FilterException
    {
        WPTable table = (WPTable) this.token;

        getListener().beginTable(table.getAttributes());

        for (int i = 0; i < table.getRowsSize(); ++i) {
            traverse(table.get(i), model);
        }

        getListener().endTable(table.getAttributes());
    }

    private void traverse(WPRow row, IWikiModel model) throws FilterException
    {
        Map<String, String> attributes = getWPRowAttributes(row);

        getListener().beginTableRow(attributes);

        for (int i = 0; i < row.getNumColumns(); ++i) {
            traverse(row.get(i), model);
        }

        getListener().endTableRow(attributes);
    }

    private void traverse(WPCell cell, IWikiModel model) throws FilterException
    {
        Map<String, String> attributes = cell.getNodeAttributes();

        switch (cell.getType()) {
            case WPCell.CAPTION:
                // TODO: add support for table caption in Rendering API
                break;

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

    // FIXME: getrid of this hack when
    // https://bitbucket.org/axelclk/info.bliki.wiki/pull-requests/3/allow-accessing-wprow-attributes is applied
    private Map<String, String> getWPRowAttributes(WPRow row)
    {
        try {
            return (Map<String, String>) FieldUtils.readDeclaredField(row, "fAttributes", true);
        } catch (Exception e) {
            return Listener.EMPTY_PARAMETERS;
        }
    }
}
