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

package csi.markers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.NonNull;

import csi.Messages;
import csi.workspace.MissingFileException;
import csi.workspace.WorkspaceManager;

/**
 * MarkerManager manages markers for a CSIclipse instance.  It is always in the context of a
 * particular WorkspaceManager object.
 */
public class MarkerManager {

	/**
	 * The global CSI annotation types (see also plugin.xml)
	 */
	public static String[] globalAnnotationTypes = {"csi.global.yesMarker", "csi.global.noMarker",
		"csi.global.maybeMarker", "csi.global.otherMarker"};
	/**
	 * The local CSI annotation types (see also plugin.xml)
	 */
	@SuppressWarnings("null") // "@NonNull" below is mistakenly flagged as redundant
	public static final @NonNull String[] localAnnotationTypes = {"csi.yesMarker", "csi.noMarker",
		"csi.maybeMarker", "csi.otherMarker", "csi.path.currentMarker", "csi.path.nextMarker"};
	
	private final WorkspaceManager workspaceManager;
	
	/**
	 * Construct a new MarkerManager object with the specified WorkspaceManager context.
	 * 
	 * @param workspaceManager the WorkspaceManager context for marker cleaning and creation
	 */
	public MarkerManager(WorkspaceManager workspaceManager) {
		this.workspaceManager = workspaceManager;
	}
	
	/**
	 * Delete all markers of the specified types from the specified file.
	 * 
	 * @param file the file from which to clear markers
	 * @param types the types of markers to clear
	 */
	public void clearMarkers(IFile file, @NonNull String[] types){
		for(String type : types){
			clearMarkers(file, type);
		}
	}
	
	/**
	 * Delete all markers of the specified types from the specified file (given as a String path).
	 * 
	 * @param file the file (as a String) from which to clear markers
	 * @param types the types of markers to clear
	 * @throws MissingFileException if the file cannot be found as a full path or in the current workspace
	 */
	public void clearMarkers(String file, @NonNull String[] types) throws MissingFileException {
		IFile theFile = workspaceManager.findFileInWorkspace(file);
		
		// TODO: update this after further refactoring.  findFileInWorkspace() will eventually be
		// non-nullable and return all problems as exceptions
		if(theFile == null){
			// a serious error occurred while searching for the file.
			// An error should already be printed in the callee
			return;
		}
		
		clearMarkers(theFile, types);
	}
	
	/**
	 * Delete all markers of the specified types from all markers in the specified workspace folder.
	 * 
	 * @param container the workspace folder object
	 * @param types the types of markers to clear
	 */
	public void clearMarkers(IContainer container, String[] types){
		IResource[] members;
		try{
			members = container.members();
		}
		catch(CoreException e){
			showErrorMessage("Unexpected fault searching through project");
			e.printStackTrace();
			return;
		}
		
		for(IResource member : members){
			switch(member.getType()){
			case IResource.FOLDER:
				clearMarkers((IContainer)member, types);
				break;
			case IResource.FILE:
				clearMarkers((IFile)member, types);
				break;
			default:
				showErrorMessage("Unexpected resource type while searching project");
				break;
			}
		}
	}
	
