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

import com.alee.extended.drag.FileDropHandler;
import com.alee.utils.CollectionUtils;
import com.alee.utils.FileUtils;
import com.alee.utils.compare.Filter;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This component is a file tree with asynchronous childs loading.
 * It also contains a few additional methods to find, select and edit visible in tree files.
 *
 * @author Mikle Garin
 * @since 1.4
 */

public class WebFileTree extends WebAsyncTree<FileTreeNode>
{
    /**
     * Whether allow files location search by dropping a file onto the tree or not.
     */
    protected boolean filesDropSearchEnabled = WebFileTreeStyle.filesDropSearchEnabled;

    /**
     * File lookup drop handler.
     */
    protected FileDropHandler fileLookupDropHandler = null;

    /**
     * Delayed selection ID operations lock.
     */
    protected final Object delayedSelectionLock = new Object ();

    /**
     * Delayed selection ID to determine wether it is the last one requested or not.
     */
    protected int delayedSelectionId = 0;

    /**
     * Costructs file tree with system hard drives as root.
     */
    public WebFileTree ()
    {
        this ( FileTreeRootType.drives );
    }

    /**
     * Constructs file tree with the specified root type.
     *
     * @param rootType file tree root type
     */
    public WebFileTree ( FileTreeRootType rootType )
    {
        this ( rootType.getRoots () );
    }

    /**
     * Constructs file tree with file under specified path as root.
     *
     * @param rootPath path to root file
     */
    public WebFileTree ( String rootPath )
    {
        this ( new File ( rootPath ) );
    }

    /**
     * Constructs file tree with specified files as root.
     *
     * @param rootFiles root files
     */
    public WebFileTree ( File... rootFiles )
    {
        this ( CollectionUtils.copy ( rootFiles ) );
    }

    /**
     * Constructs file tree with specified files as root.
     *
     * @param rootFiles root files
     */
    public WebFileTree ( List<File> rootFiles )
    {
        super ( new FileTreeDataProvider ( rootFiles ) );

        // Visual settings
        setEditable ( false );
        setRootVisible ( false );
        setCellRenderer ( new WebFileTreeCellRenderer () );
        setCellEditor ( new WebFileTreeCellEditor () );

        // Transfer handler
        setFilesDropSearchEnabled ( WebFileTreeStyle.filesDropSearchEnabled );
    }

    /**
     * {@inheritDoc}
     */
    public FileTreeDataProvider getDataProvider ()
    {
        return ( FileTreeDataProvider ) super.getDataProvider ();
    }

    /**
     * {@inheritDoc}
     */
    public void setModel ( TreeModel newModel )
    {
        // Disable asynchronous loading for the model installation time
        // This made to load initial data without delay using EDT
        // This is some kind of workaround for file chooser to allow it proper file expansion on first load
        boolean async = isAsyncLoading ();
        setAsyncLoading ( false );
        super.setModel ( newModel );
        setAsyncLoading ( async );
    }

    /**
     * Returns file drop handler that locates file in the tree when dropped.
     *
     * @return file lookup drop handler
     */
    protected FileDropHandler getFileLookupDropHandler ()
    {
        if ( fileLookupDropHandler == null )
        {
            fileLookupDropHandler = new FileDropHandler ()
            {
                protected boolean isDropEnabled ()
                {
                    return filesDropSearchEnabled;
                }

                protected boolean filesImported ( List<File> files )
                {
                    // Selecting dragged files in tree
                    if ( files.size () > 0 )
                    {
                        setSelectedFile ( files.get ( 0 ), true );
                        return true;
                    }
                    return false;
                }
            };
        }
        return fileLookupDropHandler;
    }

    /**
     * Returns whether files search by dropping system files on the tree enabled or not.
     *
     * @return true if files search by dropping system files on the tree enabled, false otherwise
     */
    public boolean isFilesDropSearchEnabled ()
    {
        return filesDropSearchEnabled;
    }

