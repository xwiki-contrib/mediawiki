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
package org.xwiki.contrib.mediawiki.syntax.bliki.internal.parser;

import java.io.IOException;
import java.io.Reader;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.mediawiki.syntax.MediaWiki;
import org.xwiki.contrib.mediawiki.syntax.MediaWikiSyntaxInputProperties;
import org.xwiki.contrib.mediawiki.syntax.MediaWikiSyntaxInputProperties.ReferenceType;
import org.xwiki.contrib.mediawiki.syntax.bliki.internal.input.BlikiMediaWikiSyntaxInputFilterStreamFactory;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.input.BeanInputFilterStreamFactory;
import org.xwiki.filter.input.DefaultReaderInputSource;
import org.xwiki.filter.input.InputFilterStream;
import org.xwiki.filter.input.InputFilterStreamFactory;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Bliki based MediaWiki streamed parser based on the <a href="https://bitbucket.org/axelclk/info.bliki.wiki">Bliki
 * engine</a>.
 *
 * @version $Id: fcd59f6c7ae81ffec64f5df3ca333eca4eaf18b3 $
 */
@Component
@Named(BlikiMediaWikiStreamParser.SYNTAX_STRING)
@Singleton
public class BlikiMediaWikiStreamParser implements StreamParser
{
    /**
     * The syntax with version.
     */
    public static final Syntax SYNTAX = new Syntax(MediaWiki.SYNTAX_TYPE, "1.6");

    /**
     * The String version of the syntax.
     */
    public static final String SYNTAX_STRING = "mediawiki/1.6";

    @Inject
    @Named(BlikiMediaWikiSyntaxInputFilterStreamFactory.FILTER_STREAM_TYPE_STRING)
    private InputFilterStreamFactory filter;

    @Override
    public Syntax getSyntax()
    {
        return SYNTAX;
    }

    @Override
    public void parse(Reader source, Listener listener) throws ParseException
    {
        MediaWikiSyntaxInputProperties properties = new MediaWikiSyntaxInputProperties();
        properties.setSource(new DefaultReaderInputSource(source));
        properties.setReferenceType(ReferenceType.XWIKI);

        BeanInputFilterStreamFactory<MediaWikiSyntaxInputProperties> beanFilter =
            (BeanInputFilterStreamFactory<MediaWikiSyntaxInputProperties>) this.filter;

        try (InputFilterStream stream = beanFilter.createInputFilterStream(properties)) {
            stream.read(listener);
        } catch (IOException e) {
            throw new ParseException("Failed to close source", e);
        } catch (FilterException e) {
            throw new ParseException("Failed to parse content", e);
        }
    }
}
