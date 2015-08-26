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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Shell;

import utils.ArrayUtils;
import csi.Messages;
import csi.models.CodeModel;
import csi.models.FileModel;
import csi.models.FrameModel;
import csi.models.LineModel;
import csi.models.ModelException;
import csi.models.RootModel;

/**
 * AnalysisReader reads analysis data (currently only from external files).
 */
public class AnalysisReader {
	
	private final Shell shell;
	
	/**
	 * The constructor requires shell context to print error messages during file reading.
	 * 
	 * @param shell the shell context to use for creating possible error dialogs
	 */
	public AnalysisReader(Shell shell) {
		this.shell = shell;
	}
	
	/**
	 * A wrapper to show error dialogs without specifying the shell context.
	 * 
	 * @param message the error message to display
	 */
	private void showErrorMessage(String message){
		Messages.showErrorMessage(shell, message);
	}
	
	/**
	 * Read analysis data from an external file.
	 * 
	 * @param path the path to the analysis data file
	 * @return an AnalysisData object containing the local and global analysis data from the file at "path"
	 */
	public @Nullable AnalysisData readAnalysisFromFile(String path){
		RootModel localData = new RootModel();
		HashMap<String, FileModel> globalData = new HashMap<>();
		
		final File f = new File(path);
		try(Scanner s = new Scanner(f)){
        	while(s.hasNextLine()){
        		// --- read and split the line ---
        		String line = s.nextLine();
        		final String trimmed = utils.NotNull.check(line.trim());
        		if(trimmed.length() == 0)
        			continue;
        		final String[] splitted = trimmed.split(";");
       			assert !Arrays.asList(splitted).contains(null);
       			@SuppressWarnings("null")
				final @NonNull String @NonNull[] lineParts = splitted;
        		if(lineParts.length != 7){
        			showErrorMessage("Invalid line detected in CSI Analysis Result File:" + System.lineSeparator() +
        					line);
        			return(null);
        		}
        		
        		
        		// --- parse each line part ---
        		// function name and file
        		String funcName = lineParts[0];
        		String funcFile = lineParts[1];
        		if(funcName.length() < 1){
        			showErrorMessage("Empty function name in CSI Analysis Result File!");
        			return(null);
        		}
        		
        		// local or global
        		boolean isLocal = false;
        		switch(lineParts[2]){
        		case "local":
        			isLocal = true;
        			break;
        		case "global":
        			isLocal = false;
        			break;
        		default:
        			showErrorMessage("Invalid local/global specifier in CSI Analysis file:" + lineParts[2]);
        			return(null);
        		}
        		
        		// Java has no "map" function...
        		//OneArg<String, int> lambda = Integer::parseInt;
        		//int[] pathLines = ArrayUtils.map(pathString, f);
        		
        		// exeYes
        		int[] yesLines;
        		try{
        			yesLines = ArrayUtils.linesArrayForLineString(lineParts[3]);
        		}
        		catch(NumberFormatException e){
        			showErrorMessage("Invalid line number in exeYes for entry:" + System.lineSeparator() +
        					line);
        			return(null);
        		}

        		// exeNo
        		int[] noLines;
        		try{
        			noLines = ArrayUtils.linesArrayForLineString(lineParts[4]);
        		}
        		catch(NumberFormatException e){
        			showErrorMessage("Invalid line number in exeNo for entry:" + System.lineSeparator() +
        					line);
        			return(null);
        		}

        		// exeMaybe
        		int[] maybeLines;
        		try{
        			maybeLines = ArrayUtils.linesArrayForLineString(lineParts[5]);
        		}
        		catch(NumberFormatException e){
        			showErrorMessage("Invalid line number in exeMaybe for entry:" + System.lineSeparator() +
        					line);
        			return(null);
        		}
        		
        		// path data
        		int[] pathLines;
        		try{
        			pathLines = ArrayUtils.linesArrayForLineString(lineParts[6]);
        		}
        		catch(NumberFormatException e){
        			showErrorMessage("Invalid line number in path for entry:" + System.lineSeparator() +
        					line);
        			return(null);
        		}
        		
        		
        		// --- build the data model ---
        		// (might be local or global data)
        		CodeModel model;
        		if(isLocal){
        			FrameModel frame = new FrameModel(funcName, funcFile);
        			
        			// handle the traced path data (not applicable for global function models)
            		for(int entry : pathLines){
            			LineModel entryModel = new LineModel(entry);
            			try {
    						frame.addChild(entryModel);
    					}
            			catch (ModelException e) {
    						showErrorMessage("Internal error building models for line numbers for function " + funcName);
    						e.printStackTrace();
    						return(null);
            			}
            		}
            		
            		try{
            			localData.addFrame(frame);
            		}
            		catch (ModelException e) {
            			showErrorMessage("Internal error building model for function " + funcName);
            			e.printStackTrace();
            			return(null);
            		}
            		
            		// the frame model is our model (for the rest of the data)
            		model = frame;
        		}
        		else{
        			// we currently use only FileModel for global models.  FunctionModel is not currently used.
        			// Some day, we will have FunctionModels as children of each FileModel.
        			// So, for now, get or create the global model. (Since we aren't using FunctionModels,
        			//this is how we aggregate multiple entries for the same source file.)
        			FileModel file = globalData.get(funcFile);
        			@SuppressWarnings("null") // nullness analysis mistakenly thinks HashMap.get returns nonnull
					final boolean absent = file == null;
        			if(absent){
        				file = new FileModel(funcFile);
        				globalData.put(funcFile, file);
        			}
        			
        			// the file model is our model (for the rest of the data)
        			model = file;
        		}
        		
        		// --- Execution trace data ---
        		// exeYes
        		for(int entry : yesLines){
        			if(entry < 0){
        				showErrorMessage("Invalid line '" + entry + "' for function " + funcName);
        				return(null);
        			}
        			model.addYesLine(entry);
        		}
        		// exeNo
        		for(int entry : noLines){
        			if(entry < 0){
        				showErrorMessage("Invalid line '" + entry + "' for function " + funcName);
        				return(null);
        			}
        			model.addNoLine(entry);
        		}
        		// exeMaybe
        		for(int entry : maybeLines){
        			if(entry < 0){
        				showErrorMessage("Invalid line '" + entry + "' for function " + funcName);
        				return(null);
        			}
        			model.addMaybeLine(entry);
        		}
        	}
        }
        catch(FileNotFoundException | IllegalArgumentException e){
        	showErrorMessage("Invalid file specified");
        	e.printStackTrace();
        	return(null);
        }
        catch(NoSuchElementException | IllegalStateException e){
        	showErrorMessage("Broken file read");
        	e.printStackTrace();
        	return(null);
        }
		
		return(new AnalysisData(localData, utils.NotNull.check(globalData.values().toArray(new FileModel[0]))));
	}
}
