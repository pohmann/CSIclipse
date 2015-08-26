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

/**
 * Lambdas is an interface for supporting generic lambdas.  It is currently unused.
 * It was intended for use with the (also unused) "map" function in ArrayUtils.java.
 * 
 * Credit goes to Sotirios Delimanolis and Leo Ufimtsev for initial code and inspiration.
 * http://stackoverflow.com/questions/27872387/can-a-java-lambda-have-more-than-1-parameter
 */
public interface Lambdas {
	/**
	 * Single argument generic lambda interface.
	 * 
	 * @param <R> the return type
	 * @param <A> the argument type
	 */
	public interface OneArg <R, A>{
		@SuppressWarnings("javadoc")
		public R apply (A a);
	}
	
	/**
	 * Two-argument generic lambda interface.
	 * 
	 * @param <R> the return type
	 * @param <A> the first argument type
	 * @param <B> the second argument type
	 */
	public interface TwoArg <R, A, B>{
		@SuppressWarnings("javadoc")
		public R apply (A a, B b);
	}
	
	/**
	 * Three-argument generic lambda interface.
	 * 
	 * @param <R> the return type
	 * @param <A> the first argument type
	 * @param <B> the second argument type
	 * @param <C> the third argument type
	 */
	public interface ThreeArg <R, A, B, C>{
		@SuppressWarnings("javadoc")
		public R apply (A a, B b, C c);
	}
}
