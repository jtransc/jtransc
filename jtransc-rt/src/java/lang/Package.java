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

import java.util.Objects;

//public class Package implements java.lang.reflect.AnnotatedElement {
public class Package {
	private String name;

	private Package(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getSpecificationTitle() {
		return name;
	}

	public String getSpecificationVersion() {
		return "1.0";
	}

	public String getSpecificationVendor() {
		return "jtransc";
	}

	public String getImplementationTitle() {
		return getSpecificationTitle();
	}

	public String getImplementationVersion() {
		return getSpecificationVersion();
	}

	public String getImplementationVendor() {
		return getSpecificationVendor();
	}

	public boolean isSealed() {
		return true;
	}

	public boolean isCompatibleWith(String desired) throws NumberFormatException {
		return Objects.equals(this.getName(), desired);
	}

	public static Package getPackage(String name) {
		return new Package(name);
	}

	public static Package[] getPackages() {
		return new Package[]{};
	}

	public int hashCode() {
		return name.hashCode();
	}

	public String toString() {
		return name;
	}
}
