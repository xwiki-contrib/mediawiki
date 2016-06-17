package org.xwiki.contrib.mediawiki.syntax.internal.parser.converter;

import java.util.Map;

import org.xwiki.filter.FilterException;
import org.xwiki.rendering.listener.Listener;

import info.bliki.htmlcleaner.BaseToken;
import info.bliki.htmlcleaner.TagNode;
import info.bliki.wiki.model.IWikiModel;

public class VerbatimEventGenerator extends AbstractEventGenerator
{
    private boolean inline;

    private String content;

    private Map<String, String> parameters;

    public VerbatimEventGenerator(boolean inline)
    {
        this.inline = inline;
    }

    @Override
    public void init(BaseToken token, EventConverter converter)
    {
        super.init(token, converter);

        TagNode node = (TagNode) token;

        this.content = node.getBodyString();
        this.parameters = node.getAttributes();
    }

    @Override
    public void traverse(Listener listener, IWikiModel model) throws FilterException
    {
        listener.onVerbatim(this.content, this.inline, this.parameters);
    }
}