    /**
     * Sets whether files search by dropping system files on the tree enabled or not
     *
     * @param filesDropSearchEnabled whether files search by dropping system files on the tree enabled or not
     */
    public void setFilesDropSearchEnabled ( boolean filesDropSearchEnabled )
    {
        this.filesDropSearchEnabled = filesDropSearchEnabled;

        final FileDropHandler lookupDropHandler = getFileLookupDropHandler ();
        if ( filesDropSearchEnabled )
        {
            setTransferHandler ( lookupDropHandler );
        }
        else if ( getTransferHandler () == lookupDropHandler )
        {
            setTransferHandler ( null );
        }
    }

    /**
     * Returns tree files filter.
     *
     * @return files filter
     */
    public Filter<File> getFileFilter ()
    {
        final Filter<FileTreeNode> filter = getFilter ();
        return filter instanceof FileTreeNodeFilter ? ( ( FileTreeNodeFilter ) filter ).getFilter () : null;
    }

    /**
     * Sets tree files filter.
     *
     * @param filter new files filter
     */
    public void setFileFilter ( Filter<File> filter )
    {
        // We don't update old FileTreeNodeFilter with new file filter as this will force us to manually update tree
        // So we simply create and set new FileTreeNodeFilter so that model can handle filter change and upate the tree
        setFilter ( filter != null ? new FileTreeNodeFilter ( filter ) : null );
    }

    /**
     * Changes displayed tree root name.
     *
     * @param rootName new root name
     */
    public void setRootName ( String rootName )
    {
        final FileTreeNode rootNode = getRootNode ();
        rootNode.setName ( rootName );
        getAsyncModel ().updateNode ( rootNode );
    }

    /**
     * Finds and selects specified file in tree.
     * This method might not have any effect in case the specified field doesn't exist under the file tree root.
     *
     * @param file file to select
     */
    public void setSelectedFile ( File file )
    {
        setSelectedFile ( file, false );
    }

    /**
     * Finds and selects specified file in tree.
     * This method might not have any effect in case the specified field doesn't exist under the file tree root.
     *
     * @param file   file to select
     * @param expand whether to expand selected file or not
     */
    public void setSelectedFile ( File file, final boolean expand )
    {
        expandToFile ( file, true, expand );
    }

    /**
     * Expands tree structure to the specified file and expands that file node.
     * This method might not have any effect in case the specified field doesn't exist under the file tree root.
     *
     * @param file file to expand
     */
    public void expandFile ( File file )
    {
        expandToFile ( file, false, true );
    }

    /**
     * Expands tree structure to the specified file.
     * This method might not have any effect in case the specified field doesn't exist under the file tree root.
     *
     * @param file file to expand tree sctructure to
     */
    public void expandToFile ( File file )
    {
        expandToFile ( file, false, false );
    }

    /**
     * Expands tree structure to the specified file.
     * This method might not have any effect in case the specified field doesn't exist under the file tree root.
     *
     * @param file   file to expand tree sctructure to
     * @param select whether to select file or not
     */
    public void expandToFile ( File file, final boolean select )
    {
        expandToFile ( file, select, false );
    }

    /**
     * Expands tree structure to the specified file.
     * This method might not have any effect in case the specified field doesn't exist under the file tree root.
     *
     * @param file   file to expand tree sctructure to
     * @param select whether to select file or not
     * @param expand whether to expand file or not
     */
    public void expandToFile ( File file, final boolean select, final boolean expand )
    {
        expandToFile ( file, select, expand, null );
    }

