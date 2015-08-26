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
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import csi.Activator;
import csi.models.FileModel;

/**
 * GlobalLabelProvider is the provider for labels and images for global analysis data models.
 */
public class GlobalLabelProvider extends LabelProvider implements ITableLabelProvider {

	@Override
	public @Nullable Image getColumnImage(@Nullable Object element, int columnIndex) {
		if(!(element instanceof FileModel) || columnIndex != 0){
			return(null);
		}
		FileModel model = (FileModel)element;
		
		// compute the image based on the coverage data:
		// if(yes > 0%) green; else if(maybe > 0%) yellow; else red;
		if(model.getExeYesSize() > 0)
			return(Activator.getImage("icons/green_circle_small.png"));
		else if(model.getExeMaybeSize() > 0)
			return(Activator.getImage("icons/yellow_diamond_small.png"));
		else
			return(Activator.getImage("icons/red_square_small.png"));
	}

	@Override
	public @Nullable String getColumnText(@Nullable Object element, int columnIndex) {
		if(!(element instanceof FileModel)){
			return(null);
		}
		FileModel model = (FileModel)element;
		
		switch(columnIndex){
		case 0: return(null);
		case 1: return(model.getFile());
		case 2: return Integer.toString(model.getExeYesSize());
		case 3: return Integer.toString(model.getExeNoSize());
		case 4: return Integer.toString(model.getExeMaybeSize());
		default: return(null);
		}
	}

}
