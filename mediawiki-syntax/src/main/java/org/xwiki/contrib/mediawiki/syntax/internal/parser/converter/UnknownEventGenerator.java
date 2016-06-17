package org.xwiki.contrib.mediawiki.syntax.internal.parser.converter;

import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.UnknownFilter;
import org.xwiki.rendering.listener.Listener;

import info.bliki.htmlcleaner.BaseToken;
import info.bliki.htmlcleaner.TagNode;
import info.bliki.htmlcleaner.TagToken;

public class UnknownEventGenerator extends AbstractEventGenerator
{
    private String id;

    private FilterEventParameters parameters;

    private boolean on = true;

    public UnknownEventGenerator()
    {
    }

    @Override
    public void init(BaseToken token, EventConverter converter)
    {
        super.init(token, converter);

        TagToken tagToken = (TagToken) token;

        this.id = tagToken.getName();

        if (token instanceof TagNode) {
            this.parameters = new FilterEventParameters();
            this.parameters.putAll(((TagNode) token).getAttributes());

            this.on = !((TagNode) token).getChildren().isEmpty();
        }
    }

    @Override
    protected void begin(Listener listener) throws FilterException
    {
        if (listener instanceof UnknownFilter) {
            if (!this.on) {
                ((UnknownFilter) listener).beginUnknwon(this.id, this.parameters);
            }
        }
    }

    @Override
    protected void end(Listener listener) throws FilterException
    {
        if (listener instanceof UnknownFilter) {
            if (!this.on) {
                ((UnknownFilter) listener).endUnknwon(this.id, this.parameters);
            } else {
                ((UnknownFilter) listener).onUnknwon(this.id, this.parameters);
            }
        }
    }
}
