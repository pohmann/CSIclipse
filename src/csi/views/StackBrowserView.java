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

package csi.views;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.commands.ActionHandler;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import csi.Activator;
import csi.Messages;
import csi.analysis.AnalysisData;
import csi.analysis.AnalysisReader;
import csi.markers.MarkerManager;
import csi.models.FileModel;
import csi.models.FrameModel;
import csi.models.LineModel;
import csi.models.LocalModel;
import csi.workspace.MissingFileException;
import csi.workspace.WorkspaceManager;
import providers.GlobalLabelProvider;
import providers.LocalLabelProvider;
import providers.LocalContentProvider;

/**
 * StackBrowserView is the primary "CSI Analysis Data" view for CSIclipse.
 */
@SuppressWarnings("deprecation")
public class StackBrowserView extends ViewPart {

	private class ViewComponents {
		private class RefreshGlobalAction implements Runnable {
			@Override
			public void run() {
				if(!StackBrowserView.this.loadedData){
					showMessage("CSI data not loaded.  Cannot refresh annotations");
					return;
				}
				
				for(FileModel fData : utils.NotNull.check(StackBrowserView.this.exeData).getGlobalData()){
					IFile theFile;
					try {
						theFile = utils.NotNull.check(workspaceManager).findFileInWorkspace(fData.getFile());
					}
					catch (MissingFileException e) {
						showErrorMessage(utils.NotNull.check(e.getMessage()));
						continue;
					}
					
					// TODO: update this after further refactoring.  findFileInWorkspace() will eventually be
					// non-nullable and return all problems as exceptions
					if(theFile == null){
						// a serious error occurred while searching for the file.
						// An error should already be printed in the callee
						continue;
					}
					
					doFileMarkersGlobal(theFile, fData.getExeYes(), fData.getExeNo(), fData.getExeMaybe());
				}
				
				globalAction.setChecked(true);
			}
		}

		private class ClearGlobalAction implements Runnable {
			@Override
			public void run() {
				clearWorkspaceAnnotations(MarkerManager.globalAnnotationTypes);
				globalAction.setChecked(false);
			}
		}

		private class RefreshLocalAction implements Runnable {
			@Override
			public void run() {
				// refresh local annotations based on previously-selected frame
				FrameModel prevFrame = StackBrowserView.this.currentFrame;
				if(prevFrame != null){
					int prevLine = prevFrame.getChildren()[StackBrowserView.this.currentPathEntry].getLine();
					if(prevLine >= 0){
						IFile openedFile = StackBrowserView.this.openFileAndGotoLine(prevFrame.getFile(), prevLine);
						if(openedFile != null)
							StackBrowserView.this.doFileMarkersLocal(openedFile,
									prevFrame.getExeYes(), prevFrame.getExeNo(), prevFrame.getExeMaybe());
					}
				}
				
				localAction.setChecked(true);
				
				// refresh current path trace entry annotations based on previously-selected entry
				revealCurrentPathEntry();
				updateFwdBackButtons(StackBrowserView.this);
			}
		}

		private class ClearLocalAction implements Runnable {
			@Override
			public void run() {
				clearWorkspaceAnnotations(MarkerManager.localAnnotationTypes);
				localAction.setChecked(false);
				updateFwdBackButtons(StackBrowserView.this);
			}
		}

		// the top-level container for trace data and the viewers for the CSI data view
		private final Composite traceContainer;
		private final TreeViewer stackTreeViewer;
		private final TableViewer globalTableViewer;

		// actions for showing CSI copyright, loading trace data, and double-clicking (i.e. going to) a particular trace element
		private final Action infoAction;
		private final Action loadDataAction;
		private final Action localClickAction;
		private final Action globalClickAction;

		// showing or hiding global or local trace data
		private final Action globalAction;
		private final Action localAction;

		// keep status of current frame for stepping through traced path
		private final Action forwardAction;
		private final Action backwardAction;
		private final IHandlerActivation forwardActivation;
		private final IHandlerActivation backwardActivation;

		private ViewComponents(StackBrowserView stackBrowserView, @Nullable Composite parent) {
			Composite top = new Composite(parent, SWT.NONE);
			top.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING, GridData.VERTICAL_ALIGN_BEGINNING, true, true));
			GridLayout topLayout = new GridLayout();
			topLayout.marginHeight = 0;
			topLayout.marginWidth = 0;
			topLayout.numColumns = 1;
			topLayout.verticalSpacing = 5;
			top.setLayout(topLayout);
			
