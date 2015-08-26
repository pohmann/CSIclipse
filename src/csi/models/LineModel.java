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

package csi.models;

import org.eclipse.jdt.annotation.Nullable;

/**
 * LineModel is a local-analysis model for lines in a path trace.  Thus, it's "parent" model type
 * is FrameModel.
 */
public class LineModel implements LocalModel {
	private @Nullable FrameModel parent = null;
	private final int line;
	
	/**
	 * Construct a line model for a given line number.
	 * 
	 * @param line the line number
	 */
	public LineModel(int line) {
		this.line = line;
	}
	
	/**
	 * Set the parent (FrameModel) of this path trace entry.
	 * NOTE: Users should *not* call this function directly.  It will cause problems.
	 *       Use FrameModel.addChild() instead.
	 * 
	 * @param parent the stack frame parent model
	 * @throws ModelException if this line model is already part of another frame's path trace
	 * @throws IllegalArgumentException if parent is null
	 */
	void setFrame(FrameModel parent) throws ModelException, IllegalArgumentException {
		if(this.parent != null)
			throw new ModelException("Attempt to add line to multiple frame models");
		
		this.parent = parent;
	}
	
	/**
	 * Get the line number for this model.
	 * 
	 * @return the line number
	 */
	public int getLine(){
		return(this.line);
	}

	@Override
	public @Nullable FrameModel getParent() {
		return(this.parent);
	}

	@Override
	public boolean hasChildren() {
		return(false);
	}

	@Override
	public LocalModel[] getChildren() {
		return new LocalModel[0];
	}
	
	@Override
	public String toString(){
		return("Line " + this.line);
	}
}
