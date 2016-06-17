package org.xwiki.contrib.mediawiki.syntax.internal.parser.converter;

import org.xwiki.rendering.block.AbstractBlock;

import info.bliki.htmlcleaner.BaseToken;
import info.bliki.htmlcleaner.TagNode;

public abstract class AbstractBlockEventGenerator extends AbstractEventGenerator
{
    protected AbstractBlock block;

    public AbstractBlockEventGenerator(AbstractBlock block)
    {
        this.block = block;
    }

    @Override
    public void init(BaseToken token, EventConverter converter)
    {
        super.init(token, converter);

        if (token instanceof TagNode) {
            this.block.setParameters(((TagNode) token).getAttributes());
        }
    }

    @Override
    public AbstractBlockEventGenerator clone() throws CloneNotSupportedException
    {
        AbstractBlockEventGenerator blockEvent = (AbstractBlockEventGenerator) super.clone();

        blockEvent.block = (AbstractBlock) this.block.clone();

        return blockEvent;
    }
}