    /**
     * Expands tree structure to the specified file.
     * This method might not have any effect in case the specified field doesn't exist under the file tree root.
     *
     * @param file   file to expand tree sctructure to
     * @param select whether to select file or not
     * @param expand whether to expand file or not
     */
    public void expandToFile ( File file, final boolean select, final boolean expand, final Runnable finalAction )
    {
        if ( file != null )
        {
            final int selectionId;
            synchronized ( delayedSelectionLock )
            {
                selectionId = delayedSelectionId;
                delayedSelectionId++;
            }

            // Expanding whole path
            final AsyncTreeModel model = getAsyncModel ();
            final FileTreeNode node = getClosestNode ( file );
            FileTreeNode parent = null;
            if ( node != null )
            {
                if ( FileUtils.equals ( node.getFile (), file ) )
                {
                    if ( select && selectionId == delayedSelectionId )
                    {
                        performFileSelection ( node, expand );
                    }
                    else
                    {
                        if ( expand )
                        {
                            expandNode ( node );
                        }
                        scrollToNode ( node );
                    }
                    if ( finalAction != null )
                    {
                        finalAction.run ();
                    }
                }
                else
                {
                    final List<File> path = FileUtils.getFilePath ( file );

                    // Removing already opened nodes
                    int index = path.indexOf ( node.getFile () );
                    for ( int i = index; i >= 0; i-- )
                    {
                        path.remove ( i );
                    }

                    // Opening path to file
                    addAsyncTreeListener ( new AsyncTreeAdapter<FileTreeNode> ()
                    {
                        private FileTreeNode lastNode = node;

                        public void childsLoadCompleted ( FileTreeNode parent, List<FileTreeNode> childs )
                        {
                            if ( parent == lastNode )
                            {
                                // Searching for path part in childs
                                boolean found = false;
                                for ( FileTreeNode child : childs )
                                {
                                    if ( child.getFile ().equals ( path.get ( 0 ) ) )
                                    {
                                        found = true;
                                        if ( path.size () == 1 )
                                        {
                                            removeAsyncTreeListener ( this );
                                            if ( select && selectionId == delayedSelectionId )
                                            {
                                                performFileSelection ( child, expand );
                                            }
                                            else
                                            {
                                                if ( expand )
                                                {
                                                    expandNode ( child );
                                                }
                                                scrollToNode ( child );
                                            }
                                            if ( finalAction != null )
                                            {
                                                finalAction.run ();
                                            }
                                            break;
                                        }
                                        else
                                        {
                                            lastNode = child;
                                            path.remove ( 0 );
                                            reloadNode ( child );
                                            break;
                                        }
                                    }
                                }
                                if ( !found )
                                {
                                    removeAsyncTreeListener ( this );
                                    if ( select && selectionId == delayedSelectionId )
                                    {
                                        performFileSelection ( parent, false );
                                    }
                                    else
                                    {
                                        scrollToNode ( parent );
                                    }
                                    if ( finalAction != null )
                                    {
                                        finalAction.run ();
                                    }
                                }
                            }
                        }
                    } );

                    // Reload node to make sure we see up-to-date files list
                    reloadNode ( node );
                }
            }
        }
        else if ( select )
        {
            clearSelection ();
        }
    }

    /**
     * Performs the actual file selection.
     *
     * @param node   node to select
     * @param expand should expand the node
     */
    protected void performFileSelection ( FileTreeNode node, boolean expand )
    {
        try
        {
            // Selecting node
            TreePath path = node.getTreePath ();
            setSelectionPath ( path );

            // Expanding if requested
            if ( expand )
            {
                // Expanding
                if ( !isAutoExpandSelectedNode () )
                {
                    expandPath ( path );
                }

                // todo Use a better view rect?
                // Scrolling view to node childs
                Rectangle pathBounds = getPathBounds ( path );
                if ( pathBounds != null )
                {
                    Rectangle vr = getVisibleRect ();
                    Rectangle rect = new Rectangle ( vr.x, pathBounds.y, vr.width, vr.height );
                    scrollRectToVisible ( rect );
                }
            }
            else
            {
                // todo Use a better view rect?
                // Properly scrolling view
                scrollPathToVisible ( path );
            }
        }
        catch ( Throwable e )
        {
            e.printStackTrace ();
        }
    }

    /**
     * Returns selected file.
     *
     * @return selected file
     */
    public File getSelectedFile ()
    {
        final FileTreeNode selectedNode = getSelectedNode ();
        return selectedNode != null ? selectedNode.getFile () : null;
    }

    /**
     * Returns selected files.
     *
     * @return selected files
     */
    public List<File> getSelectedFiles ()
    {
        List<File> selectedFiles = new ArrayList<File> ();
        if ( getSelectionPaths () != null )
        {
            for ( TreePath path : getSelectionPaths () )
            {
                selectedFiles.add ( getNodeForPath ( path ).getFile () );
            }
        }
        return selectedFiles;
    }

    /**
     * Returns selected nodes.
     *
     * @return selected nodes
     */
    public List<FileTreeNode> getSelectedNodes ()
    {
        List<FileTreeNode> selectedNodes = new ArrayList<FileTreeNode> ();
        if ( getSelectionPaths () != null )
        {
            for ( TreePath path : getSelectionPaths () )
            {
                selectedNodes.add ( getNodeForPath ( path ) );
            }
        }
        return selectedNodes;
    }

