package org.xwiki.contrib.mediawiki.syntax.internal.parser.converter;

import org.xwiki.rendering.block.AbstractBlock;
import org.xwiki.rendering.listener.Listener;

public class BeginEndBlockEventGenerator extends AbstractBlockEventGenerator
{
    public BeginEndBlockEventGenerator(AbstractBlock block)
    {
        super(block);
    }

    @Override
    public void begin(Listener listener)
    {
        this.block.before(listener);
    }

    @Override
    public void end(Listener listener)
    {
        this.block.after(listener);
    }
}
