/*
* This file is part of WebLookAndFeel library.
*
* WebLookAndFeel library is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* WebLookAndFeel library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with WebLookAndFeel library.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alee.extended.tree;

import com.alee.laf.GlobalConstants;
import com.alee.utils.compare.Filter;

import java.io.File;

/**
 * Custom filter for file tree nodes.
 *
 * @author Mikle Garin
 * @since 1.4
 */

public class FileTreeNodeFilter implements Filter<FileTreeNode>
{
    /**
     * Used file filter.
     */
    protected Filter<File> filter;

    /**
     * Constructs new FileTreeNodeFilter with default file filter.
     */
    public FileTreeNodeFilter ()
    {
        this ( GlobalConstants.NON_HIDDEN_ONLY_FILTER );
    }

    /**
     * Constructs new FileTreeNodeFilter using the specified file filter.
     *
     * @param filter file filter
     */
    public FileTreeNodeFilter ( Filter<File> filter )
    {
        super ();
        this.filter = filter;
    }

    /**
     * Returns used file filter.
     *
     * @return used file filter
     */
    public Filter<File> getFilter ()
    {
        return filter;
    }

    /**
     * Sets used file filter.
     *
     * @param filter used file filter
     */
    public void setFilter ( Filter<File> filter )
    {
        this.filter = filter;
    }

    /**
     * {@inheritDoc}
     */
    public boolean accept ( FileTreeNode object )
    {
        return filter.accept ( object.getFile () );
    }
}