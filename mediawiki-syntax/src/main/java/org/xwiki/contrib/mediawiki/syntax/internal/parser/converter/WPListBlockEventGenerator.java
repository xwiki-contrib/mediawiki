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

import java.lang.reflect.Method;
import java.util.Collections;

import org.xwiki.filter.FilterException;
import org.xwiki.rendering.block.AbstractBlock;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.BulletedListBlock;
import org.xwiki.rendering.block.DefinitionListBlock;
import org.xwiki.rendering.block.NumberedListBlock;

import info.bliki.wiki.filter.WPList;
import info.bliki.wiki.filter.WPList.InternalList;
import info.bliki.wiki.filter.WPListElement;
import info.bliki.wiki.model.IWikiModel;

public class WPListBlockEventGenerator extends BeginEndBlockEventGenerator
{
    public WPListBlockEventGenerator(AbstractBlock block)
    {
        super(block);
    }

    @Override
    public void traverse(IWikiModel model) throws FilterException
    {
        WPList list = (WPList) this.token;

        for (Object element : list.getNestedElements()) {
            if (element instanceof InternalList) {
                traverse((InternalList) element, model);
            } else if (element instanceof WPListElement) {
                this.converter.traverse(((WPListElement) element).getTagStack(), model);
            }
        }
    }

    private BeginEndBlockEventGenerator createListBlockEvent(InternalList list)
    {
        BeginEndBlockEventGenerator generator;

        switch (list.getChar()) {
            case WPList.UL_CHAR:
                generator = new BeginEndBlockEventGenerator(new BulletedListBlock(Collections.<Block>emptyList()));
                break;
            case WPList.OL_CHAR:
                generator = new BeginEndBlockEventGenerator(new NumberedListBlock(Collections.<Block>emptyList()));
                break;
            default:
                generator = new BeginEndBlockEventGenerator(new DefinitionListBlock(Collections.<Block>emptyList()));
                break;
        }

        generator.init(this.token, this.converter);

        return generator;
    }

    private void traverse(InternalList list, IWikiModel model) throws FilterException
    {
        BeginEndBlockEventGenerator blockEvent = createListBlockEvent(list);

        blockEvent.begin();

        traverseElements(list, model);

        blockEvent.end();
    }

    private void begingListElement(char c)
    {
        switch (c) {
            case WPList.DL_DD_CHAR:
                getListener().beginDefinitionDescription();
                break;

            case WPList.DL_DT_CHAR:
                getListener().beginDefinitionTerm();
                break;

            default:
                getListener().beginListItem();
                break;
        }
    }

    private void endListElement(char c)
    {
        switch (c) {
            case WPList.DL_DD_CHAR:
                getListener().endDefinitionDescription();
                break;

            case WPList.DL_DT_CHAR:
                getListener().endDefinitionTerm();
                break;

            default:
                getListener().endListItem();
                break;
        }
    }

    private void traverseElements(InternalList list, IWikiModel model) throws FilterException
    {
        boolean itemOpen = false;

        char currentChar = list.getChar();

        begingListElement(currentChar);

        for (Object element : list) {
            if (element instanceof InternalList) {
                traverse((InternalList) element, model);
            } else if (element instanceof WPListElement) {
                if (itemOpen) {
                    endListElement(currentChar);

                    char[] temp = getSequence((WPListElement) element);
                    currentChar = temp[temp.length - 1];

                    begingListElement(currentChar);
                }
                itemOpen = true;
                this.converter.traverse(((WPListElement) element).getTagStack(), model);
            }
        }

        endListElement(currentChar);
    }

    // FIXME: getrid of this hack when
    // https://bitbucket.org/axelclk/info.bliki.wiki/pull-requests/3/allow-accessing-wprow-attributes is applied
    private char[] getSequence(WPListElement listElement)
    {
        try {
            Method method = WPListElement.class.getDeclaredMethod("getSequence");
            method.setAccessible(true);
            return (char[]) method.invoke(listElement);
        } catch (Exception e) {
            return new char[] {'*'};
        }
    }
}
