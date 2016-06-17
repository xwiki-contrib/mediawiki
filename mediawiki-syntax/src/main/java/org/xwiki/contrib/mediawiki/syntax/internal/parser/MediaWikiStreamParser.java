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

import java.io.IOException;
import java.io.Reader;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.mediawiki.syntax.internal.parser.converter.EventConverter;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;

import info.bliki.wiki.model.WikiModel;

/**
 * MediaWiki streamed parser based on the <a href="https://bitbucket.org/axelclk/info.bliki.wiki">Bliki engine</a>.
 *
 * @version $Id: fcd59f6c7ae81ffec64f5df3ca333eca4eaf18b3 $
 */
@Component
@Named(MediaWikiStreamParser.MEDIAWIKI_1_6_STRING)
@Singleton
public class MediaWikiStreamParser implements StreamParser
{
    /**
     * The syntax type.
     */
    public static final SyntaxType MEDIAWIKI = new SyntaxType("mediawiki", "MediaWiki");

    /**
     * The syntax with version.
     */
    public static final Syntax MEDIAWIKI_1_6 = new Syntax(MEDIAWIKI, "1.6");

    /**
     * The String version of the syntax.
     */
    public static final String MEDIAWIKI_1_6_STRING = "mediawiki/1.6";

    @Inject
    private Provider<EventConverter> converterProvider;

    @Override
    public Syntax getSyntax()
    {
        return MEDIAWIKI_1_6;
    }

    @Override
    public void parse(Reader source, Listener listener) throws ParseException
    {
        // Create custom converter
        EventConverter converter = this.converterProvider.get();
        converter.init(listener);

        EventWikiModel wikiModel = new EventWikiModel();

        MetaData metaData = new MetaData();
        metaData.addMetaData(MetaData.SYNTAX, getSyntax());

        listener.beginDocument(metaData);

        try {
            // Get content
            String sourceString = IOUtils.toString(source);

            // Parse
            wikiModel.render(converter, sourceString, null, false, false);
            //System.out.println(WikiModel.toHtml(sourceString));
        } catch (IOException e) {
            throw new ParseException("Failed to parse source", e);
        }

        listener.endDocument(metaData);
    }
}
