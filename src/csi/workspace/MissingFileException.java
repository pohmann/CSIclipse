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

package csi.workspace;

/**
 * MissingFileException is an exception class indicating that the WorkspaceManager was unable to locate a file
 * relating to analysis data.
 */
public class MissingFileException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Construct a MissingFileException with no message.
	 */
	public MissingFileException() {
	}

	/**
	 * Construct a MissingFileException with a String message.
	 * 
	 * @param message the message
	 */
	public MissingFileException(String message) {
		super(message);
	}

	/**
	 * Construct a MissingFileException from a base Throwable cause.
	 * 
	 * @param cause the base Throwable
	 */
	public MissingFileException(Throwable cause) {
		super(cause);
	}

	/**
	 * Construct a MissingFileException with a String message and a base Throwable cause.
	 * 
	 * @param message the message
	 * @param cause the base Throwable
	 */
	public MissingFileException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Construct a MissingFileException with a message, base cause, custom suppression setting, and custom
	 * stack trace setting.
	 * 
	 * @param message the message
	 * @param cause the base Throwable
	 * @param enableSuppression whether suppression is enabled
	 * @param writeableStackTrace whether the stack trace is writable
	 */
	public MissingFileException(String message, Throwable cause, boolean enableSuppression,
			boolean writeableStackTrace) {
		super(message, cause, enableSuppression, writeableStackTrace);
	}

}
