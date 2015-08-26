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

package csi.analysis;

import csi.models.FileModel;
import csi.models.RootModel;

/**
 * AnalysisData is a basic pair class that stores local and global analysis data.  Currently,
 * this data is always produced all-at-once by an analysis file read.
 */
public class AnalysisData {

	private final RootModel localData;
	private final FileModel[] globalData;
	
	/**
	 * Construct an analysis data object from complete local and global analysis data.  Currently,
	 * we expect that neither data storage should be updated after object creation.
	 * 
	 * @param localData the local analysis results, in the viewer's tree model format
	 * @param globalData the global analysis results
	 */
	public AnalysisData(RootModel localData, FileModel[] globalData) {
		this.localData = localData;
		this.globalData = globalData;
	}
	
	/**
	 * Get the local analysis data.
	 * 
	 * @return the local analysis data
	 */
	public RootModel getLocalData(){
		return(localData);
	}
	
	/**
	 * Get the global analysis data. 
	 * 
	 * @return the global analysis data
	 */
	public FileModel[] getGlobalData(){
		return(globalData);
	}

}
