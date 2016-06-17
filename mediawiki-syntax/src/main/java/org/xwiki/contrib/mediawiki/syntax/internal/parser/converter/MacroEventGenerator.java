package org.xwiki.contrib.mediawiki.syntax.internal.parser.converter;

import java.util.Map;

import org.xwiki.filter.FilterException;
import org.xwiki.rendering.listener.Listener;

import info.bliki.htmlcleaner.BaseToken;
import info.bliki.htmlcleaner.TagNode;
import info.bliki.wiki.model.IWikiModel;

public class MacroEventGenerator extends AbstractEventGenerator
{
    private String id;

    private String content;

    private Map<String, String> parameters;

    public MacroEventGenerator()
    {
    }

    @Override
    public void init(BaseToken token, EventConverter converter)
    {
        super.init(token, converter);

        TagNode node = (TagNode) token;

        this.id = node.getName();
        this.content = node.getBodyString();
        this.parameters = node.getAttributes();
    }

    @Override
    public void traverse(Listener listener, IWikiModel model) throws FilterException
    {
        // TODO: find if the macro is inline or not
        listener.onMacro(this.id, this.parameters, this.content, false);
    }
}
