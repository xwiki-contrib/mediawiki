package org.xwiki.contrib.mediawiki.syntax.internal.parser.converter;

import java.util.Map;

import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.Listener;

import info.bliki.htmlcleaner.BaseToken;
import info.bliki.htmlcleaner.TagNode;

public class BoldItalicEventGenerator extends AbstractEventGenerator
{
    private Map<String, String> parameters;

    public BoldItalicEventGenerator()
    {
    }

    @Override
    public void init(BaseToken token, EventConverter converter)
    {
        super.init(token, converter);

        if (token instanceof TagNode) {
            this.parameters = ((TagNode) token).getAttributes();
        }
    }

    @Override
    public void begin(Listener listener)
    {
        listener.beginFormat(Format.BOLD, this.parameters);
        listener.beginFormat(Format.ITALIC, this.parameters);
    }

    @Override
    public void end(Listener listener)
    {
        listener.endFormat(Format.ITALIC, this.parameters);
        listener.endFormat(Format.BOLD, this.parameters);
    }
}
