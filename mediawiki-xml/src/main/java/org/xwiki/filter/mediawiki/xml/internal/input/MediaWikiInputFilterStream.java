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
package org.xwiki.filter.mediawiki.xml.internal.input;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.contrib.mediawiki.syntax.internal.parser.MediaWikiContext;
import org.xwiki.contrib.mediawiki.syntax.internal.parser.MediaWikiContext.ReferenceType;
import org.xwiki.contrib.mediawiki.syntax.internal.parser.MediaWikiStreamParser;
import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.event.model.WikiDocumentFilter;
import org.xwiki.filter.input.AbstractBeanInputFilterStream;
import org.xwiki.filter.input.InputStreamInputSource;
import org.xwiki.filter.input.ReaderInputSource;
import org.xwiki.filter.mediawiki.input.MediaWikiInputProperties;
import org.xwiki.filter.mediawiki.xml.internal.MediaWikiFilter;
import org.xwiki.filter.xml.input.SourceInputSource;
import org.xwiki.model.EntityType;
import org.xwiki.model.ModelConfiguration;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.xml.stax.StAXUtils;

/**
 * @version $Id: 41df1dab66b03111214dbec56fee8dbd44747638 $
 */
@Component
@Named(MediaWikiInputProperties.FILTER_STREAM_TYPE_STRING)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class MediaWikiInputFilterStream extends AbstractBeanInputFilterStream<MediaWikiInputProperties, MediaWikiFilter>
{
    private static final String TAG_SITEINFO = "siteinfo";

    private static final String TAG_SITEINFO_NAMESPACES = "namespaces";

    private static final String TAG_SITEINFO_NAMESPACE = "namespace";

    private static final String TAG_PAGE = "page";

    private static final String TAG_PAGE_TITLE = "title";

    private static final String TAG_PAGE_NAMESPACE = "ns";

    private static final String TAG_PAGE_REVISION = "revision";

    private static final String TAG_PAGE_REVISION_CONTRIBUTOR = "contributor";

    private static final String TAG_PAGE_REVISION_CONTRIBUTOR_USERNAME = "username";

    private static final String TAG_PAGE_REVISION_CONTRIBUTOR_IP = "ip";

    private static final String TAG_PAGE_REVISION_TIMESTAMP = "timestamp";

    private static final String TAG_PAGE_REVISION_MINOR = "minor";

    private static final String TAG_PAGE_REVISION_VERSION = "id";

    private static final String TAG_PAGE_REVISION_COMMENT = "comment";

    private static final String TAG_PAGE_REVISION_CONTENT = "text";

    private static final String PAGE_NAME_MAIN = "Main_Page";

    private static final String NAMESPACE_FILE = "File";

    private static final String NAMESPACE_USER = "User";

    private static final String NAMESPACE_SPECIAL = "Special";

    @Inject
    private Logger logger;

    @Inject
    @Named(MediaWikiStreamParser.SYNTAX_STRING)
    private StreamParser confluenceWIKIParser;

    @Inject
    private ModelConfiguration modelConfiguration;

    @Inject
    private MediaWikiContext context;

    @Inject
    private Provider<MediaWikiContextConverterListener> listenerProvider;

    private Map<String, String> namespaces = new HashMap<>();

    private String currentPageNamespace;

    private String currentPageTitle;

    private EntityReference previousParentReference;

    private EntityReference currentParentReference;

    @Override
    public void close() throws IOException
    {
        this.properties.getSource().close();
    }

    @Override
    protected void read(Object filter, MediaWikiFilter proxyFilter) throws FilterException
    {
        // Create reader
        XMLStreamReader xmlReader;
        try {
            xmlReader = getXMLStreamReader();
        } catch (Exception e) {
            throw new FilterException("Failed to create XMLStreamReader", e);
        }

        // Initialize defaults
        this.previousParentReference = this.properties.getParent();

        // Read document
        try {
            read(xmlReader, filter, proxyFilter);
        } catch (Exception e) {
            throw new FilterException("Failed to parse XML", e);
        }
    }

    private XMLStreamReader getXMLStreamReader()
        throws XMLStreamException, FactoryConfigurationError, FilterException, IOException
    {
        XMLStreamReader xmlReader;

        if (this.properties.getSource() instanceof SourceInputSource) {
            xmlReader = StAXUtils.getXMLStreamReader(((SourceInputSource) this.properties.getSource()).getSource());
        } else if (this.properties.getSource() instanceof ReaderInputSource) {
            xmlReader = XMLInputFactory.newInstance()
                .createXMLStreamReader(((ReaderInputSource) this.properties.getSource()).getReader());
        } else if (this.properties.getSource() instanceof InputStreamInputSource) {
            xmlReader = XMLInputFactory.newInstance()
                .createXMLStreamReader(((InputStreamInputSource) this.properties.getSource()).getInputStream());
        } else {
            throw new FilterException("Unknown source type [" + this.properties.getSource().getClass() + "]");
        }

        return xmlReader;
    }

    private void read(XMLStreamReader xmlReader, Object filter, MediaWikiFilter proxyFilter)
        throws XMLStreamException, FilterException
    {
        xmlReader.nextTag();

        readMediaWiki(xmlReader, filter, proxyFilter);
    }

    private void readMediaWiki(XMLStreamReader xmlReader, Object filter, MediaWikiFilter proxyFilter)
        throws XMLStreamException, FilterException
    {
        for (xmlReader.nextTag(); xmlReader.isStartElement(); xmlReader.nextTag()) {
            String elementName = xmlReader.getLocalName();

            if (elementName.equals(TAG_SITEINFO)) {
                readSiteInfo(xmlReader);
            } else if (elementName.equals(TAG_PAGE)) {
                readPage(xmlReader, filter, proxyFilter);
            } else {
                StAXUtils.skipElement(xmlReader);
            }
        }
    }

    private void readSiteInfo(XMLStreamReader xmlReader) throws XMLStreamException
    {
        for (xmlReader.nextTag(); xmlReader.isStartElement(); xmlReader.nextTag()) {
            String elementName = xmlReader.getLocalName();

            if (elementName.equals(TAG_SITEINFO_NAMESPACES)) {
                readNamespaces(xmlReader);
            } else {
                StAXUtils.skipElement(xmlReader);
            }
        }
    }

    private void readNamespaces(XMLStreamReader xmlReader) throws XMLStreamException
    {
        for (xmlReader.nextTag(); xmlReader.isStartElement(); xmlReader.nextTag()) {
            String elementName = xmlReader.getLocalName();

            if (elementName.equals(TAG_SITEINFO_NAMESPACE)) {
                this.namespaces.put(xmlReader.getAttributeValue(null, "key"), xmlReader.getElementText());
            } else {
                StAXUtils.skipElement(xmlReader);
            }
        }
    }

    private void readPage(XMLStreamReader xmlReader, Object filter, MediaWikiFilter proxyFilter)
        throws XMLStreamException, FilterException
    {
        boolean skip = false;

        for (xmlReader.nextTag(); xmlReader.isStartElement(); xmlReader.nextTag()) {
            String elementName = xmlReader.getLocalName();

            if (skip) {
                StAXUtils.skipElement(xmlReader);
            } else if (elementName.equals(TAG_PAGE_TITLE)) {
                this.currentPageTitle = xmlReader.getElementText();
            } else if (elementName.equals(TAG_PAGE_NAMESPACE)) {
                this.currentPageNamespace = this.namespaces.get(xmlReader.getElementText());
            } else if (elementName.equals(TAG_PAGE_REVISION)) {
                // Find current page name
                String pageName = this.currentPageTitle;
                if (this.currentPageNamespace == null) {
                    // In old versions of MediaWiki there was no explicit namespace field, it was just the title
                    // prefix
                    int index = pageName.indexOf(':');
                    this.currentPageNamespace = pageName.substring(0, index);
                    pageName = pageName.substring(index + 1);
                } else {
                    // Remove the namespace prefix from the page name
                    pageName = StringUtils.removeStart(pageName, this.currentPageNamespace + ':');
                }
                // MediaWiki replace the white spaces with an underscore in the URL
                pageName = pageName.replace(' ', '_');
                // Maybe convert MediaWiki home page name into XWiki home page name
                if (this.properties.isConvertToXWiki() && pageName.equals(PAGE_NAME_MAIN)) {
                    pageName = this.modelConfiguration.getDefaultReferenceValue(EntityType.DOCUMENT);
                }

                // Find current page parent
                if (StringUtils.isEmpty(this.currentPageNamespace)) {
                    if (this.properties.isConvertToXWiki()) {
                        this.currentParentReference =
                            new EntityReference(this.modelConfiguration.getDefaultReferenceValue(EntityType.SPACE),
                                EntityType.SPACE, this.properties.getParent());
                    } else {
                        this.currentParentReference = this.properties.getParent();
                    }
                } else if (this.properties.getFilesSpace() != null
                    && this.currentPageNamespace.equals(NAMESPACE_FILE)) {
                    this.currentParentReference = this.properties.getFilesSpace();
                    if (this.currentParentReference.extractFirstReference(EntityType.WIKI) == null) {
                        this.currentParentReference =
                            new EntityReference(this.currentParentReference, this.properties.getParent());
                    }
                } else if (this.currentPageNamespace.equals(NAMESPACE_USER)) {
                    // TODO: add support for users
                    skip = true;
                } else if (this.currentPageNamespace.equals(NAMESPACE_SPECIAL)) {
                    skip = true;
                } else {
                    this.currentParentReference =
                        new EntityReference(this.currentPageNamespace, EntityType.SPACE, this.properties.getParent());
                }

                if (!skip) {
                    // Send parent events
                    sendSpaceEvents(proxyFilter);

                    // Send document event
                    proxyFilter.beginWikiDocument(pageName, FilterEventParameters.EMPTY);
                    proxyFilter.beginWikiDocumentLocale(Locale.ROOT, FilterEventParameters.EMPTY);

                    readPageRevision(xmlReader, filter, proxyFilter);
                } else {
                    StAXUtils.skipElement(xmlReader);
                }
            } else {
                StAXUtils.skipElement(xmlReader);
            }
        }

        proxyFilter.endWikiDocumentLocale(Locale.ROOT, FilterEventParameters.EMPTY);
        proxyFilter.endWikiDocument(this.currentParentReference.getName(), FilterEventParameters.EMPTY);
    }

    private void sendSpaceEvents(MediaWikiFilter proxyFilter) throws FilterException
    {
        // Close spaces that need to be closed
        if (this.previousParentReference != null) {
            sendEndParents(proxyFilter);
        }

        // Open spaces that need to be open
        sendBeginParents(proxyFilter);
    }

    private void sendEndParents(MediaWikiFilter proxyFilter) throws FilterException
    {
        List<EntityReference> previousParents = this.previousParentReference.getReversedReferenceChain();
        List<EntityReference> currentParents = this.currentParentReference.getReversedReferenceChain();

        // Find the first different level
        int i = 0;
        while (i < previousParents.size() && i < currentParents.size()) {
            if (!currentParents.get(i).equals(previousParents.get(i))) {
                break;
            }

            ++i;
        }

        if (i < previousParents.size()) {
            // Delete what is different
            for (int diff = previousParents.size() - i; diff > 0; --diff, this.previousParentReference =
                this.previousParentReference.getParent()) {
                if (this.previousParentReference.getType() == EntityType.WIKI) {
                    proxyFilter.endWiki(this.previousParentReference.getName(), FilterEventParameters.EMPTY);
                } else {
                    proxyFilter.endWikiSpace(this.previousParentReference.getName(), FilterEventParameters.EMPTY);
                }
            }
        }
    }

    private void sendBeginParents(MediaWikiFilter proxyFilter) throws FilterException
    {
        int previousSize = this.previousParentReference != null ? this.previousParentReference.size() : 0;
        int currentSize = this.currentParentReference != null ? this.currentParentReference.size() : 0;

        int diff = currentSize - previousSize;

        if (diff > 0) {
            List<EntityReference> parents = this.currentParentReference.getReversedReferenceChain();
            for (int i = parents.size() - diff; i < parents.size(); ++i) {
                EntityReference parent = parents.get(i);
                if (parent.getType() == EntityType.WIKI) {
                    proxyFilter.beginWiki(parent.getName(), FilterEventParameters.EMPTY);
                } else {
                    proxyFilter.beginWikiSpace(parent.getName(), FilterEventParameters.EMPTY);
                }

                this.previousParentReference =
                    new EntityReference(parent.getName(), parent.getType(), this.previousParentReference);
            }
        }
    }

    private void readPageRevision(XMLStreamReader xmlReader, Object filter, MediaWikiFilter proxyFilter)
        throws XMLStreamException, FilterException
    {
        FilterEventParameters pageRevisionParameters = new FilterEventParameters();

        // Defaults
        pageRevisionParameters.put(WikiDocumentFilter.PARAMETER_REVISION_MINOR, false);

        pageRevisionParameters.put(WikiDocumentFilter.PARAMETER_TITLE, this.currentPageTitle);

        String version = "1.1";

        boolean beginWikiDocumentRevisionSent = false;

        for (xmlReader.nextTag(); xmlReader.isStartElement(); xmlReader.nextTag()) {
            String elementName = xmlReader.getLocalName();

            if (elementName.equals(TAG_PAGE_REVISION_VERSION)) {
                version = xmlReader.getElementText();
            } else if (elementName.equals(TAG_PAGE_REVISION_COMMENT)) {
                pageRevisionParameters.put(WikiDocumentFilter.PARAMETER_REVISION_COMMENT, xmlReader.getElementText());
            } else if (elementName.equals(TAG_PAGE_REVISION_CONTENT)) {
                String content = xmlReader.getElementText();

                if (filter instanceof Listener) {
                    // Begin document revision
                    proxyFilter.beginWikiDocumentRevision(version, pageRevisionParameters);
                    beginWikiDocumentRevisionSent = true;

                    ReferenceType referenceType = this.context.getReferenceType();
                    try {
                        // Make sure to keep source references unchanged
                        this.context.setReferenceType(ReferenceType.MEDIAWIKI);

                        // Generate events
                        MediaWikiContextConverterListener listener = this.listenerProvider.get();
                        listener.initialize(listener, this.properties);
                        this.confluenceWIKIParser.parse(new StringReader(content), listener);
                    } catch (ParseException e) {
                        throw new FilterException("Failed to parse MediaWiki syntax content", e);
                    } finally {
                        this.context.setReferenceType(referenceType);
                    }
                } else if (this.properties.isConvertToXWiki()) {
                    // FIXME: Convert to latest XWiki syntax
                    pageRevisionParameters.put(WikiDocumentFilter.PARAMETER_CONTENT, content);
                    pageRevisionParameters.put(WikiDocumentFilter.PARAMETER_SYNTAX, MediaWikiStreamParser.SYNTAX);
                } else {
                    // Keep MediaWiki syntax
                    pageRevisionParameters.put(WikiDocumentFilter.PARAMETER_CONTENT, content);
                    pageRevisionParameters.put(WikiDocumentFilter.PARAMETER_SYNTAX, MediaWikiStreamParser.SYNTAX);
                }
            } else if (elementName.equals(TAG_PAGE_REVISION_MINOR)) {
                pageRevisionParameters.put(WikiDocumentFilter.PARAMETER_REVISION_MINOR, true);
            } else if (elementName.equals(TAG_PAGE_REVISION_TIMESTAMP)) {
                try {
                    Date date = DatatypeFactory.newInstance().newXMLGregorianCalendar(xmlReader.getElementText())
                        .toGregorianCalendar().getTime();
                    pageRevisionParameters.put(WikiDocumentFilter.PARAMETER_REVISION_DATE, date);
                } catch (DatatypeConfigurationException e) {
                    this.logger.error("Failed to create DatatypeFactory instance", e);
                }
            } else if (elementName.equals(TAG_PAGE_REVISION_CONTRIBUTOR)) {
                pageRevisionParameters.put(WikiDocumentFilter.PARAMETER_REVISION_AUTHOR, getPageContributor(xmlReader));
            } else {
                StAXUtils.skipElement(xmlReader);
            }
        }

        if (!beginWikiDocumentRevisionSent) {
            proxyFilter.beginWikiDocumentRevision(version, pageRevisionParameters);
        }

        proxyFilter.endWikiDocumentRevision(version, pageRevisionParameters);
    }

    private String getPageContributor(XMLStreamReader xmlReader) throws XMLStreamException
    {
        for (xmlReader.nextTag(); xmlReader.isStartElement(); xmlReader.nextTag()) {
            String elementName = xmlReader.getLocalName();

            if (elementName.equals(TAG_PAGE_REVISION_CONTRIBUTOR_USERNAME)) {
                return xmlReader.getElementText();
            } else if (elementName.equals(TAG_PAGE_REVISION_CONTRIBUTOR_IP)) {
                if (!this.properties.isConvertToXWiki()) {
                    return xmlReader.getElementText();
                }
            }
        }

        return null;
    }
}
