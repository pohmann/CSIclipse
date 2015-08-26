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
 * ModelException is an exception class for issues constructing local and global analysis data models.
 */
public class ModelException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Construct a ModelException with no message.
	 */
	public ModelException() {
	}

	/**
	 * Construct a ModelException with a String message.
	 * 
	 * @param message the message
	 */
	public ModelException(String message) {
		super(message);
	}

	/**
	 * Construct a ModelException from a base Throwable cause.
	 * 
	 * @param cause the base Throwable
	 */
	public ModelException(Throwable cause) {
		super(cause);
	}

	/**
	 * Construct a ModelException with a String message and a base Throwable cause.
	 * 
	 * @param message the message
	 * @param cause the base Throwable
	 */
	public ModelException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Construct a ModelException with a message, base cause, custom suppression setting, and custom
	 * stack trace setting.
	 * 
	 * @param message the message
	 * @param cause the base Throwable
	 * @param enableSuppression whether suppression is enabled
	 * @param writeableStackTrace whether the stack trace is writable
	 */
	public ModelException(String message, Throwable cause, boolean enableSuppression,
			boolean writeableStackTrace) {
		super(message, cause, enableSuppression, writeableStackTrace);
	}

}
