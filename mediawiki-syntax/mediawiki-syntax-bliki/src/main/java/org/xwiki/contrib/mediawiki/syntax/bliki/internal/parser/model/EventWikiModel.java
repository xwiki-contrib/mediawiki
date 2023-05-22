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
package org.xwiki.contrib.mediawiki.syntax.bliki.internal.parser.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.contrib.mediawiki.syntax.MediaWikiSyntaxInputProperties;
import org.xwiki.contrib.mediawiki.syntax.MediaWikiSyntaxInputProperties.ReferenceType;
import org.xwiki.rendering.listener.reference.AttachmentResourceReference;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ResourceReferenceParser;

import info.bliki.htmlcleaner.BaseToken;
import info.bliki.htmlcleaner.ContentToken;
import info.bliki.htmlcleaner.TagNode;
import info.bliki.htmlcleaner.TagToken;
import info.bliki.wiki.filter.AbstractWikipediaParser;
import info.bliki.wiki.filter.WikipediaParser;
import info.bliki.wiki.filter.WikipediaPreTagParser;
import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.model.ImageFormat;
import info.bliki.wiki.model.WikiModel;
import info.bliki.wiki.namespaces.INamespace.NamespaceCode;
import info.bliki.wiki.namespaces.Namespace;
import info.bliki.wiki.namespaces.Namespace.NamespaceValue;
import info.bliki.wiki.tags.PTag;

/**
 * Custom WikiModel.
 * 
 * @version $Id$
 */
