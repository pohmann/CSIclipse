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

package providers;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;

import csi.Messages;
import csi.models.LocalModel;

/**
 * LocalContentProvider is the provider for tree content for local analysis data models.  That is, it acts
 * as a bridge between the local csi.models classes and the Eclipse ITree interface.
 */
public class LocalContentProvider implements ITreeContentProvider {
	private Shell parent;
	
	/**
	 * The constructor requires shell context to print error messages during any content callbacks.
	 * 
	 * @param parent the parent shell context to use for creating possible error dialogs
	 */
	public LocalContentProvider(Shell parent){
		this.parent = parent;
	}
	
	@Override
	public Object @Nullable[] getElements(@Nullable Object inputElement) {
		return(this.getChildren(inputElement));
	}

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(@Nullable Viewer viewer, @Nullable Object oldInput, @Nullable Object newInput) {

	}

	@Override
	public Object @Nullable[] getChildren(@Nullable Object parentElement) {
		if(parentElement == null)
			return(null);
		else if(!(parentElement instanceof LocalModel)){
			Messages.showErrorMessage(this.parent, "Invalid attempt attempt to show content for non-viewer class");
			return(null);
		}
		
		return(((LocalModel)parentElement).getChildren());
	}

	@Override
	public @Nullable Object getParent(@Nullable Object element) {
		if(element == null){
			return(null);
		}
		else if(!(element instanceof LocalModel)){
			Messages.showErrorMessage(this.parent, "Invalid attempt attempt to show parent content for non-viewer class");
			return(null);
		}

		return(((LocalModel)element).getParent());
	}

	@Override
	public boolean hasChildren(@Nullable Object element) {
		if(element == null){
			return(false);
		}
		else if(!(element instanceof LocalModel)){
			Messages.showErrorMessage(this.parent, "Invalid attempt attempt to check children for non-viewer class");
			return(false);
		}
		
		return(((LocalModel)element).hasChildren());
	}

}
