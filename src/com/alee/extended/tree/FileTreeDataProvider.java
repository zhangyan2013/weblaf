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

import com.alee.utils.CollectionUtils;
import com.alee.utils.FileUtils;
import com.alee.utils.compare.Filter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Asynchronous data provider for WebFileTree.
 *
 * @author Mikle Garin
 * @since 1.4
 */

public class FileTreeDataProvider extends AbstractTreeDataProvider<FileTreeNode>
{
    /**
     * Tree root files.
     */
    private List<File> rootFiles;

    /**
     * Constructs file tree data provider with the specified files as root.
     *
     * @param rootFiles tree root files
     */
    public FileTreeDataProvider ( File... rootFiles )
    {
        super ();
        this.rootFiles = CollectionUtils.copy ( rootFiles );
        this.comparator = new FileTreeNodeComparator ();
        this.filter = WebFileTreeStyle.filter;
    }

    /**
     * Constructs file tree data provider with the specified files as root.
     *
     * @param rootFiles tree root files
     */
    public FileTreeDataProvider ( List<File> rootFiles )
    {
        super ();
        this.rootFiles = rootFiles;
        this.comparator = new FileTreeNodeComparator ();
        this.filter = WebFileTreeStyle.filter;
    }

    /**
     * Constructs file tree data provider with the specified files as root.
     *
     * @param filter    tree nodes filter
     * @param rootFiles tree root files
     */
    public FileTreeDataProvider ( Filter<FileTreeNode> filter, File... rootFiles )
    {
        super ();
        this.rootFiles = CollectionUtils.copy ( rootFiles );
        this.comparator = new FileTreeNodeComparator ();
        this.filter = filter;
    }

    /**
     * Constructs file tree data provider with the specified files as root.
     *
     * @param filter    tree nodes filter
     * @param rootFiles tree root files
     */
    public FileTreeDataProvider ( Filter<FileTreeNode> filter, List<File> rootFiles )
    {
        super ();
        this.rootFiles = rootFiles;
        this.comparator = new FileTreeNodeComparator ();
        this.filter = filter;
    }

    /**
     * {@inheritDoc}
     */
    public FileTreeNode getRoot ()
    {
        return new FileTreeNode ( null );
    }

    /**
     * {@inheritDoc}
     */
    public List<FileTreeNode> getChilds ( FileTreeNode node )
    {
        return node.getFile () == null ? getRootChilds () : getFileChilds ( node );
    }

    /**
     * Returns root child nodes.
     *
     * @return root child nodes
     */
    private List<FileTreeNode> getRootChilds ()
    {
        List<FileTreeNode> childs = new ArrayList<FileTreeNode> ( rootFiles.size () );
        for ( File rootFile : rootFiles )
        {
            childs.add ( new FileTreeNode ( rootFile ) );
        }
        return childs;
    }

    /**
     * Returns child nodes for specified node.
     *
     * @param node parent node
     * @return child nodes
     */
    public List<FileTreeNode> getFileChilds ( FileTreeNode node )
    {
        final File[] childsList = node.getFile ().listFiles ();
        if ( childsList == null || childsList.length == 0 )
        {
            return new ArrayList<FileTreeNode> ( 0 );
        }
        else
        {
            List<FileTreeNode> childs = new ArrayList<FileTreeNode> ( childsList.length );
            for ( File f : childsList )
            {
                childs.add ( new FileTreeNode ( f ) );
            }
            return childs;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isLeaf ( FileTreeNode node )
    {
        return node.getFile () != null && !FileUtils.isDirectory ( node.getFile () );
    }
}