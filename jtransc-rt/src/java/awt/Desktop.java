/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package java.awt;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class Desktop {
	public enum Action {
		OPEN,
		EDIT,
		PRINT,
		MAIL,
		BROWSE
	}

	static private Desktop instance;

	private Desktop() {

	}

	public static synchronized Desktop getDesktop() {
		if (instance == null) {
			instance = new Desktop();
		}
		return instance;
	}

	native public static boolean isDesktopSupported();

	native public boolean isSupported(Action action);

	native public void open(File file) throws IOException;

	native public void edit(File file) throws IOException;

	native public void print(File file) throws IOException;

	native public void browse(URI uri) throws IOException;

	native public void mail() throws IOException;

	native public void mail(URI mailtoURI) throws IOException;
}
