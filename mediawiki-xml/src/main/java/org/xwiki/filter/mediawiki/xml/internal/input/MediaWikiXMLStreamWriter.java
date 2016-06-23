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

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.contrib.mediawiki.syntax.internal.parser.MediaWikiStreamParser;
import org.xwiki.filter.FilterDescriptorManager;
import org.xwiki.filter.mediawiki.input.MediaWikiXMLInputProperties;
import org.xwiki.filter.mediawiki.xml.internal.MediaWikiFilter;
import org.xwiki.rendering.parser.StreamParser;

/**
 * @version $Id: 41df1dab66b03111214dbec56fee8dbd44747638 $
 */
@Component(roles = MediaWikiXMLStreamWriter.class)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class MediaWikiXMLStreamWriter implements XMLStreamWriter
{
    @Inject
    private FilterDescriptorManager filterDescriptorManager;

    @Inject
    private Logger logger;

    @Inject
    @Named(MediaWikiStreamParser.MEDIAWIKI_1_6_STRING)
    private StreamParser syntaxParser;

    private Object filter;

    private MediaWikiFilter filterProxy;

    private MediaWikiXMLInputProperties parameters;

    /**
     * @param filter the filter to send events to
     * @param parameters the parameters to control le behavior
     */
    public void init(Object filter, MediaWikiXMLInputProperties parameters)
    {
        this.filter = filter;
        this.filterProxy = filterDescriptorManager.createFilterProxy(this.filter, MediaWikiFilter.class);
        this.parameters = parameters;
    }

    @Override
    public void writeStartElement(String localName) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeEndElement() throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeEndDocument() throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void flush() throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
        throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeComment(String data) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeCData(String data) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeStartDocument() throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public NamespaceContext getNamespaceContext()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
