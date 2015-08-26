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

package utils;

import java.lang.reflect.Array;

/**
 * ArrayUtils is a purely static utility class containing methods for array type manipulation.
 * TODO: rewrite most or all of this with lambdas.
 */
public final class ArrayUtils {

	/**
	 * An unused function emulating the functional-style "map" function.
	 * 
	 * @param from the source array
	 * @param f the map function
	 * @return the new array after applying "f" to each element of "from"
	 */
	public static <F, T> T[] map(F[] from, Lambdas.OneArg<T, F> f){
        // can't do this: ...Java
		//T[] result = new T[from.length];
        
        // so instead do this:...Java
        Class<T> x = null;
        @SuppressWarnings("unchecked")
		T[] result = (T[]) Array.newInstance(x, from.length); // Type safety: Unchecked cast from Object to T[]
        System.out.println("in this case: "+result.getClass().getComponentType().getSimpleName());
        
		for(int i = 0; i < from.length; ++i){
			result[i] = f.apply(from[i]);
		}
		return result;
	}
	
	/**
	 * Convert an input array of String to an array of int.
	 * 
	 * @param input the input array
	 * @return an array of input.length with each element converted to an int
	 * @throws NumberFormatException if any element of "input" does not expressed a signed
	 * decimal integer (i.e., cannot be converted to an int via Integer.parseInt)
	 */
	public static int[] stringArrayToIntArray(String[] input) throws NumberFormatException {
		int[] result = new int[input.length];
		for(int i = 0; i < input.length; ++i){
			// might throw NumberFormatException
			result[i] = Integer.parseInt(input[i]);
		}
		return(result);
	}
	
	/**
	 * Convert an input string (with entries separated by ",") to an array of ints.
	 * 
	 * @param input the input string
	 * @return the corresponding array of int
	 * @throws NumberFormatException if any element of "input" does not expressed a signed
	 * decimal integer (i.e., cannot be converted to an int via Integer.parseInt)
	 */
	public static int[] linesArrayForLineString(String input) throws NumberFormatException {
		if(input.trim().isEmpty())
			return(new int[0]);

		String[] yesString = input.trim().split(",");
		// could re-throw NumberFormatException
		assert yesString != null;
		return(stringArrayToIntArray(yesString));
	}
	
	private ArrayUtils() {
		// cannot instantiate this class
		System.exit(1);
	}

}
