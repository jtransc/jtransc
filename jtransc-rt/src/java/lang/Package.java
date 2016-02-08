/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang;

//public class Package implements java.lang.reflect.AnnotatedElement {
public class Package {
	native public String getName();

	native public String getSpecificationTitle();

	native public String getSpecificationVersion();

	native public String getSpecificationVendor();

	native public String getImplementationTitle();

	native public String getImplementationVersion();

	native public String getImplementationVendor();

	native public boolean isSealed();

	native public boolean isCompatibleWith(String desired) throws NumberFormatException;

	native public static Package getPackage(String name);

	native public static Package[] getPackages();

	native public int hashCode();

	native public String toString();
}
