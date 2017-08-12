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

package javax.sound.sampled;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class AudioSystem {
	public static final int NOT_SPECIFIED = -1;

	private AudioSystem() {
	}

	native public static Mixer.Info[] getMixerInfo();

	native public static Mixer getMixer(Mixer.Info info);

	native public static Line.Info[] getSourceLineInfo(Line.Info info);

	native public static Line.Info[] getTargetLineInfo(Line.Info info);

	native public static boolean isLineSupported(Line.Info info);

	native public static Line getLine(Line.Info info) throws LineUnavailableException;

	native public static Clip getClip() throws LineUnavailableException;

	native public static Clip getClip(Mixer.Info mixerInfo) throws LineUnavailableException;

	native public static SourceDataLine getSourceDataLine(AudioFormat format) throws LineUnavailableException;

	native public static SourceDataLine getSourceDataLine(AudioFormat format, Mixer.Info mixerinfo) throws LineUnavailableException;

	native public static TargetDataLine getTargetDataLine(AudioFormat format) throws LineUnavailableException;

	native public static TargetDataLine getTargetDataLine(AudioFormat format, Mixer.Info mixerinfo) throws LineUnavailableException;

	native public static AudioFormat.Encoding[] getTargetEncodings(AudioFormat.Encoding sourceEncoding);

	native public static AudioFormat.Encoding[] getTargetEncodings(AudioFormat sourceFormat);

	native public static boolean isConversionSupported(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat);

	native public static AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding, AudioInputStream sourceStream);

	native public static AudioFormat[] getTargetFormats(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat);

	native public static boolean isConversionSupported(AudioFormat targetFormat, AudioFormat sourceFormat);

	native public static AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream sourceStream);

	native public static AudioFileFormat getAudioFileFormat(InputStream stream) throws UnsupportedAudioFileException, IOException;

	native public static AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException;

	native public static AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException;

	native public static AudioInputStream getAudioInputStream(InputStream stream) throws UnsupportedAudioFileException, IOException;

	native public static AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException;

	native public static AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException;

	native public static AudioFileFormat.Type[] getAudioFileTypes();

	native public static boolean isFileTypeSupported(AudioFileFormat.Type fileType);

	native public static AudioFileFormat.Type[] getAudioFileTypes(AudioInputStream stream);

	native public static boolean isFileTypeSupported(AudioFileFormat.Type fileType, AudioInputStream stream);

	native public static int write(AudioInputStream stream, AudioFileFormat.Type fileType, OutputStream out) throws IOException;

	native public static int write(AudioInputStream stream, AudioFileFormat.Type fileType, File out) throws IOException;
}
