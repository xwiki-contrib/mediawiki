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
package org.xwiki.contrib.mediawiki.syntax.mylyn.internal.input;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.io.IOUtils;
import org.eclipse.mylyn.wikitext.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.util.ServiceLocator;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.contrib.mediawiki.syntax.MediaWikiSyntaxInputProperties;
import org.xwiki.contrib.mediawiki.syntax.internal.input.MediaWikiContentFilter;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.input.AbstractBeanInputFilterStream;
import org.xwiki.filter.input.ReaderInputSource;
import org.xwiki.filter.input.StringInputSource;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.SectionGeneratorListener;

/**
 * @version $Id: 41df1dab66b03111214dbec56fee8dbd44747638 $
 * @since 2.0.0
 */
@Component
@Named(MylynMediaWikiSyntaxInputFilterStreamFactory.FILTER_STREAM_TYPE_STRING)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class MylynMediaWikiSyntaxInputFilterStream
    extends AbstractBeanInputFilterStream<MediaWikiSyntaxInputProperties, MediaWikiContentFilter>
{
    @Inject
    private Provider<XWikiDocumentBuilder> builderProvider;

    @Override
    public void close() throws IOException
    {
        this.properties.getSource().close();
    }

    protected String getSource() throws IOException, FilterException
    {
        if (this.properties.getSource() instanceof StringInputSource) {
            return this.properties.getSource().toString();
        } else if (this.properties.getSource() instanceof ReaderInputSource) {
            return IOUtils.toString(((ReaderInputSource) this.properties.getSource()).getReader());
        } else {
            throw new FilterException("Unknown source type [" + this.properties.getSource().getClass() + "]");
        }
    }

    @Override
    protected void read(Object filter, MediaWikiContentFilter proxyFilter) throws FilterException
    {
        String source;
        try {
            source = getSource();
        } catch (IOException e) {
            throw new FilterException("Failed to read source", e);
        }

        Listener listener = (Listener) filter;

        SectionGeneratorListener wrappingLisrener = new SectionGeneratorListener(listener);

        try {
            MarkupLanguage markupLanguage = ServiceLocator.getInstance().getMarkupLanguage("MediaWiki");
            MarkupParser parser = new MarkupParser(markupLanguage);

            XWikiDocumentBuilder builder = this.builderProvider.get();
            builder.init(listener, this.properties);
            parser.setBuilder(builder);

            parser.parse(source);
        } catch (Exception e) {
            throw new FilterException("Failed to parse source", e);
        }

        /*
        for (String category : wikiModel.getCategories().keySet()) {
            proxyFilter.onCategory(category, FilterEventParameters.EMPTY);
        }

        if (wikiModel.getRedirectLink() != null) {
            proxyFilter.onRedirect(wikiModel.getRedirectLink(), FilterEventParameters.EMPTY);
        }
        */
    }
}