			// note that this whole control is invisible until we load trace data
			traceContainer = top;
			top.setVisible(false);
			
			Group globalGroup = new Group(top, SWT.NONE);
			globalGroup.setText("Global Data");
			globalGroup.setLayout(new GridLayout());
			GridData globalGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
			globalGridData.verticalSpan = 1;
			globalGroup.setLayoutData(globalGridData);
			
			final TableViewer tableViewer = new TableViewer(globalGroup, SWT.V_SCROLL | SWT.FULL_SELECTION);
			globalTableViewer = tableViewer;
			tableViewer.setContentProvider(new ArrayContentProvider());
			tableViewer.setLabelProvider(new GlobalLabelProvider());
			tableViewer.setInput(null);
			tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			tableViewer.getTable().setLinesVisible(true);
			tableViewer.getTable().setHeaderVisible(true);
			TableColumn column = new TableColumn(tableViewer.getTable(), SWT.CENTER);
			column.setText("");
			column.setToolTipText("File execution status");
			column = new TableColumn(tableViewer.getTable(), SWT.LEFT);
			column.setText("File");
			column.setToolTipText("File name");
			column = new TableColumn(tableViewer.getTable(), SWT.RIGHT);
			column.setText("Yes");
			column.setToolTipText("Number of lines executed");
			column = new TableColumn(tableViewer.getTable(), SWT.RIGHT);
			column.setText("No");
			column.setToolTipText("Number of lines unexecuted");
			column = new TableColumn(tableViewer.getTable(), SWT.RIGHT);
			column.setText("Maybe");
			column.setToolTipText("Number of lines maybe executed");
			
			Group stackGroup = new Group(top, SWT.NONE);
			stackGroup.setText("Stack Data");
			stackGroup.setLayout(new GridLayout());
			GridData stackGridData = new GridData(GridData.FILL, GridData.FILL, true, true);
			stackGridData.verticalSpan = 4;
			stackGroup.setLayoutData(stackGridData);
			
			stackTreeViewer = new TreeViewer(stackGroup, SWT.NONE);
			final Shell shell = getShell();
			stackTreeViewer.setContentProvider(new LocalContentProvider(shell));
			stackTreeViewer.setLabelProvider(new LocalLabelProvider());
			stackTreeViewer.setInput(null);
			stackTreeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
			// get the control from the viewer we just set up
			Control myControl = stackTreeViewer.getControl();
			
			// Create the help context id for the viewer's control
			PlatformUI.getWorkbench().getHelpSystem().setHelp(myControl, "HighlightTest.treeViewer");

