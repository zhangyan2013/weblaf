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

import java.awt.*;
import java.awt.image.ImageObserver;

/**
 * Custom image observer class for animated loader icons.
 *
 * @author Mikle Garin
 * @since 1.4
 */

public class NodeImageObserver implements ImageObserver
{
    /**
     * Asynchronous tree.
     */
    protected WebAsyncTree tree;

    /**
     * Observed node.
     */
    protected AsyncUniqueNode node;

    /**
     * Constructs default node observer.
     *
     * @param tree asynchronous tree
     * @param node observed node
     */
    public NodeImageObserver ( WebAsyncTree tree, AsyncUniqueNode node )
    {
        this.tree = tree;
        this.node = node;
    }

    /**
     * Updates loader icon view in tree cell.
     *
     * @param img   image being observed
     * @param flags bitwise inclusive OR of flags
     * @param x     x coordinate
     * @param y     y coordinate
     * @param w     width
     * @param h     height
     * @return false if the infoflags indicate that the image is completely loaded, true otherwise
     */
    public boolean imageUpdate ( Image img, int flags, int x, int y, int w, int h )
    {
        if ( node.isBusy () && ( flags & ( FRAMEBITS | ALLBITS ) ) != 0 )
        {
            final Rectangle rect = tree.getPathBounds ( node.getTreePath () );
            if ( rect != null )
            {
                tree.repaint ( rect );
            }
        }
        return ( flags & ( ALLBITS | ABORT ) ) == 0;
    }
}