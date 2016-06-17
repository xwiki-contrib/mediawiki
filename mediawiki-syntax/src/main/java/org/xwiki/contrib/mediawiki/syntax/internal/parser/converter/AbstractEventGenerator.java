package org.xwiki.contrib.mediawiki.syntax.internal.parser.converter;

import org.xwiki.filter.FilterException;
import org.xwiki.rendering.listener.Listener;

import info.bliki.htmlcleaner.BaseToken;
import info.bliki.htmlcleaner.TagNode;
import info.bliki.wiki.model.IWikiModel;

public abstract class AbstractEventGenerator implements EventGenerator
{
    protected BaseToken token;

    protected EventConverter converter;

    public AbstractEventGenerator()
    {
    }

    @Override
    public void init(BaseToken token, EventConverter converter)
    {
        this.token = token;
        this.converter = converter;
    }

    @Override
    public AbstractEventGenerator clone() throws CloneNotSupportedException
    {
        return (AbstractEventGenerator) super.clone();
    }

    protected void begin(Listener listener) throws FilterException
    {
        // To overwrite
    }

    protected void end(Listener listener) throws FilterException
    {
        // To overwrite
    }

    @Override
    public void traverse(Listener listener, IWikiModel model) throws FilterException
    {
        begin(listener);

        if (this.token instanceof TagNode) {
            this.converter.traverse(((TagNode) this.token).getChildren(), model);
        }

        end(listener);
    }
}
