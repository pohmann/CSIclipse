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
 * LocalModel is an interface for all local analysis data models.  These are stored in the
 * form of a tree; thus, each must supply methods to access its parent and children.
 */
public interface LocalModel {
	/**
	 * Get the parent model for this model (in the local data model tree).
	 * 
	 * @return the model's parent
	 */
	public @Nullable LocalModel getParent();
	
	/**
	 * Return whether or not this model has any children (in the local data model tree).
	 * 
	 * @return true if this model has children, false otherwise
	 */
	public boolean hasChildren();
	
	/**
	 * Get the model's children (in the local data model tree).
	 * NOTE: Implementing classes will likely return a specific type (as each only allows
	 * one specific type of child).
	 * 
	 * @return the model's children
	 */
	public LocalModel[] getChildren();
}
