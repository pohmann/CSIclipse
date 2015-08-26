/**
 * Copyright (c) 2015 Peter J. Ohmann and Benjamin R. Liblit
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package csi.workspace;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Shell;

import csi.Messages;

/**
 * WorkspaceManager manages the opened and annotated files in the workspace associated with the
 * current CSIclipse instance.  It is also responsible for managing the hidden "CSIclipse_extern_links"
 * project, in which we store the read-only copies of analysis files.
 */
public class WorkspaceManager {

	private Shell shell;
	
	/**
	 * The constructor requires shell context to print error messages during workspace operations.
	 * 
	 * @param shell the shell context to use for creating possible error dialogs
	 */
	public WorkspaceManager(Shell shell) {
		this.shell = shell;
	}
	
	/**
	 * Find or create a file in the workspace for CSI annotations.  Clients need not worry
	 * about how this is done.  As a more detailed description: attempts to find the file
	 * by the following order of priority:
	 * (1) Use the copy of fileName in the hidden CSI project for read-only file copies
	 * (2) Treat fileName as a full path, and create+open a read-only copy of the file
	 *     in the CSI hidden project
	 * (3) Search all projects in the current workspace for a file with a matching name.
	 * In all cases, the return value is a (read-only if possible) IFile handle.
	 * If the file cannot be found by any of these methods, null is returned.
	 * 
	 * @param fileName the file to search for
	 * @return the IFile handle for the found file, or null if not found
	 * @throws MissingFileException if the file cannot be found as a full path or in the workspace
	 */
	public @Nullable IFile findFileInWorkspace(String fileName) throws MissingFileException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		
		// First, attempt to treat fileName as a full path, and open the file as a link in
		// the special CSI hidden workspace project
		// (This is the preferred method, even though it's a bit messy, because it doesn't
		// require the file to be in an existing Eclipse project.  Some day, this will facilitate
		// better integration with the Debug perspective.)
		Path filePath = new Path(fileName);
		if(filePath.isValidPath(fileName)){
			// get or create the special hidden CSI project for storing external file links 
			IProject csiProject = root.getProject("CSIclipse_extern_links");
			try{
				if(!csiProject.exists())
					csiProject.create(null);
				if(!csiProject.isOpen())
					csiProject.open(null);
				csiProject.setHidden(true);
			}
			catch(CoreException e){
				showErrorMessage("Unable to create CSIclipse external project");
				e.printStackTrace();
				return(null);
			}
			
			// (try to) create a file in the project that is a link to the full path
			IFile linkFile = csiProject.getFile(filePath.lastSegment());
			if(linkFile.exists())
				return(linkFile);

			try {
				linkFile.createLink(filePath, IResource.NONE, null);
				return(linkFile);
			} catch (CoreException e) {
				// this is fine, and will happen if fileName is indeed not a full path from the filesystem
				// Oh, well.  On to checking all the projects in the workspace...
			}
		}
		
		// If that fails, search the entire workspace (all projects) for a file with the same name.
		// (This is not ideal, but certainly makes testing easy!)
		for (IProject project : root.getProjects()) {
			// make sure all is good with opening the project
			if(!project.exists()){
				showErrorMessage("Plugin returned non-existant project!");
				return(null);
			}
			else if(!project.isOpen()){
				continue;
			}

			String fileToFind = filePath.lastSegment();
			if(fileToFind == null)
				fileToFind = fileName;
			IResource found = recursiveMemberSearch(project, fileToFind);
			if (found != null && found.exists()) {
				if(!(found instanceof IFile)){
					showErrorMessage("Internal error on project search: non-file returned");
					return(null);
				}
				
				// success!
				return((IFile)found);
			}
		}

		// we searched all the projects and never found the file
		throw new MissingFileException("File \"" + fileName + "\" not found!");
	}
	
	/**
	 * Get the shell context for this manager.
	 * 
	 * @return the shell context
	 */
	public Shell getShell(){
		return(this.shell);
	}
	
	private @Nullable IResource recursiveMemberSearch(IContainer container, String searchName){
		IResource[] members;
		try{
			members = container.members();
		}
		catch(CoreException e){
			showErrorMessage("Unexpected fault searching through project");
			e.printStackTrace();
			return(null);
		}
		
		for(IResource member : members){
			switch(member.getType()){
			case IResource.FOLDER:
				IResource recFound = recursiveMemberSearch((IContainer)member, searchName);
				if(recFound != null)
					return(recFound);
				break;
			case IResource.FILE:
				if(member.getName().equals(searchName))
					return(member);
				break;
			default:
				showErrorMessage("Unexpected resource type while searching project");
				break;
			}
		}
		return(null);
	}
	
	private void showErrorMessage(String message){
		Messages.showErrorMessage(shell, message);
	}

}
