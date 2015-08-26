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
 * RootModel is the tree root model for local analysis data.  For any given analysis result, a user should
 * construct exactly one RootModel, with one FrameModel for each stack frame.  Global data should be stored
 * separately as FunctionModels.
 */
public class RootModel implements LocalModel {
	private final List<FrameModel> frames = new ArrayList<>();
	
	/**
	 * Add a stack frame to the local data model.
	 * NOTE: order of insertion matters.
	 * 
	 * @param child the stack frame model
	 * @throws ModelException if the frame is already part of another root model's tree
	 */
	public void addFrame(FrameModel child) throws ModelException {
		// could re-throw ModelException
		child.setParent(this);
		
		frames.add(child);
	}

	@Override
	public @Nullable LocalModel getParent() {
		return(null);
	}

	@Override
	public boolean hasChildren() {
		return (frames.size() > 0);
	}

	@Override
	public FrameModel[] getChildren() {
		return utils.NotNull.check(frames.toArray(new FrameModel[0]));
	}
}
