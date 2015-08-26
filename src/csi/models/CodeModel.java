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

import java.util.HashSet;
import java.util.Set;

/**
 * CodeModel is an abstract base class for all local and global data models that represent
 * executed or unexecuted code.  Thus, it provides the normal functionality for creating, storing,
 * and updating yes/no/maybe data at a file level.
 */
public abstract class CodeModel {
	private final String file;
	private final Set<Integer> exeYes = new HashSet<>();
	private final Set<Integer> exeNo = new HashSet<>();
	private final Set<Integer> exeMaybe = new HashSet<>();
	
	/**
	 * The constructor only requires the file name for the data model.  (Yes/No/Maybe lines
	 * are added via the appropriate "add" method.)
	 * 
	 * @param file the file represented by the data model
	 */
	public CodeModel(String file){
		this.file = file;
	}
	
	/**
	 * Add a line to the "yes" set.
	 * 
	 * @param line the line to add
	 * @throws IllegalArgumentException if the line number is less than zero
	 */
	public void addYesLine(int line) throws IllegalArgumentException {
		if(line < 1)
			throw new IllegalArgumentException("Invalid negative line '" + line + "'");
		// TODO: also check for conflicts
		this.exeYes.add(line);
	}
	
	/**
	 * Add a line to the "no" set.
	 * 
	 * @param line the line to add
	 * @throws IllegalArgumentException if the line number is less than zero
	 */
	public void addNoLine(int line) throws IllegalArgumentException {
		if(line < 1)
			throw new IllegalArgumentException("Invalid negative line '" + line + "'");
		// TODO: also check for conflicts
		this.exeNo.add(line);
	}
	
	/**
	 * Add a line to the "maybe" set.
	 * 
	 * @param line the line to add
	 * @throws IllegalArgumentException if the line number is less than zero
	 */
	public void addMaybeLine(int line) throws IllegalArgumentException {
		if(line < 1)
			throw new IllegalArgumentException("Invalid negative line '" + line + "'");
		// TODO: also check for conflicts
		this.exeMaybe.add(line);
	}
	
	/**
	 * Get the file for the model.
	 * 
	 * @return the file
	 */
	public String getFile(){
		return(this.file);
	}
	
	/**
	 * Get the size of the "yes" set.
	 * 
	 * @return the size of the set
	 */
	public int getExeYesSize(){
		return(this.exeYes.size());
	}
	
	/**
	 * Get the size of the "no" set.
	 * 
	 * @return the size of the set
	 */
	public int getExeNoSize(){
		return(this.exeNo.size());
	}
	
	/**
	 * Get the size of the "maybe" set.
	 * 
	 * @return the size of the set
	 */
	public int getExeMaybeSize(){
		return(this.exeMaybe.size());
	}
	
	/**
	 * Get a copy of the "yes" set.  This is not a cheap operation.  If you just need the size
	 * of the set, use getExeYesSize().
	 * 
	 * @return a copy of the set
	 */
	public Set<Integer> getExeYes(){
		return(new HashSet<>(this.exeYes));
	}
	
	/**
	 * Get a copy of the "no" set.  This is not a cheap operation.  If you just need the size
	 * of the set, use getExeNoSize().
	 * 
	 * @return a copy of the set
	 */
	public Set<Integer> getExeNo(){
		return(new HashSet<>(this.exeNo));
	}
	
	/**
	 * Get a copy of the "maybe" set.  This is not a cheap operation.  If you just need the size
	 * of the set, use getExeMaybeSize().
	 * 
	 * @return a copy of the set
	 */
	public Set<Integer> getExeMaybe(){
		return(new HashSet<>(this.exeMaybe));
	}
}