			// make actions
			loadDataAction = new Action() {
				@Override
				public void run() {
					FileDialog d = new FileDialog(shell);
					d.setText("CSI Analysis File Selection");
					d.setFilterPath(null);
					String[] filterExt = { "*.csi", "*.*" };
					d.setFilterExtensions(filterExt);
	
					String filePath = d.open();
					if (filePath == null)
						return;
	
					boolean previouslyLoaded = stackBrowserView.loadedData;
	
					// load execution data from the file
					AnalysisData data = stackBrowserView.readAnalysisDataFromFile(filePath);
					if(data == null)
						return;
					
					// assign loaded data to the various viewers / fields
					stackTreeViewer.setInput(data.getLocalData());
					globalTableViewer.setInput(data.getGlobalData());
					stackBrowserView.exeData = data;
					stackBrowserView.loadedData = true;
	
					// massage the global view (color and resize columns)
					Table globalTable = tableViewer.getTable();
					Display tableDisplay = tableViewer.getControl().getDisplay();
					for (TableItem rowItem : globalTable.getItems()) {
						rowItem.setForeground(2, tableDisplay.getSystemColor(SWT.COLOR_GREEN));
						rowItem.setForeground(3, tableDisplay.getSystemColor(SWT.COLOR_RED));
						rowItem.setForeground(4, tableDisplay.getSystemColor(SWT.COLOR_DARK_YELLOW));
					}
					//globalTable.pack();
					for (int i = 0; i < globalTable.getColumnCount(); ++i)
						globalTable.getColumn(i).pack();
					//globalTable.pack();
					
					// utterly silly way to get the table (and surrounding composite) to resize and
					// layout nicely...but it seems to work
					traceContainer.layout(true, true);
					Point viewSize = traceContainer.getSize();
					viewSize.x += 1;
					traceContainer.setSize(viewSize);
					traceContainer.layout(true, true);
					viewSize.x -= 1;
					traceContainer.setSize(viewSize);
					traceContainer.layout(true, true);
	
					// make sure to clear old markers whenever new data is loaded
					// (and toggle the global button appropriately--start by not showing it, unless requested)
					stackBrowserView.clearGlobalAndLocalAnnotations();
					globalAction.setChecked(false);
	
					stackBrowserView.clearFrame();
					if (stackBrowserView.loadedData) {
						traceContainer.setVisible(true);
						if (!previouslyLoaded)
							updateActionBarsForLoadedData(stackBrowserView);
					}
				}
			};
			loadDataAction.setText("Load Crash Data");
			loadDataAction.setToolTipText("Load CSI Crash Data");
			loadDataAction.setImageDescriptor(
					PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER));
		
			localAction = new Action() {
				@Override
				public void run() {
					if (localAction.isChecked()) {
						// if the button is turning off, consider refreshing local annotations based on previous selection
						// right now, this function does almost nothing...
						BusyIndicator.showWhile(null, new RefreshLocalAction());
					} else {
						// otherwise, clear local annotations
						BusyIndicator.showWhile(null, new ClearLocalAction());
					}
				}
			};
			localAction.setText("Display local data");
			localAction.setToolTipText("Display local data");
			localAction.setImageDescriptor(Activator.getImageDescriptor("icons/local_small.png"));
			localAction.setChecked(true);

			globalAction = new Action() {
				@Override
				public void run() {
					if (globalAction.isChecked()) {
						// if we are "activating" the button, turn annotations "on"
						BusyIndicator.showWhile(null, new RefreshGlobalAction());
					} else {
						// otherwise, clear global annotations
						BusyIndicator.showWhile(null, new ClearGlobalAction());
					}
				}
			};
			globalAction.setText("Display global data");
			globalAction.setToolTipText("Display global data");
			globalAction.setImageDescriptor(Activator.getImageDescriptor("icons/global_small.png"));
			globalAction.setChecked(false);

			// NOTE: steps go in the opposite direction one might expect
			// (because the line number array is from crash->entry)
			backwardAction = new Action() {
				@Override
				public void run() {
					stackBrowserView.pathStep(1);
				}
			};
			backwardAction.setText("Step backward in path trace");
			backwardAction.setToolTipText("Step backward in path trace");
			backwardAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
					.getImageDescriptor(ISharedImages.IMG_TOOL_BACK_DISABLED));
			backwardAction.setActionDefinitionId("csi.trace.next");
			backwardAction.setEnabled(false);

			forwardAction = new Action() {
				@Override
				public void run() {
					stackBrowserView.pathStep(-1);
				}
			};
			forwardAction.setText("Step forward in path trace");
			forwardAction.setToolTipText("Step forward in path trace");
			forwardAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
					.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD_DISABLED));
			forwardAction.setActionDefinitionId("csi.trace.next");
			forwardAction.setEnabled(false);

			// fix up shortcut keys for "step back/forward in trace" actions
			// props to: http://udig.refractions.net/files/docs/latest/1.3.2-SNAPSHOT/developer/keyboard_shortcut_example.html
			// for helping me figure out how to do this!
			IWorkbenchWindow window = stackBrowserView.getViewSite().getWorkbenchWindow();
			IHandlerService handlerService = window.getService(IHandlerService.class);
			forwardActivation = utils.NotNull.check(
					handlerService.activateHandler("csi.trace.next", new ActionHandler(forwardAction)));
			backwardActivation = utils.NotNull.check(
					handlerService.activateHandler("csi.trace.prev", new ActionHandler(backwardAction)));
			
			localClickAction = new Action() {
				@Override
				public void run() {
					ISelection selection = stackTreeViewer.getSelection();
					//ISelection selection = viewer.getSelection();
					Object selectedObj = ((IStructuredSelection)selection).getFirstElement();
					if(selectedObj == null || !(selectedObj instanceof LocalModel)){
						stackBrowserView.showErrorMessage("Internal error.  Unexpected double-click event type for local click");
						return;
					}
					
					if(selectedObj instanceof FrameModel){
						FrameModel thisObj = (FrameModel)selectedObj;
						String fileName = Paths.get(thisObj.getFile()).getFileName().toString();
						assert fileName != null;
						int line = 0;
						if(thisObj.hasChildren())
							line = (thisObj.getChildren()[0]).getLine();
						
						IFile openedFile = stackBrowserView.openFileAndGotoLine(fileName, line);
						if(openedFile != null && localAction.isChecked())
							stackBrowserView.doFileMarkersLocal(openedFile, thisObj.getExeYes(), thisObj.getExeNo(), thisObj.getExeMaybe());
						stackBrowserView.updateFrameAndPathEntry(thisObj, 0);
					}
					else if(selectedObj instanceof LineModel){
						LineModel thisObj = (LineModel)selectedObj;
						FrameModel parentObj = thisObj.getParent();
						assert parentObj != null;
						String fileName = Paths.get(parentObj.getFile()).getFileName().toString();
						assert fileName != null;
						int line = thisObj.getLine();
		
						IFile openedFile = stackBrowserView.openFileAndGotoLine(fileName, line);
						if(openedFile != null){
							if(parentObj != stackBrowserView.currentFrame && localAction.isChecked())
								stackBrowserView.doFileMarkersLocal(openedFile,
										parentObj.getExeYes(), parentObj.getExeNo(), parentObj.getExeMaybe());
							
							int entryInParent = Arrays.asList(parentObj.getChildren()).indexOf(thisObj);
							if(entryInParent < 0){
								stackBrowserView.showErrorMessage("Internal error determining path entry location");
								stackBrowserView.updateFrameAndPathEntry(null, -1);
							}
							else{
								stackBrowserView.updateFrameAndPathEntry(parentObj, entryInParent);
							}
						}
					}
					
					// if showing local annotations, set the previously-selected path trace entry
					if(localAction.isChecked())
						revealCurrentPathEntry();
				}
			};
			
			globalClickAction = new Action() {
				@Override
				public void run() {
					ISelection selection = globalTableViewer.getSelection();
					Object selectedObj = ((IStructuredSelection)selection).getFirstElement();
					if(selectedObj == null || !(selectedObj instanceof FileModel)){
						stackBrowserView.showErrorMessage("Internal error.  Unexpected double-click event type for global click");
						return;
					}
					
					FileModel thisObj = (FileModel)selectedObj;
					stackBrowserView.clearFrame();
					IFile openedFile = stackBrowserView.openFileAndGotoLine(thisObj.getFile(), 1);
					if(openedFile != null) {
						utils.NotNull.check(stackBrowserView.markerManager).clearMarkers(openedFile, MarkerManager.localAnnotationTypes);
					}
				}
			};

			infoAction = new Action(){
				@Override
				public void run(){
					showMessage("CSI Analysis Integration / Data Explorer" + System.lineSeparator() +
							"Copyright \u00a9 Peter Ohmann and Ben Liblit" + System.lineSeparator() +
							System.lineSeparator() +
							"For more information, see the website:" + System.lineSeparator() +
							"https://github.com/liblit/csi-cc");
				}
			};
			infoAction.setText("Information");
			infoAction.setToolTipText("CSI Analysis Information");
			infoAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
					.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

			hookContextMenu(stackBrowserView);
			hookDoubleClickAction();
			contributeToActionBars(stackBrowserView);
			
			// set up the workspace/marker manager, and clear all local and global data in starting workspace
			stackBrowserView.workspaceManager = new WorkspaceManager(shell);
			stackBrowserView.markerManager = new MarkerManager(stackBrowserView.workspaceManager);
			stackBrowserView.clearGlobalAndLocalAnnotations();
		}

		private void addLocalToolBar(IToolBarManager manager, ContributionItem item){
			manager.add(item);
		}

		private void addLocalToolBar(IToolBarManager manager, Action action){
			manager.add(action);
		}

		private void contributeToActionBars(StackBrowserView stackBrowserView) {
			IActionBars bars = stackBrowserView.getViewSite().getActionBars();
			fillLocalPullDown(utils.NotNull.check(bars.getMenuManager()));
			fillLocalToolBar(utils.NotNull.check(bars.getToolBarManager()));
		}

		private void fillLocalPullDown(IMenuManager manager) {
			manager.add(new Separator());
			manager.add(infoAction);
		}

		private void fillLocalToolBar(IToolBarManager manager) {
			addLocalToolBar(manager, loadDataAction);
		}

		private Shell getShell() {
			return utils.NotNull.check(traceContainer.getShell());
		}

		private void hookContextMenu(StackBrowserView stackBrowserView) {
			MenuManager menuMgr = new MenuManager("#PopupMenu");
			menuMgr.setRemoveAllWhenShown(true);
			menuMgr.addMenuListener(StackBrowserView::fillContextMenu);
			final TreeViewer treeViewer = utils.NotNull.check(stackTreeViewer);
			Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
			treeViewer.getControl().setMenu(menu);
			stackBrowserView.getSite().registerContextMenu(menuMgr, treeViewer);
		}

		private void hookDoubleClickAction() {
			stackTreeViewer.addDoubleClickListener(event -> {
				localClickAction.run();
			});
			
			globalTableViewer.addDoubleClickListener(event -> {
				globalClickAction.run();
			});
		}

		private void setFocus() {
			stackTreeViewer.getControl().setFocus();
		}

		private void showMessage(String message) {
			Messages.showMessage(getShell(), message);
		}

		private void updateActionBarsForLoadedData(StackBrowserView stackBrowserView){
			IActionBars bars = stackBrowserView.getViewSite().getActionBars();
			IToolBarManager manager = bars.getToolBarManager();
			assert manager != null;
			addLocalToolBar(manager, new Separator());
			addLocalToolBar(manager, localAction);
			addLocalToolBar(manager, globalAction);
			addLocalToolBar(manager, new Separator());
			addLocalToolBar(manager, backwardAction);
			addLocalToolBar(manager, forwardAction);
			bars.updateActionBars();
		}

		/**
		 * Reveal the current path trace entry.  This method is specifically intended for cases where
		 * the path trace markers were previously hidden, and you want to reveal them without changing
		 * anything about the current entry.
		 * 
		 * If you are changing path entries (i.e., stepping through the path trace), you should instead
		 * use StackBrowserView.pathStep().
		 */
		private void revealCurrentPathEntry() {
			// a bit tricky: do this by stepping 0 path trace entries from current
			pathStep(0);
		}
		
		/**
		 * updateFwdBackButtons() will grey-out or make visible the step forward/backward buttons based
		 * on the current frame + entry context.
		 * @param stackBrowserView the view object (for context)
		 */
		private void updateFwdBackButtons(StackBrowserView stackBrowserView){
			// Eclipse's shared images
			ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
			
			// determine which of the fwd/bwd buttons should be enabled
			ImageDescriptor fwd;
			boolean fwdEnabled;
			ImageDescriptor bwd;
			boolean bwdEnabled;
			final FrameModel frame = stackBrowserView.currentFrame;
			if(!localAction.isChecked() || frame == null || stackBrowserView.currentPathEntry < 1){
				fwd = images.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD_DISABLED);
				fwdEnabled = false;
			}
			else{
				fwd = images.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD);
				fwdEnabled = true;
			}
			if(!localAction.isChecked() || frame == null || !frame.hasChildren() ||
					stackBrowserView.currentPathEntry > frame.getChildren().length-2){
				bwd = images.getImageDescriptor(ISharedImages.IMG_TOOL_BACK_DISABLED);
				bwdEnabled = false;
			}
			else{
				bwd = images.getImageDescriptor(ISharedImages.IMG_TOOL_BACK);
				bwdEnabled = true;
			}
			
			forwardAction.setImageDescriptor(fwd);
			forwardAction.setEnabled(fwdEnabled);
			backwardAction.setImageDescriptor(bwd);
			backwardAction.setEnabled(bwdEnabled);
		}
		
		/**
		 * Clean up components, and deactivate shortcut keys for actions.
		 */
		private void dispose() {
			backwardActivation.getHandlerService().deactivateHandler(this.backwardActivation);
			forwardActivation.getHandlerService().deactivateHandler(forwardActivation);
		}
	}

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "csi.views.StackBrowserView";

	private boolean loadedData;
	private @Nullable AnalysisData exeData;
	
	private @Nullable ViewComponents viewComponents;

	// keep status of current frame for stepping through traced path
	private @Nullable FrameModel currentFrame;
	private int currentPathEntry = -1;
	
	// handling workspace file management and management of markers/annotations
	private @Nullable WorkspaceManager workspaceManager;
	private @Nullable MarkerManager markerManager;
	
	private @Nullable AnalysisData readAnalysisDataFromFile(String path){
		final ViewComponents components = utils.NotNull.check(viewComponents);
		AnalysisReader reader = new AnalysisReader(components.getShell());
		return(reader.readAnalysisFromFile(path));
	}
	
	/**
	 * createPartControl() is a callback function from the Eclipse UI that does necessary setup to create the viewer
	 * and initialize it.
	 * @param parent the parent container for this viewer
	 */
	@Override
	public void createPartControl(@Nullable Composite parent) {
		viewComponents = new ViewComponents(this, parent);
	}
	
	/**
	 * fillContextMenu() sets up actions for the right-click drop-down menu inside the viewer.  (Currently none.)
	 * NOTE: It seems to support allowing further additions by other add-ons?  I don't know anything about this...it
	 * was here in the sample that I started with.
	 * @param manager the manager for the drop-down menu
	 */
	private static void fillContextMenu(@Nullable IMenuManager manager) {
		// for now, we contribute nothing to the right-click drop-down menu
		assert manager != null;
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void clearGlobalAndLocalAnnotations(){
		clearWorkspaceAnnotations(MarkerManager.globalAnnotationTypes);
		clearWorkspaceAnnotations(MarkerManager.localAnnotationTypes);
	}
	
	private void doFileMarkersGlobal(IFile theFile, Set<Integer> exeYes, Set<Integer> exeNo, Set<Integer> exeMaybe){
		assert markerManager != null;
		markerManager.doFileMarkers(theFile, "csi.global", "Global", exeYes, exeNo, exeMaybe);
	}
	
	private void doFileMarkersLocal(IFile theFile, Set<Integer> exeYes, Set<Integer> exeNo, Set<Integer> exeMaybe){
		// clear previous stack-frame-local markers
		// (Note that this must be done in the view, rather than in MarkerManager, because it iterates
		// through loaded data)
		clearWorkspaceAnnotations(MarkerManager.localAnnotationTypes);
		
		assert markerManager != null;
		markerManager.doFileMarkers(theFile, "csi", "Local", exeYes, exeNo, exeMaybe);
	}
	
	private void clearFrame(){
		updateFrameAndPathEntry(null, -1);
	}
	
	/**
	 * updateFrameAndPathEntry() updates the current frame and the current active path entry for that frame.
	 * Note that both inputs are validated: the only valid entry for a null frame is -1, and entries for non-null
	 * frames are valid if they are within the number of path entries for that frame.
	 * 
	 * @param frame the newly current stack frame model
	 * @param entry the newly current path trace entry index
	 */
	private void updateFrameAndPathEntry(@Nullable FrameModel frame, int entry){
		final ViewComponents components = viewComponents;
		assert components != null;

		// check input validity
		if(frame == null && entry != -1){
			showErrorMessage("Internal error: attempt to set index for no local frame");
			clearFrame();
			return;
		}
		else if(frame != null && (entry < 0 || !frame.hasChildren() || entry > frame.getChildren().length - 1)){
			showErrorMessage("Internal error: attempt to set invalid path index for frame");
			clearFrame();
			return;
		}
		
		// update the values
		boolean updatedFrame = false;
		int prevEntry = this.currentPathEntry;
		if(frame != this.currentFrame){
			this.currentFrame = frame;
			updatedFrame = true;
		}
		this.currentPathEntry = entry;
		
		// grey-out or make-visible the forward/backward buttons, if appropriate
		boolean movedToOrFromEnd = prevEntry != entry &&
				(frame == null || entry == 0 || entry == frame.getChildren().length-1 ||
				 prevEntry <= 0 || prevEntry == frame.getChildren().length-1);
		if(updatedFrame || movedToOrFromEnd)
			components.updateFwdBackButtons(this);
	}
	
	/**
	 * updatePathEntry() updates the current active path entry for the current active frame.
	 * Note that both input is validated as: the only valid entry for a null frame is -1, and entries for non-null
	 * frames are valid if they are within the number of path entries for that frame.  Thus, the caller should be
	 * aware of the current frame, or call updateFrameAndPathEntry() instead.
	 * 
	 * @param entry the newly current path trace entry index
	 */
	private void updatePathEntry(int entry){
		updateFrameAndPathEntry(this.currentFrame, entry);
	}
	
	private void pathStep(int amount){
		if(!this.loadedData || this.currentFrame == null || this.currentPathEntry < 0)
			return;
		
		// get the lines (steps / children) in the current frame
		assert currentFrame != null;
		LocalModel[] children = currentFrame.getChildren();
		
		// fix up in case errors have occurred setting the path value to something invalid
		this.currentPathEntry = Math.min(this.currentPathEntry, children.length - 1);
		
		int newValue = this.currentPathEntry + amount;
		if(newValue < 0 || newValue > children.length - 1)
			return;
		updatePathEntry(newValue);
		
		LineModel theLine;
		if(!(children[this.currentPathEntry] instanceof LineModel)){
			showErrorMessage("Internal error: Unexpected structure relating lines to frames");
			return;
		}
		theLine = (LineModel)children[this.currentPathEntry];
		utils.NotNull.check(viewComponents).stackTreeViewer.setSelection(new StructuredSelection(theLine), true);
		
		// TODO: clean this up.  (It sets the "current" and "prev/next" arrows for path entries.)
		IFile theFile = openFileAndGotoLine(utils.NotNull.check(theLine.getParent()).getFile(), theLine.getLine());
		if(theFile == null){
			showErrorMessage("Internal error: could not open path entry file");
			return;
		}
		final MarkerManager manager = markerManager;
		assert manager != null;
		manager.clearMarkers(theFile, "csi.path.currentMarker");
		manager.clearMarkers(theFile, "csi.path.nextMarker");
		Set<Integer> currentLines = new HashSet<>();
		currentLines.add(theLine.getLine());
		manager.setMarkers(theFile, currentLines, "csi.path.currentMarker", "Current path entry");
		Set<Integer> nextLines = new HashSet<>();
		if(this.currentPathEntry > 0)
			nextLines.add(((LineModel)children[this.currentPathEntry-1]).getLine());
		if(this.currentPathEntry < children.length-1)
			nextLines.add(((LineModel)children[this.currentPathEntry+1]).getLine());
		manager.setMarkers(theFile, nextLines, "csi.path.nextMarker", "Adjacent path entry");
	}
	
	/*
	private IFile createTempCopyOfFile(IFile file){
		if(file == null)
			return(null);
		
		String tempDir = System.getProperty("java.io.tmpdir");
		System.out.println("temp dir is: " + tempDir);
		
		IFile newFile = file.getProject().getFile(tempDir + file.getName());
		//theFile.getResourceAttributes().setReadOnly(true);
		//theFile.setResourceAttributes(theFile.getResourceAttributes());
		theFile.setReadOnly(true);
		return(newFile);
	}
	*/
	
	/**
	 * openFileAndGotoLine() opens the specified file in its default editor, then seeks to the specified line in
	 * that file.
	 * 
	 * @param fileName the file to open (as a path or filename)
	 * @param gotoLine the line to seek to
	 * @return the file reference, if successfully opened and focused.  Otherwise, null.
	 */
	private @Nullable IFile openFileAndGotoLine(String fileName, int gotoLine){
		assert workspaceManager != null;
		IFile theFile;
		try {
			theFile = utils.NotNull.check(workspaceManager).findFileInWorkspace(fileName);
		}
		catch (MissingFileException e) {
			showErrorMessage(utils.NotNull.check(e.getMessage()));
			return(null);
		}
		
		// TODO: update this after further refactoring.  findFileInWorkspace() will eventually be
		// non-nullable and return all problems as exceptions
		if(theFile == null){
			// a serious error occurred while searching for the file.
			// An error should already be printed in the callee
			return(null);
		}

		// get the current workbench and page from the Eclipse UI
		IWorkbenchWindow eclipseWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if(eclipseWindow == null){
			showErrorMessage("Internal error.  Unexpected file search from non-UI thread");
			return(null);
		}
		IWorkbenchPage eclipsePage = eclipseWindow.getActivePage();
		if(eclipsePage == null){
			showErrorMessage("Internal error.  No eclipse page due to call from non-UI thread");
			return(null);
		}
		
		// find the appropriate editor for the file
		IEditorDescriptor defaultEditor =
				PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(theFile.getName());
		if(defaultEditor == null){
			// fall back on default text editor -- this is an unfortunate thing to do
			defaultEditor = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor("test.txt");
		}
		
		// open the editor and get document parts and things
		IEditorPart openedEditor;
		try{
			openedEditor = eclipsePage.openEditor(new FileEditorInput(theFile), defaultEditor.getId());
		}
		catch(PartInitException e){
			showErrorMessage("Some crazy exception initializing parts");
			e.printStackTrace();
			return(null);
		}
		if(openedEditor == null || !(openedEditor instanceof ITextEditor)){
			showErrorMessage("Internal error: Text editor properties unexpected.");
			return(null);
		}
		ITextEditor editor = (ITextEditor)openedEditor;
		IDocumentProvider provider = editor.getDocumentProvider();
		IDocument document = provider.getDocument(editor.getEditorInput());
		
		// go to the appropriate line (if it exists)
		try {
			// a hack to get it to center the line in the editor (there seems to be no simple way to do this)
			editor.selectAndReveal(0, 0);
			
			// seems to go one line too far (0-indexed array?)
			int start = document.getLineOffset(gotoLine-1) +
					document.getLineInformation(gotoLine-1).getLength();
			editor.selectAndReveal(start, 0);
		}
		catch (BadLocationException e) {
			showErrorMessage("Provided line number '" + gotoLine + "' does not exist in file");
			return(null);
		}
		
		return(theFile);
	}
	
	private void clearAnnotationsIgnoringExceptions(String file, @NonNull String[] annotationTypes){
		if(markerManager == null)
			return;
		
		try{
			// TODO: I get an error without this non-null assertion here.  Why?
			utils.NotNull.check(markerManager).clearMarkers(file, utils.NotNull.check(annotationTypes));
		}
		catch(MissingFileException e){
			// ignore this Exception: we should not show huge numbers of error boxes
			// just to try to clear annotations for analysis data
		}
	}
	
	private void clearWorkspaceAnnotations(String[] types){
		// clear all annotations from all files in the current workspace
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final MarkerManager manager = utils.NotNull.check(markerManager);
		for (IProject project : root.getProjects()) {
			// clear annotations from open projects
			if(!project.exists()){
				showErrorMessage("Plugin returned non-existant project while clearing annotations");
				return;
			}
			else if(project.isOpen()){
				manager.clearMarkers(project, types);
			}
		}
		
		// clear all annotations from all files currently open in editors
		IEditorReference[] editors =
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		try{
			for(IEditorReference editor : editors){
				if(editor.getEditorInput() instanceof FileEditorInput){
					FileEditorInput fileEditor = (FileEditorInput)editor.getEditorInput();
					final IFile file = fileEditor.getFile();
					assert file != null;
					manager.clearMarkers(file, types);
				}
			}
		}
		catch(PartInitException e){
			showErrorMessage("Error accessing open editors");
			e.printStackTrace();
		}
		
		// clear all annotations from all files in loaded CSI execution data
		if(loadedData){
			final AnalysisData data = utils.NotNull.check(exeData);
			for(FrameModel fData : data.getLocalData().getChildren())
				clearAnnotationsIgnoringExceptions(fData.getFile(), types);
			for(FileModel fData : data.getGlobalData()){
				clearAnnotationsIgnoringExceptions(fData.getFile(), types);
			}
		}
	}
	
	/**
	 * Necessary clean-up for the view.  Specifically, we need to remove all annotations, and
	 * clean up the view components (i.e., disable all actions and keyboard shortcuts).
	 */
	@Override
	public void dispose() {
		clearGlobalAndLocalAnnotations();
		
		// dispose the view components (disables all keyboard shortcuts)
		utils.NotNull.check(this.viewComponents).dispose();
	}

	private void showErrorMessage(String message){
		final ViewComponents components = utils.NotNull.check(viewComponents);
		Messages.showErrorMessage(components.getShell(), message);
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		utils.NotNull.check(viewComponents).setFocus();
	}
}
