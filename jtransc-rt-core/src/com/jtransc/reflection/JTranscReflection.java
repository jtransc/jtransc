package com.jtransc.reflection;

import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JTranscReflection {
	@HaxeMethodBody("return HaxeNatives.strArray(R.getAllClasses());")
	static public String[] getAllClasses() {
		return new ClasspathScanner().getAllClasses();
	}

	static private class ClasspathScanner {
		private static final String CLASS_FILE_EXTENSION = ".class";
		private static final String JAR_FILE_EXTENSION = ".jar";

		public String[] getAllClasses() {
			final ClassLoader classLoader = getClass().getClassLoader();
			try {
				final Enumeration<URL> resources = classLoader.getResources("");
				final Queue<DepthFile> filesWithDepthsToProcess = new LinkedList<DepthFile>();
				while (resources.hasMoreElements()) {
					try {
						filesWithDepthsToProcess.add(new DepthFile(0, toFile(resources.nextElement())));
					} catch (final Exception uriSyntaxException) {
						// Will throw an exception for non-hierarchical files. Ignored.
					}
				}
				final Set<String> classNames = new HashSet<String>();
				if (filesWithDepthsToProcess.isEmpty()) {
					extractFromJar(classLoader, classNames);
				}
				extractFromBinaries(filesWithDepthsToProcess, classNames);
				return classNames.toArray(new String[classNames.size()]);
			} catch (final Exception exception) {
				throw new RuntimeException("Unable to scan classpath.", exception);
			}
		}

		private static void extractFromBinaries(final Queue<DepthFile> filesWithDepthsToProcess,
												final Set<String> classNames) throws Exception {
			while (!filesWithDepthsToProcess.isEmpty()) {
				final DepthFile classPathFileWithDepth = filesWithDepthsToProcess.poll();
				final File classPathFile = classPathFileWithDepth.file;
				final int depth = classPathFileWithDepth.depth;
				if (classPathFile.isDirectory()) {
					addAllChildren(filesWithDepthsToProcess, classPathFile, depth);
				} else {
					final String className = getBinaryClassName(classPathFile, depth);
					if (isNotPackageInfo(className)) {
						classNames.add(className);
					}
				}
			}
		}

		private static boolean isNotPackageInfo(final String className) {
			return className.indexOf('-') < 0;
		}

		private static File toFile(final URL url) throws URISyntaxException {
			return new File(url.toURI()).getAbsoluteFile();
		}

		private static void addAllChildren(final Queue<DepthFile> rootFiles, final File classPathFile, int depth) {
			depth++;
			for (final File file : classPathFile.listFiles()) {
				if (file.isDirectory() || file.getName().endsWith(CLASS_FILE_EXTENSION)) {
					rootFiles.add(new DepthFile(depth, file));
				}
			}
		}

		private static String getBinaryClassName(final File classPathFile, final int depth) {
			final String[] classFolders = classPathFile.getPath().split(File.separator);
			final StringBuilder builder = new StringBuilder();
			for (int folderIndex = classFolders.length - depth; folderIndex < classFolders.length - 1; folderIndex++) {
				if (builder.length() > 0) {
					builder.append('.');
				}
				builder.append(classFolders[folderIndex]);
			}
			final String classFileName = classFolders[classFolders.length - 1];
			builder.append('.').append(classFileName.substring(0, classFileName.length() - CLASS_FILE_EXTENSION.length()));
			return builder.toString();
		}

		private static void extractFromJar(final ClassLoader classLoader, final Set<String> classNames) throws Exception {
			final List<JarFile> filesToProcess = getJarFilesToProcess();
			for (final JarFile jarFile : filesToProcess) {
				final Enumeration<JarEntry> entries = jarFile.entries();
				while (entries.hasMoreElements()) {
					final JarEntry entry = entries.nextElement();
					processEntry(entry, classNames);
				}
			}
		}

		private static List<JarFile> getJarFilesToProcess() throws URISyntaxException, IOException {
			final List<JarFile> filesToProcess = new ArrayList<JarFile>();
			final File jarDirectory = new File(ClassLoader.getSystemClassLoader().getResource(".").toURI());
			for (final File file : jarDirectory.listFiles()) {
				if (file.getName().endsWith(JAR_FILE_EXTENSION)) {
					filesToProcess.add(new JarFile(file));
				}
			}
			return filesToProcess;
		}

		private static void processEntry(final JarEntry entry, final Set<String> classNames) throws Exception {
			if (!entry.isDirectory()) {
				final String entryName = entry.getName();
				if (entryName.endsWith(CLASS_FILE_EXTENSION) && !isNotPackageInfo(entryName)) {
					classNames.add(jarEntryToClassName(entryName));
				}
			}
		}

		private static String jarEntryToClassName(final String entryName) {
			return entryName.substring(0, entryName.length() - CLASS_FILE_EXTENSION.length()).replace(File.separatorChar,
				'.');
		}

		private static class DepthFile {
			private final int depth;
			private final File file;

			public DepthFile(final int depth, final File file) {
				this.depth = depth;
				this.file = file;
			}
		}
	}
}