@Component(roles = EventWikiModel.class)
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class EventWikiModel extends WikiModel
{
    private static final Set<String> FORMATTED_NODE_NAMES =
        new HashSet<>(Arrays.asList("table", "tr", "ul", "ol", "tbody", "tfoot", "thead"));

    /**
     * Note the greatest URL validator ever but it's actually very close to what MediaWiki does...
     */
    private static final Pattern URL_SCHEME_PATTERN = Pattern.compile("[a-zA-Z0-9+.-]*://.*");

    private static final Tika TIKA = new Tika();

    class Tokens implements Map<String, TagToken>
    {
        @Override
        public int size()
        {
            return EventWikiModel.super.getTokenMap().size();
        }

        @Override
        public boolean isEmpty()
        {
            return EventWikiModel.super.getTokenMap().isEmpty();
        }

        @Override
        public boolean containsKey(Object key)
        {
            return EventWikiModel.super.getTokenMap().containsKey(key);
        }

        @Override
        public boolean containsValue(Object value)
        {
            return EventWikiModel.super.getTokenMap().containsValue(value);
        }

        @Override
        public TagToken get(Object key)
        {
            TagToken token = EventWikiModel.super.getTokenMap().get(key);

            if (token == null) {
                String keyString = (String) key;
                if (keyString.startsWith(XMacroTag.TAGPREFIX)) {
                    String macroId = ((String) key).substring(XMacroTag.TAGPREFIX.length());
                    token = new XMacroTag(macroId, EventWikiModel.this);
                }
            }

            return token;
        }

        @Override
        public TagToken put(String key, TagToken value)
        {
            return EventWikiModel.super.getTokenMap().put(key, value);
        }

        @Override
        public TagToken remove(Object key)
        {
            return EventWikiModel.super.getTokenMap().remove(key);
        }

        @Override
        public void putAll(Map<? extends String, ? extends TagToken> m)
        {
            EventWikiModel.super.getTokenMap().putAll(m);
        }

        @Override
        public void clear()
        {
            EventWikiModel.super.getTokenMap().clear();
        }

        @Override
        public Set<String> keySet()
        {
            return EventWikiModel.super.getTokenMap().keySet();
        }

        @Override
        public Collection<TagToken> values()
        {
            return EventWikiModel.super.getTokenMap().values();
        }

        @Override
        public Set<java.util.Map.Entry<String, TagToken>> entrySet()
        {
            return EventWikiModel.super.getTokenMap().entrySet();
        }

    }

    @Inject
    @Named("default/link")
    private ResourceReferenceParser linkReferenceParser;

    private boolean nopipe;

    /**
     * @see #getImageReferenceParser()
     */
    @Inject
    @Named("default/image")
    private ResourceReferenceParser imageReferenceParser;

    private final Tokens tokens = new Tokens();

    private MediaWikiSyntaxInputProperties properties;

    /**
     * Default constructor.
     */
    public EventWikiModel()
    {
        super("${image}", "${title}");

        addStandaloneMacroTag("blockquote");
        addInlineMacroTag("code");

        addTokenTag(new GalleryXMacroTag());

        // Security is not our concern at this level
        TagNode.addAllowedAttribute("style");
    }

    @Override
    public void setUp()
    {
        super.setUp();

        // Make sure to keep the order from the content
        this.categories = new LinkedHashMap<>();
    }

    @Override
    public AbstractWikipediaParser createNewInstance(String rawWikitext)
    {
        WikipediaParser wikipediaParser = (WikipediaParser) super.createNewInstance(rawWikitext);

        wikipediaParser.setNoToC(this.properties.isNoToc());
        wikipediaParser.setTemplateTag(true);

        return wikipediaParser;
    }

    @Override
    public void substituteTemplateCall(String templateName, Map<String, String> parameterMap, Appendable writer)
        throws IOException
    {
        // Put back template
        writer.append("{{");
        writer.append(templateName);
        if (!parameterMap.isEmpty()) {
            for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
                writer.append('|');
                writer.append(entry.getKey());
                writer.append('=');
                writer.append(entry.getValue());
            }
        }
        writer.append("}}");
    }

    private void addTokenTag(GalleryXMacroTag tag)
    {
        addTokenTag(tag.getName(), tag);
    }

    private void addStandaloneMacroTag(String name)
    {
        addTokenTag(name, new XMacroTag(name, false, this));
    }

    private void addInlineMacroTag(String name)
    {
        addTokenTag(name, new XMacroTag(name, true, this));
    }

    public void init(MediaWikiSyntaxInputProperties properties)
    {
        this.properties = properties;

        // Set custom namespaces
        if (this.properties.getCustomNamespaces() != null) {
            Namespace namespaces = (Namespace) this.fNamespace;

            for (Map.Entry<Integer, Collection<String>> entry : this.properties.getCustomNamespaces().entrySet()) {
                final NamespaceValue namespace = namespaces.getNamespaceByNumber(entry.getKey());
                if (namespace != null) {
                    for (String alias : entry.getValue()) {
                        namespace.addAlias(alias);
                    }
                }
            }
        }
    }

    @Override
    public boolean appendRawNamespaceLinks(String rawNamespaceTopic, String viewableLinkDescription,
        boolean containsNoPipe)
    {
        this.nopipe = containsNoPipe;

        return super.appendRawNamespaceLinks(rawNamespaceTopic, viewableLinkDescription, containsNoPipe);
    }

    private String cleanReference(String reference)
    {
        String cleanReference = reference;

        if (this.properties.getReferenceType() == ReferenceType.MEDIAWIKI) {
            // MediaWiki automatically replace white space with underscore in pages or files
            cleanReference = cleanReference.replace(' ', '_');

            // MediaWiki automatically capitalize references to pages or files
            cleanReference = StringUtils.capitalize(cleanReference);
        }

        return cleanReference;
    }

    private ResourceReference toResourceReference(String topic, String hashSection)
    {
        ResourceReference reference = null;

        String anchor;
        if (StringUtils.isEmpty(hashSection)) {
            anchor = null;
        } else {
            // MediaWiki automatically replace white space in hash section by underscore
            anchor = encodeTitleDotUrl(hashSection, false);
        }

        if (this.properties.getReferenceType() == ReferenceType.NONE || StringUtils.isEmpty(topic)) {
            if (anchor != null) {
                reference = new ResourceReference(topic + '#' + anchor, ResourceType.PATH);
            } else {
                reference = new ResourceReference(topic, ResourceType.PATH);
            }
        } else {
            int index = topic.indexOf(':', 1);
            if (index > 0) {
                String namespace = topic.substring(0, index);
                if (this.fNamespace.isNamespace(namespace, NamespaceCode.MEDIA_NAMESPACE_KEY)) {
                    reference =
                        new AttachmentResourceReference(cleanReference(topic.substring(namespace.length() + 1)));
                }
            }
        }

        // Fallback on standard link reference parser
        if (reference == null) {
            if (this.properties.getReferenceType() == ReferenceType.XWIKI) {
                reference = this.linkReferenceParser.parse(topic);
            } else {
                reference = new DocumentResourceReference(cleanReference(topic));
            }

            // Set anchor
            if (anchor != null) {
                if (reference instanceof DocumentResourceReference) {
                    ((DocumentResourceReference) reference).setAnchor(anchor);
                } else {
                    reference = this.linkReferenceParser.parse(topic + '#' + anchor);
                }
            }
        }

        return reference;
    }

    @Override
    public void appendInternalLink(String topic, String hashSection, String topicDescription, String cssClass,
        boolean parseRecursive)
    {
        // Create reference
        ResourceReference reference = toResourceReference(topic, hashSection);

        // Create tag
        LinkTag linkTag = new LinkTag(reference, false);

        // Append tag
        pushNode(linkTag);
        if (!this.nopipe) {
            if (parseRecursive) {
                WikipediaPreTagParser.parseRecursive(topicDescription, this, false, true);
            } else {
                linkTag.addChild(new ContentToken(topicDescription));
            }
        }
        popNode();
    }

    @Override
    public void appendInternalImageLink(String hrefImageLink, String srcImageLink, ImageFormat imageFormat)
    {
        // If the image is not actually an image generate an attachment link
        String contentType = TIKA.detect(imageFormat.getFilename());
        if (MediaType.parse(contentType).getType().equals("image")) {
            // Image source
            ResourceReference reference;
            if (this.properties.getReferenceType() == ReferenceType.XWIKI) {
                reference = this.imageReferenceParser.parse(imageFormat.getFilename());
            } else {
                reference = new AttachmentResourceReference(cleanReference(imageFormat.getFilename()));
                reference.setTyped(false);
            }

            // Image link
            ResourceReference link;
            if (imageFormat.getLink() == null) {
                link = toResourceReference(imageFormat.getNamespace() + ':' + imageFormat.getFilename(), null);
            } else if (imageFormat.getLink().isEmpty()) {
                link = null;
            } else {
                if (URL_SCHEME_PATTERN.matcher(imageFormat.getLink()).matches()) {
                    link = new ResourceReference(imageFormat.getLink(), ResourceType.URL);
                } else {
                    link = toResourceReference(imageFormat.getLink(), null);
                }
            }

            // Create tag
            ImageTag imageTag = new ImageTag(reference, false, link, imageFormat);

            if (imageFormat.getWidthStr() != null) {
                imageTag.addAttribute("width", imageFormat.getWidthStr(), false);
            }

            if (imageFormat.getHeightStr() != null) {
                imageTag.addAttribute("height", imageFormat.getHeightStr(), false);
            }

            append(imageTag);
        } else {
            AttachmentResourceReference reference =
                new AttachmentResourceReference(cleanReference(imageFormat.getFilename()));
            LinkTag linkTag = new LinkTag(reference, false);

            // Append tag
            append(linkTag);
        }
    }

    @Override
    public void appendMailtoLink(String link, String linkName, boolean withoutSquareBrackets)
    {
        ResourceReference reference = new ResourceReference(link.substring("mailto:".length()), ResourceType.MAILTO);

        LinkTag linkTag = new LinkTag(reference, withoutSquareBrackets);

        if (!withoutSquareBrackets && !StringUtils.equals(link, linkName)) {
            linkTag.addChild(new ContentToken(linkName));
        }

        append(linkTag);
    }

    @Override
    public void append(BaseToken token)
    {
        // Generate missing paragraph
        if (token instanceof ContentToken) {
            appendContentToken((ContentToken) token);
        } else {
            super.append(token);
        }
    }

    private void appendContentToken(ContentToken token)
    {
        if (stackSize() == 0 || !FORMATTED_NODE_NAMES.contains(peekNode().getName()))
            if (stackSize() == 0 && getRecursionLevel() == 1) {
                // Workaround https://bitbucket.org/axelclk/info.bliki.wiki/issues/34
                if (!token.getContent().equals("\n")) {
                    pushNode(new PTag());

                    super.append(token);
                }
            } else {
                super.append(token);
            }
    }

    @Override
    public void appendExternalLink(String uriSchemeName, String link, String linkName, boolean withoutSquareBrackets)
    {
        // TODO: support uri with empty scheme (fallback on current request scheme)

        ResourceReference reference = new ResourceReference(link, ResourceType.URL);

        LinkTag linkTag = new LinkTag(reference, withoutSquareBrackets);

        if (!withoutSquareBrackets && !StringUtils.equals(link, linkName)) {
            linkTag.addChild(new ContentToken(linkName));
        }

        append(linkTag);
    }

    boolean isInline()
    {
        TagToken token = peekNode();

        if (token != null) {
            if (!Configuration.SPECIAL_BLOCK_TAGS.contains('|' + token.getName() + '|')) {
                if (token instanceof PTag) {
                    // Bliki always produce a opening P tag no matter what and only close it later of the following tag
                    // is a standalone tag
                    return ((PTag) token).getChildren().size() > 0;
                } else {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Map<String, TagToken> getTokenMap()
    {
        return this.tokens;
    }
}
