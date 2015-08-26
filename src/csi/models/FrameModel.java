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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * FrameModel is the local-data variant of the FunctionModel data model.  The key differences are
 * that FrameModels:
 * (1) have a root tree model as a parent, and
 * (2) have path trace data.
 */
public class FrameModel extends FunctionModel implements LocalModel {
	private @Nullable RootModel parent = null;
	private final List<LineModel> path = new ArrayList<>();
	
	/**
	 * Construct a FrameModel from function and file names.
	 * 
	 * @param name the name of the frame's function
	 * @param file the name of (or path to) the containing file
	 */
	public FrameModel(String name, String file){
		super(name, file);
	}
	
	/**
	 * Add a line to the frame's path trace.
	 * 
	 * @param child the data model for the added line entry
	 * @throws ModelException if the child is already part of another frame's path trace
	 */
	public void addChild(LineModel child) throws ModelException {
		// could re-throw ModelException
		child.setFrame(this);
		
		path.add(child);
	}
	
	/**
	 * Set the parent (RootModel) of this stack frame.
	 * NOTE: Users should *not* call this function directly.  It will cause problems.
	 *       Use RootModel.addFrame() instead.
	 * 
	 * @param parent the tree root parent model
	 * @throws ModelException if this frame is already part of another root model's tree
	 * @throws IllegalArgumentException if parent is null
	 */
	void setParent(RootModel parent) throws ModelException, IllegalArgumentException {
		if(this.parent != null)
			throw new ModelException("Attempt to add frame to multiple root models");
		
		this.parent = parent;
	}

	@Override
	public boolean hasChildren() {
		return(this.path.size() > 0);
	}

	@Override
	public LineModel[] getChildren() {
		return utils.NotNull.check(this.path.toArray(new LineModel[0]));
	}
	
	@Override
	public String toString(){
		return(super.toString() + (this.hasChildren() ? " : " + this.path.get(0) : ""));
	}

	@Override
	public @Nullable LocalModel getParent() {
		return(this.parent);
	}
}
