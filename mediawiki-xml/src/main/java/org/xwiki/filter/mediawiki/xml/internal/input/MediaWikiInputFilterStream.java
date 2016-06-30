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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.contrib.mediawiki.syntax.MediaWikiSyntaxInputProperties;
import org.xwiki.contrib.mediawiki.syntax.MediaWikiSyntaxInputProperties.ReferenceType;
import org.xwiki.contrib.mediawiki.syntax.internal.parser.MediaWikiStreamParser;
import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.event.model.WikiDocumentFilter;
import org.xwiki.filter.input.AbstractBeanInputFilterStream;
import org.xwiki.filter.input.BeanInputFilterStream;
import org.xwiki.filter.input.BeanInputFilterStreamFactory;
import org.xwiki.filter.input.FileInputSource;
import org.xwiki.filter.input.InputFilterStreamFactory;
import org.xwiki.filter.input.InputSource;
import org.xwiki.filter.input.InputStreamInputSource;
import org.xwiki.filter.input.ReaderInputSource;
import org.xwiki.filter.input.StringInputSource;
import org.xwiki.filter.mediawiki.input.MediaWikiInputProperties;
import org.xwiki.filter.mediawiki.xml.internal.MediaWikiFilter;
import org.xwiki.filter.xml.input.SourceInputSource;
import org.xwiki.model.EntityType;
import org.xwiki.model.ModelConfiguration;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
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
    @Named(MediaWikiSyntaxInputProperties.FILTER_STREAM_TYPE_STRING)
    private InputFilterStreamFactory parserFactory;

    @Inject
    private ModelConfiguration modelConfiguration;

    @Inject
    private Provider<MediaWikiContextConverterListener> listenerProvider;

    @Inject
    @Named("xwiki/2.1")
    private PrintRendererFactory xwiki21Factory;

    private Map<String, String> namespaces = new HashMap<>();

    String currentPageNamespace;

    String currentPageTitle;

    EntityReference previousParentReference;

    EntityReference currentParentReference;

    Set<String> currentFiles;

    MediaWikiInputProperties getProperties()
    {
        return this.properties;
    }

    EntityReference toEntityReference(String reference)
    {
        String pageName = reference;
        String namespace;

        // Separate namespace and page name
        int index = pageName.indexOf(':');
        if (index > 0) {
            namespace = pageName.substring(0, index);
            pageName = pageName.substring(index + 1);
        } else {
            namespace = null;
        }

        // MediaWiki replace the white spaces with an underscore in the URL
        pageName = pageName.replace(' ', '_');

        // Maybe convert MediaWiki home page name into XWiki home page name
        if (this.properties.isConvertToXWiki() && pageName.equals(PAGE_NAME_MAIN)) {
            pageName = this.modelConfiguration.getDefaultReferenceValue(EntityType.DOCUMENT);
        }

        // Find page parent reference
        EntityReference parentReference;
        if (namespace != null) {
            if (namespace.equalsIgnoreCase(NAMESPACE_FILE)) {
                return toFileEntityReference(pageName);
            } else {
                parentReference = new EntityReference(namespace, EntityType.SPACE, this.properties.getParent());
            }
        } else if (this.currentPageNamespace.equals(NAMESPACE_USER)) {
            // TODO: add support for users
            return null;
        } else if (this.currentPageNamespace.equals(NAMESPACE_SPECIAL)) {
            return null;
        } else {
            if (this.properties.getParent() == null) {
                parentReference =
                    new EntityReference(this.modelConfiguration.getDefaultReferenceValue(EntityType.SPACE),
                        EntityType.SPACE, this.properties.getParent());
            } else {
                parentReference = this.properties.getParent();
            }
        }

        return new EntityReference(pageName, EntityType.DOCUMENT, parentReference);
    }

    EntityReference toFileEntityReference(String pageName)
    {
        // Don't import File namespace page if files are attached to each page using it
        if (this.properties.isFileAttached()) {
            return null;
        }

        EntityReference parentReference = this.properties.getFileSpace();
        if (parentReference != null && parentReference.extractFirstReference(EntityType.WIKI) == null) {
            parentReference = new EntityReference(parentReference, this.properties.getParent());
        }

        return new EntityReference(pageName, EntityType.DOCUMENT, parentReference);
    }

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
        throws XMLStreamException, FilterException, IOException
    {
        xmlReader.nextTag();

        readMediaWiki(xmlReader, filter, proxyFilter);
    }

    private void readMediaWiki(XMLStreamReader xmlReader, Object filter, MediaWikiFilter proxyFilter)
        throws XMLStreamException, FilterException, IOException
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
        throws XMLStreamException, FilterException, IOException
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
                // Find current page reference
                EntityReference pageReference = toEntityReference(this.currentPageTitle);

                if (pageReference != null) {
                    this.currentParentReference = pageReference.getParent();

                    // Send parent events
                    sendSpaceEvents(proxyFilter);

                    // Send document event
                    proxyFilter.beginWikiDocument(pageReference.getName(), FilterEventParameters.EMPTY);
                    proxyFilter.beginWikiDocumentLocale(Locale.ROOT, FilterEventParameters.EMPTY);

                    readPageRevision(xmlReader, filter, proxyFilter);
                } else {
                    skip = true;

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
        throws XMLStreamException, FilterException, IOException
    {
        FilterEventParameters pageRevisionParameters = new FilterEventParameters();

        // Defaults
        pageRevisionParameters.put(WikiDocumentFilter.PARAMETER_REVISION_MINOR, false);

        pageRevisionParameters.put(WikiDocumentFilter.PARAMETER_TITLE, this.currentPageTitle);

        String version = "1.1";

        boolean beginWikiDocumentRevisionSent = false;

        this.currentFiles = Collections.emptySet();

        for (xmlReader.nextTag(); xmlReader.isStartElement(); xmlReader.nextTag()) {
            String elementName = xmlReader.getLocalName();

            if (elementName.equals(TAG_PAGE_REVISION_VERSION)) {
                version = xmlReader.getElementText();
            } else if (elementName.equals(TAG_PAGE_REVISION_COMMENT)) {
                pageRevisionParameters.put(WikiDocumentFilter.PARAMETER_REVISION_COMMENT, xmlReader.getElementText());
            } else if (elementName.equals(TAG_PAGE_REVISION_CONTENT)) {
                String content = xmlReader.getElementText();

                if (this.properties.isContentEvents() && filter instanceof Listener) {
                    // Begin document revision
                    proxyFilter.beginWikiDocumentRevision(version, pageRevisionParameters);
                    beginWikiDocumentRevisionSent = true;

                    MediaWikiSyntaxInputProperties parserProperties = new MediaWikiSyntaxInputProperties();
                    parserProperties.setSource(new StringInputSource(content));
                    // Make sure to keep source references unchanged
                    parserProperties.setReferenceType(ReferenceType.MEDIAWIKI);

                    // Refactor references and find attachments
                    MediaWikiContextConverterListener listener = this.listenerProvider.get();
                    listener.initialize(proxyFilter, this, null);

                    // Generate events
                    try (BeanInputFilterStream<MediaWikiSyntaxInputProperties> stream =
                        ((BeanInputFilterStreamFactory) this.parserFactory).createInputFilterStream(parserProperties)) {
                        stream.read(listener);
                    }

                    // Remember linked files
                    this.currentFiles = listener.getFiles();
                } else if (this.properties.isConvertToXWiki()) {
                    // Convert content to XWiki syntax
                    pageRevisionParameters.put(WikiDocumentFilter.PARAMETER_CONTENT, convertToXWiki21(content));
                    pageRevisionParameters.put(WikiDocumentFilter.PARAMETER_SYNTAX, Syntax.XWIKI_2_1);
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

        // It might be a page dedicated to a file
        if (this.currentPageTitle.startsWith(NAMESPACE_FILE)) {
            this.currentFiles.add(this.currentPageTitle.substring(NAMESPACE_FILE.length() + 1).replace(' ', '_'));
        }

        // Attach files if any
        if (!this.currentFiles.isEmpty()) {
            for (String fileName : this.currentFiles) {
                sendAttachment(fileName, proxyFilter);
            }
        }

        // Reset files
        this.currentFiles = null;

        proxyFilter.endWikiDocumentRevision(version, pageRevisionParameters);
    }

    private String convertToXWiki21(String content)
    {
        DefaultWikiPrinter printer = new DefaultWikiPrinter();
        PrintRenderer renderer = this.xwiki21Factory.createRenderer(printer);

        MediaWikiSyntaxInputProperties parserProperties = new MediaWikiSyntaxInputProperties();
        parserProperties.setSource(new StringInputSource(content));
        // Make sure to keep source references unchanged
        parserProperties.setReferenceType(ReferenceType.MEDIAWIKI);

        // Refactor references and find attachments
        MediaWikiContextConverterListener listener = this.listenerProvider.get();
        listener.initialize(renderer, this, this.xwiki21Factory.getSyntax());

        // Generate events
        try (BeanInputFilterStream<MediaWikiSyntaxInputProperties> stream =
            ((BeanInputFilterStreamFactory) this.parserFactory).createInputFilterStream(parserProperties)) {
            stream.read(listener);
        } catch (Exception e) {
            // TODO log something ?
        }

        this.currentFiles = listener.getFiles();

        return printer.toString();
    }

    private void sendAttachment(String fileName, MediaWikiFilter proxyFilter) throws FilterException, IOException
    {
        File file = getFile(fileName);

        if (file != null) {
            try (InputStream streamToClose = new FileInputStream(file)) {
                proxyFilter.onWikiAttachment(fileName, streamToClose, file.length(), FilterEventParameters.EMPTY);
            }
        }
    }

    private File getFile(String fileName) throws FilterException
    {
        InputSource files = this.properties.getFiles();

        if (files instanceof FileInputSource) {
            File folder = ((FileInputSource) files).getFile();

            String md5Hex = DigestUtils.md5Hex(fileName).substring(0, 2);
            String folderName1 = md5Hex.substring(0, 1);
            String folderName2 = md5Hex.substring(0, 2);

            File folder1 = new File(folder, folderName1);
            File folder2 = new File(folder1, folderName2);

            File file = new File(folder2, fileName);

            if (file.exists() && file.isFile()) {
                return file;
            }

            this.logger.warn("Can't find file [{}]", file.getAbsolutePath());
        } else {
            throw new FilterException("Unsupported input source [" + files.getClass() + "] ([" + files + "])");
        }

        return null;
    }

    private String getPageContributor(XMLStreamReader xmlReader) throws XMLStreamException
    {
        String userName = null;

        for (xmlReader.nextTag(); xmlReader.isStartElement(); xmlReader.nextTag()) {
            String elementName = xmlReader.getLocalName();

            switch (elementName) {
                case TAG_PAGE_REVISION_CONTRIBUTOR_USERNAME:
                    userName = xmlReader.getElementText();
                    continue;

                case TAG_PAGE_REVISION_CONTRIBUTOR_IP:
                    if (!this.properties.isConvertToXWiki()) {
                        userName = xmlReader.getElementText();
                        continue;
                    }
            }

            StAXUtils.skipElement(xmlReader);
        }

        return userName;
    }
}