package org.xwiki.contrib.mediawiki.syntax.internal.parser.converter;

import java.util.LinkedHashMap;
import java.util.Map;

import org.xwiki.filter.FilterException;
import org.xwiki.rendering.listener.Listener;

import info.bliki.htmlcleaner.BaseToken;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.tags.SourceTag;

public class SourceEventGenerator extends AbstractEventGenerator
{
    private boolean inline;

    private String content;

    private Map<String, String> parameters;

    public SourceEventGenerator()
    {
    }

    @Override
    public void init(BaseToken token, EventConverter converter)
    {
        super.init(token, converter);

        SourceTag source = (SourceTag) token;

        this.content = source.getBodyString();

        Map<String, String> attributes = source.getAttributes();

        this.parameters = new LinkedHashMap<>();
        String language = attributes.get("lang");
        if (language != null) {
            this.parameters.put("language", language);
        }

        this.inline = attributes.containsKey("inline");
    }

    @Override
    public void traverse(Listener listener, IWikiModel model) throws FilterException
    {
        listener.onMacro("code", this.parameters, this.content, this.inline);
    }

    @Override
    public SourceEventGenerator clone() throws CloneNotSupportedException
    {
        return (SourceEventGenerator) super.clone();
    }
}
