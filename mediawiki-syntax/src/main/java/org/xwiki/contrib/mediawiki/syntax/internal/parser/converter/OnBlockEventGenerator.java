package org.xwiki.contrib.mediawiki.syntax.internal.parser.converter;

import org.xwiki.filter.FilterException;
import org.xwiki.rendering.block.AbstractBlock;
import org.xwiki.rendering.listener.Listener;

import info.bliki.wiki.model.IWikiModel;

public class OnBlockEventGenerator extends AbstractBlockEventGenerator
{
    public OnBlockEventGenerator(AbstractBlock block)
    {
        super(block);
    }

    @Override
    public void traverse(Listener listener, IWikiModel model) throws FilterException
    {
        this.block.traverse(listener);
    }
}
