package org.xwiki.contrib.mediawiki.syntax.internal.parser.converter;

import org.xwiki.filter.FilterException;
import org.xwiki.rendering.listener.Listener;

import info.bliki.htmlcleaner.BaseToken;
import info.bliki.wiki.model.IWikiModel;

public interface EventGenerator extends Cloneable
{
    void init(BaseToken token, EventConverter converter);

    EventGenerator clone() throws CloneNotSupportedException;

    void traverse(Listener listener, IWikiModel model) throws FilterException;
}