	/**
	 * Add markers to the specified lines in the specified file.  The markers will be of the specified
	 * markerType (see plugin.xml for the types we declare), with the specified markerMessage (which shows
	 * up as a tooltip for the marker).
	 * 
	 * @param file the file in which to create markers
	 * @param lines the lines of file on which to create markers
	 * @param markerType the type of marker (see plugin.xml)
	 * @param markerMessage the tooltip text for the marker
	 */
	public void setMarkers(IFile file, Set<Integer> lines, String markerType, String markerMessage){
		try{
			// MAJOR props to: https://www.eclipse.org/forums/index.php?t=msg&th=489989&goto=1092436&#msg_1092436
			// for the design of these markers!  (here and the stuff in plugin.xml)
			for(Integer i : lines){
				IMarker marker = file.createMarker(markerType);
				marker.setAttribute(IMarker.MESSAGE, markerMessage);
				marker.setAttribute(IMarker.LINE_NUMBER, i);
				// TODO: try to combine markers for contiguous lines where possible?
				//marker.setAttribute(IMarker.CHAR_START, i*10);
				//marker.setAttribute(IMarker.CHAR_END, i*10+50);
				//IDocumentProvider p = new TextFileDocumentProvider();
				//p.connect(file);
				//IDocument doc = p.getDocument(file);
			}
		}
		catch(CoreException e){
			showErrorMessage("Internal error: unknown error setting tracing annotations");
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * Delete all markers of the specified type from the specified file.
	 * 
	 * @param file the file from which to clear markers
	 * @param markerType the type of markers to clear
	 */
	public void clearMarkers(IFile file, String markerType){
		try{
			file.deleteMarkers(markerType, true, 1);
		}
		catch(CoreException e){
			showErrorMessage("Internal error: unable to delete marker type (" + markerType + ")");
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * Place appropriate markers/annotations on the file specified based on exeYes/No/Maybe data.
	 * As a side note: users of markers through StackBrowserView should NOT call this function directly, and
	 * instead use doFileMarkers[Local/Global].
	 * 
	 * @param theFile file in which to create markers
	 * @param markerPrefix a "local" or "global" prefix. (See plugin.xml; currently,
	 *                     the local prefix is "csi" and the global prefix is "csi.global".)
	 * @param labelPrefix a prefix for the marker message (e.g., "Local" or "Global")
	 * @param exeYes the set of "yes" lines from theFile
	 * @param exeNo the set of "no" lines from theFile
	 * @param exeMaybe the set of "maybe" lines from theFile
	 */
	public void doFileMarkers(IFile theFile, String markerPrefix, String labelPrefix,
			Set<Integer> exeYes, Set<Integer> exeNo, Set<Integer> exeMaybe){
		// compute the actual markers
		Set<Integer> allThree = new HashSet<>(exeYes);
		allThree.retainAll(exeNo);
		allThree.retainAll(exeMaybe);
		Set<Integer> yesAndMaybe = new HashSet<>(exeYes);
		yesAndMaybe.retainAll(exeMaybe);
		yesAndMaybe.removeAll(allThree);
		Set<Integer> yesAndNo = new HashSet<>(exeYes);
		yesAndNo.retainAll(exeNo);
		yesAndNo.removeAll(allThree);
		Set<Integer> noAndMaybe = new HashSet<>(exeNo);
		noAndMaybe.retainAll(exeMaybe);
		noAndMaybe.removeAll(allThree);
		Set<Integer> yes = new HashSet<>(exeYes);
		yes.removeAll(allThree);
		yes.removeAll(yesAndNo);
		yes.removeAll(yesAndMaybe);
		Set<Integer> no = new HashSet<>(exeNo);
		no.removeAll(allThree);
		no.removeAll(yesAndNo);
		no.removeAll(noAndMaybe);
		Set<Integer> maybe = new HashSet<>(exeMaybe);
		maybe.removeAll(allThree);
		maybe.removeAll(yesAndMaybe);
		maybe.removeAll(noAndMaybe);
		
		setMarkers(theFile, yes, markerPrefix + ".yesMarker", labelPrefix + ": Line completely executed");
		setMarkers(theFile, no, markerPrefix + ".noMarker", labelPrefix + ": Line not executed");
		setMarkers(theFile, maybe, markerPrefix + ".maybeMarker", labelPrefix + ": Line may have been executed");
		setMarkers(theFile, yesAndMaybe, markerPrefix + ".otherMarker", labelPrefix +
				": Line partially executed; remainder of line maybe executed");
		setMarkers(theFile, noAndMaybe, markerPrefix + ".otherMarker", labelPrefix +
				": Line partially unexecuted; remainder of line maybe executed");
		setMarkers(theFile, yesAndNo, markerPrefix + ".otherMarker", labelPrefix +
				": Line partially executed; line partially unexecuted");
		setMarkers(theFile, allThree, markerPrefix + ".otherMarker", labelPrefix +
				": Line partially executed, unexecuted, and maybe executed");
	}
	
	/**
	 * A wrapper to show error dialogs without specifying the shell context.
	 * 
	 * @param message the error message to display
	 */
	private void showErrorMessage(String message){
		Messages.showErrorMessage(this.workspaceManager.getShell(), message);
	}

}
