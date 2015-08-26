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

package csi;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Messages is a purely-static class used to show error messages to the user
 * via dialog boxes.
 */
public final class Messages {

	private Messages() {
		// static class.  can't be instantiated
		System.exit(1);
	}

	/**
	 * Display the specified message in an "error"-style dialog.
	 * 
	 * @param shell the shell to use for creating the dialog
	 * @param message the message to display
	 */
	public static void showErrorMessage(Shell shell, String message){
		System.err.println("ERROR: " + message);
		MessageDialog.openError(shell, "CSI Error", message);
	}
	
	/**
	 * Display the specified message in an "information"-style dialog.
	 * 
	 * @param shell the shell to use for creating the dialog
	 * @param message the message to display
	 */
	public static void showMessage(Shell shell, String message){
		System.out.println("MESSAGE: " + message);
		MessageDialog.openInformation(shell, "CSI Information", message);
	}

}