    /**
     * Adds new file into tree structure.
     * This method will have effect only if node with parent file exists and it has already loaded childs.
     *
     * @param parent parent file
     * @param file   added file
     * @return true if tree structure was changed by the operation, false otherwise
     */
    public boolean addFile ( File parent, File file )
    {
        return addFiles ( parent, file );
    }

    /**
     * Adds new file into tree structure.
     * This method will have effect only if node with parent file exists and it has already loaded childs.
     *
     * @param parentNode parent node
     * @param file       added file
     * @return true if tree structure was changed by the operation, false otherwise
     */
    public boolean addFile ( FileTreeNode parentNode, File file )
    {
        return addFiles ( parentNode, file );
    }

    /**
     * Adds new files into tree structure.
     * This method will have effect only if node with parent file exists and it has already loaded childs.
     *
     * @param parent parent file
     * @param files  added files
     * @return true if tree structure was changed by the operation, false otherwise
     */
    public boolean addFiles ( File parent, File... files )
    {
        return addFiles ( parent, CollectionUtils.copy ( files ) );
    }

    /**
     * Adds new files into tree structure.
     * This method will have effect only if node with parent file exists and it has already loaded childs.
     *
     * @param parentNode parent node
     * @param files      added files
     * @return true if tree structure was changed by the operation, false otherwise
     */
    public boolean addFiles ( FileTreeNode parentNode, File... files )
    {
        return addFiles ( parentNode, CollectionUtils.copy ( files ) );
    }

    /**
     * Adds new files into tree structure.
     * This method will have effect only if node with parent file exists and it has already loaded childs.
     *
     * @param parent parent file
     * @param files  added files
     * @return true if tree structure was changed by the operation, false otherwise
     */
    public boolean addFiles ( File parent, List<File> files )
    {
        // Checking that node for the file exists
        FileTreeNode parentNode = getNode ( parent );
        return parentNode != null && addFiles ( parentNode, files );
    }

