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

package java.util.zip;

import jtransc.internal.Inflater;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class ZipFile implements ZipConstants, Closeable {
	public static final int OPEN_READ = 0x1;
	public static final int OPEN_DELETE = 0x4;

	private File file;
	private int mode;
	private Charset charset;

	public ZipFile(String name) throws IOException {
		this(new File(name), OPEN_READ);
	}

	public ZipFile(File file, int mode) throws IOException {
		this(file, mode, Charset.forName("UTF-8"));
	}

	public ZipFile(File file) throws ZipException, IOException {
		this(file, OPEN_READ);
	}

	public ZipFile(String name, Charset charset) throws IOException {
		this(new File(name), OPEN_READ, charset);
	}

	public ZipFile(File file, Charset charset) throws IOException {
		this(file, OPEN_READ, charset);
	}

	private RandomAccessFile dis;

	public ZipFile(File file, int mode, Charset charset) throws IOException {
		this.file = file;
		this.mode = mode;
		this.charset = charset;
		this.dis = new RandomAccessFile(file, "r");
		openZip(file);
	}

	private boolean hasMore() throws IOException {
		return dis.getFilePointer() < dis.length();
	}

	private short readShort() throws IOException {
		return Short.reverseBytes(dis.readShort());
	}

	private int readUnsignedShort() throws IOException {
		return Short.reverseBytes(dis.readShort()) & 0xFFFF;
	}

	private int readInt() throws IOException {
		return Integer.reverseBytes(dis.readInt());
	}

	private byte[] readBytes(int count) throws IOException {
		byte[] out = new byte[count];
		dis.read(out);
		return out;
	}

	private String readString(int count) throws IOException {
		if (count == 0) return null;
		return new String(readBytes(count), "UTF-8");
	}

	private String comment = null;
	private ArrayList<ZipEntry> entries = new ArrayList<>();
	private HashMap<String, ZipEntry> entriesByName = new HashMap<>();
	private HashMap<String, ZipExtra> extrasByName = new HashMap<>();

	@SuppressWarnings("deprecation")
	private static long dosToJavaTime(int dtime) {
		return new Date(
			(((dtime >>> 25) & 0x7f) + 80),
			(((dtime >>> 21) & 0x0f) - 1),
			((dtime >>> 16) & 0x1f),
			((dtime >>> 11) & 0x1f),
			((dtime >>> 5) & 0x3f),
			((dtime << 1) & 0x3e)
		).getTime();
	}

	private class ZipExtra {
		long offset;
	}

	//@HaxeMethodBody("haxe.zip.Reader.readZip();")
	@SuppressWarnings("unused")
	private void openZip(File file) throws IOException {
		while (hasMore()) {
			int MAGIC = readUnsignedShort();
			if (MAGIC != 0x4b50) throw new RuntimeException("Not a ZIP file. Magic found: " + MAGIC);

			switch (readUnsignedShort()) {
				case 0x0201: // Central directory file header
				{
					int version = readUnsignedShort();
					int minVer = readUnsignedShort();
					int flags = readUnsignedShort();
					int compressionMethod = readUnsignedShort();
					int lastModDateTime = readInt();
					int crc32 = readInt();
					int compressedSize = readInt();
					int uncompressedSize = readInt();
					int fileNameLength = readUnsignedShort();
					int extraLength = readUnsignedShort();
					int fileCommentLength = readUnsignedShort();
					int diskStart = readUnsignedShort();
					int internalAttributes = readUnsignedShort();
					int externalAttributes = readInt();
					int relativeOffsetOfLocalFileHeader = readInt();
					String fileName = readString(fileNameLength);
					byte[] extraBytes = readBytes(extraLength);
					String fileComment = readString(extraLength);

					//System.out.println("AA:" + relativeOffsetOfLocalFileHeader);

					ZipEntry entry = new ZipEntry();
					entry.name = fileName;
					entry.csize = compressedSize;
					entry.size = uncompressedSize;
					entry.crc = crc32;
					entry.extra = extraBytes;
					entry.method = compressionMethod;
					entry.flag = flags;
					entry.comment = fileComment;

					entries.add(entry);
					entriesByName.put(entry.getName(), entry);
				}
				break;
				case 0x0403: // Local file header
				{
					int minVer = readUnsignedShort();
					int flags = readUnsignedShort();
					int compressionMethod = readUnsignedShort();
					int lastModTime = readUnsignedShort();
					int lastModDate = readUnsignedShort();
					int crc32 = readInt();
					int compressedSize = readInt();
					int uncompressedSize = readInt();
					int fileNameLength = readUnsignedShort();
					int extraLength = readUnsignedShort();
					String fileName = readString(fileNameLength);
					byte[] extraBytes = readBytes(extraLength);
					long dataPos = dis.getFilePointer();
					dis.skipBytes(compressedSize);

					ZipExtra extra = new ZipExtra();
					extra.offset = dataPos;
					extrasByName.put(fileName, extra);
				}
				break;
				case 0x0605: // End of central directory record (EOCD)
				{
					int numberOfThisDisk = readUnsignedShort();
					int diskCentralDirectory = readUnsignedShort();
					int centralDirectoryRecordCount = readUnsignedShort();
					int totalNumberOfCentralDirectoryRecords = readUnsignedShort();
					int sizeOfCentralDirectoryBytes = readInt();
					int offsetOfStartOfCentralDirectoryRelativeToStartOfArchive = readInt();
					int commentLength = readUnsignedShort();
					byte[] comment = readBytes(commentLength);
					this.comment = (commentLength > 0) ? new String(comment, "UTF-8") : null;
				}
				break;
				case 0x0807: // Data descriptor
				{
					int crc32 = readInt();
					int compressedSize = readInt();
					int uncompressedSize = readInt();
				}
				break;
			}
		}
	}

	public String getComment() {
		return comment;
	}

	public ZipEntry getEntry(String name) {
		return entriesByName.get(name);
	}

	private byte[] getCompressedBytes(ZipEntry entry) throws IOException {
		// @TODO: Create slice stream!
		ZipExtra extra = extrasByName.get(entry.getName());
		byte[] compressedData = new byte[(int) entry.csize];

		dis.seek(extra.offset);
		dis.read(compressedData);
		return compressedData;
	}

	private InputStream getCompressedInputStream(ZipEntry entry) throws IOException {
		return new ByteArrayInputStream(getCompressedBytes(entry));
	}

	//@HaxeMethodBody("trace(p0.getBytes().length); trace(p0.getBytes()); var bytes = haxe.zip.Uncompress.run(p0.getBytes(), -15); trace(bytes.length); trace(bytes); return HaxeByteArray.fromBytes(bytes);")
	/*
	@HaxeMethodBody(
		"var u = new haxe.zip.Uncompress(-15);" +
			"var src = p0.getBytes();" +
			"var dst = haxe.io.Bytes.alloc(p1);" +
			"u.execute(src, 0, dst, 0);" +
			"u.close();" +
			"return HaxeByteArray.fromBytes(dst);"
	)
	native static private byte[] deflate(byte[] data, int outputSize);
	*/

	static private byte[] deflate(byte[] data, int outputSize) throws IOException, DataFormatException {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ByteArrayOutputStream bos = new ByteArrayOutputStream(outputSize);
		Inflater inflater = new Inflater(bis, bos);
		//inflater.
		return bos.toByteArray();
	}

	static private final int METHOD_STORED = 0;
	static private final int METHOD_DEFLATED = 8;

	public InputStream getInputStream(ZipEntry entry) throws IOException {
		try {
			switch (entry.method) {
				case METHOD_STORED: // stored
					return getCompressedInputStream(entry);
				case METHOD_DEFLATED: // deflated
					return new ByteArrayInputStream(deflate(getCompressedBytes(entry), (int) entry.size));
			}
		} catch (DataFormatException dfe) {
			throw new IOException(dfe);
		}
		throw new RuntimeException("Not supported method " + entry.method + "!");
	}

	public String getName() {
		return file.getAbsolutePath();
	}

	public Enumeration<? extends ZipEntry> entries() {
		return new Vector<>(entries).elements();
	}

	public int size() {
		return entries.size();
	}

	public void close() throws IOException {
		dis.close();
	}

	protected void finalize() throws IOException {
		close();
	}
}
