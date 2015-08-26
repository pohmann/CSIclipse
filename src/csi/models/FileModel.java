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

/**
 * FileModel is a CodeModel for global analysis data at the file level.
 */
public class FileModel extends CodeModel {
	
	/**
	 * The constructor only requires the file name for the data model.  (Yes/No/Maybe lines
	 * are added via the appropriate "add" method.)
	 * 
	 * @param file the file represented by the data model
	 */
	public FileModel(String file){
		super(file);
	}
	
	@Override
	public String toString(){
		return(this.getFile());
	}
}
