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
import csi.models.FrameModel;
import csi.models.LineModel;

/**
 * LocalLabelProvider is the provider for labels and images for local analysis data models.
 */
public class LocalLabelProvider extends LabelProvider implements ITableLabelProvider {

	@Override
	public @Nullable Image getColumnImage(@Nullable Object element, int columnIndex) {
		return getImage(element);
	}

	@Override
	public String getColumnText(@Nullable Object element, int columnIndex) {
		return utils.NotNull.check(getText(element));
	}

	@Override
	public @Nullable Image getImage(@Nullable Object obj) {
		if(obj instanceof LineModel)
			return(Activator.getImage("icons/stack_line.gif"));
		else if(obj instanceof FrameModel)
			return(Activator.getImage("icons/stack_frame.gif"));
		else
			return(null);
	}

}