    /**
     * Adds new files into tree structure.
     * This method will have effect only if node with parent file exists and it has already loaded childs.
     *
     * @param parentNode parent node
     * @param files      added files
     * @return true if tree structure was changed by the operation, false otherwise
     */
    public boolean addFiles ( FileTreeNode parentNode, List<File> files )
    {
        if ( hasBeenExpanded ( getPathForNode ( parentNode ) ) )
        {
            List<FileTreeNode> childNodes = new ArrayList<FileTreeNode> ( files.size () );
            for ( File file : files )
            {
                childNodes.add ( new FileTreeNode ( file ) );
            }
            addChildNodes ( parentNode, childNodes );
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Removes file from tree structure.
     * This method will have effect only if node with the specified file exists.
     *
     * @param file removed file
     * @return true if tree structure was changed by the operation, false otherwise
     */
    public boolean removeFile ( File file )
    {
        return removeNode ( getNode ( file ) );
    }

    /**
     * Removes files from tree structure.
     * This method only works if nodes with the specified files exist.
     *
     * @param files removed files
     * @return true if tree structure was changed by the operation, false otherwise
     */
    public boolean removeFiles ( File... files )
    {
        // todo Optimize (multi-node delete method in model)
        boolean changed = false;
        for ( File file : files )
        {
            changed |= removeFile ( file );
        }
        return changed;
    }

    /**
     * Removes files from tree structure.
     * This method only works if nodes with the specified files exist.
     *
     * @param files removed files
     * @return true if tree structure was changed by the operation, false otherwise
     */
    public boolean removeFiles ( List<File> files )
    {
        boolean changed = false;
        for ( File file : files )
        {
            changed |= removeFile ( file );
        }
        return changed;
    }

    /**
     * Starts editing cell with the specified file.
     *
     * @param file file to edit
     */
    public void startEditingFile ( final File file )
    {
        final FileTreeNode node = getNode ( file );
        if ( node != null )
        {
            startEditingNode ( node );
        }
        else
        {
            expandToFile ( file, false, false, new Runnable ()
            {
                public void run ()
                {
                    final FileTreeNode node = getNode ( file );
                    if ( node != null )
                    {
                        startEditingNode ( node );
                    }
                }
            } );
        }
    }

    /**
     * Returns files collected from loaded node childs.
     * This method will not force childs load.
     *
     * @param node node
     * @return files from node childs
     */
    public List<File> getFileChilds ( FileTreeNode node )
    {
        List<File> files = new ArrayList<File> ();
        for ( int i = 0; i < node.getChildCount (); i++ )
        {
            files.add ( node.getChildAt ( i ).getFile () );
        }
        return files;
    }

    /**
     * Returns node for the specified file if it is already loaded.
     * Returns null if node for this file was not loaded yet or if the specified file doesn't exist under the file tree root.
     *
     * @param file file to search for
     * @return file node
     */
    public FileTreeNode getNode ( File file )
    {
        FileTreeNode node = getClosestNode ( file );
        return node != null && FileUtils.equals ( file, node.getFile () ) ? node : null;
    }

    /**
     * Returns loaded and closest to file node.
     * Might return null in case file path parts were not found in tree.
     *
     * @param file file to look for
     * @return closest to file node
     */
    public FileTreeNode getClosestNode ( File file )
    {
        return getClosestNode ( ( FileTreeNode ) getModel ().getRoot (), FileUtils.getFilePath ( file ) );
    }

    /**
     * Returns loaded and closest to file node.
     * Might return null in case file path parts were not found in tree.
     *
     * @param node node to look into
     * @param path path of the file to look for
     * @return closest to file node
     */
    protected FileTreeNode getClosestNode ( FileTreeNode node, List<File> path )
    {
        // Check if this file is part of the file path
        final File file = node.getFile ();
        if ( file != null )
        {
            if ( path.contains ( file ) )
            {
                // Filter out upper levels of path
                int index = path.indexOf ( file );
                for ( int i = index; i >= 0; i-- )
                {
                    path.remove ( i );
                }

                // Find the deepest laoded path node
                return getDeepestPathNode ( node, path );
            }
        }

        // Check child nodes
        for ( int i = 0; i < node.getChildCount (); i++ )
        {
            FileTreeNode found = getClosestNode ( node.getChildAt ( i ), path );
            if ( found != null )
            {
                return found;
            }
        }

        // No nodes found
        return null;
    }

    /**
     * Returns deepest path node available.
     *
     * @param pathNode current path node
     * @param path     files path
     * @return deepest path node available
     */
    protected FileTreeNode getDeepestPathNode ( FileTreeNode pathNode, List<File> path )
    {
        if ( path.size () > 0 )
        {
            for ( int i = 0; i < pathNode.getChildCount (); i++ )
            {
                FileTreeNode child = pathNode.getChildAt ( i );
                if ( child.getFile ().equals ( path.get ( 0 ) ) )
                {
                    path.remove ( 0 );
                    return getDeepestPathNode ( child, path );
                }
            }
        }
        return pathNode;
    }

    /**
     * Reloads child files for the specified folder.
     * Unlike asynchronous methods this one works in EDT and forces to wait until the nodes load finishes.
     *
     * @param folder folder to reload childs for
     */
    public void reloadChildsSync ( File folder )
    {
        reloadChildsSync ( folder, false );
    }

    /**
     * Reloads child files for the specified folder and selects folder node if requested.
     * Unlike asynchronous methods this one works in EDT and forces to wait until the nodes load finishes.
     *
     * @param folder folder to reload childs for
     * @param select whether select folder node or not
     */
    public void reloadChildsSync ( File folder, boolean select )
    {
        FileTreeNode node = getNode ( folder );
        if ( node != null )
        {
            reloadNodeSync ( node );
        }
    }

    /**
     * Reloads child files for the specified folder.
     *
     * @param folder folder to reload childs for
     */
    public void reloadChilds ( File folder )
    {
        reloadChilds ( folder, false );
    }

    /**
     * Reloads child files for the specified folder and selects folder node if requested.
     *
     * @param folder folder to reload childs for
     * @param select whether select folder node or not
     */
    public void reloadChilds ( File folder, boolean select )
    {
        FileTreeNode node = getNode ( folder );
        if ( node != null )
        {
            reloadNode ( node );
        }
    }
}