package com.jtransc.internal;

/*
 * Fast DEFLATE implementation
 *
 * Copyright (c) 2014 Project Nayuki
 * All rights reserved. Contact Nayuki for licensing.
 * https://www.nayuki.io/page/simple-deflate-implementation
 */

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.DataFormatException;

public final class Inflater {

	/* Decompressor streams and state */

	// Main I/O streams
	private InputStream input;    // Only used by readBits() and readBytes(); never use this directly
	private OutputStream output;  // Only used by writeOutputBuffer()

	// Input byte buffer and bit buffer
	private static final int INPUT_BUFFER_SIZE = 16 * 1024;
	private byte[] inputBuffer;       // Can have any positive length
	private int inputBufferFilled;    // 0 <= inputBufferFilled <= inputBuffer.length, or -1 to indicate end of stream
	private int inputBufferIndex;     // 0 <= inputBufferIndex <= max(inputBufferFilled, 0)
	private long inputNextBits;       // Unused high-order bits must be zero. Must only contain data from the current input buffer (i.e. not past buffers) so that the bits after the end of the DEFLATE stream can be unread
	private int inputNextBitsLength;  // Always in the range [0, 63] before and after every call to readBits()

	// Circular dictionary
	private static final int DICTIONARY_SIZE = 32 * 1024;  // Not actually configurable - will run without crashing, but decompression behavior would be incompatible with DEFLATE
	private static final int DICTIONARY_SIZE_MASK = DICTIONARY_SIZE - 1;  // This is why DICTIONARY_SIZE must be a power of 2
	private byte[] dictionary;    // Length equal to DICTIONARY_SIZE
	private int dictionaryIndex;  // Index of next byte to write. Always 0 <= dictionaryIndex < dictionary.length

	// Output buffer
	private static final int OUTPUT_BUFFER_SIZE = 64 * 1024;  // Must be at least 65535 to accommodate uncompressed blocks (configurable)
	private byte[] tempOutputBuffer;  // Only used within decompressUncompressedBlock() and decompressHuffmanBlock(); invalid data outside of these function calls

	// Output statistics
	private long outputLength;
	private int outputCrc32;



	/* Public main methods */

	public Inflater(InputStream in, OutputStream out) throws IOException, DataFormatException {
		if (in == null || out == null)
			throw new NullPointerException();
		if (!in.markSupported())
			throw new IllegalArgumentException("Input stream needs to be markable");

		// Initialize all instance fields
		input = in;
		output = out;

		inputBuffer = new byte[INPUT_BUFFER_SIZE];
		inputBufferFilled = 0;
		inputBufferIndex = 0;
		inputNextBits = 0;
		inputNextBitsLength = 0;

		assert DICTIONARY_SIZE > 0 && Integer.bitCount(DICTIONARY_SIZE) == 1;  // Is power of 2
		dictionary = new byte[DICTIONARY_SIZE];
		dictionaryIndex = 0;

		assert OUTPUT_BUFFER_SIZE >= 65535;
		tempOutputBuffer = new byte[OUTPUT_BUFFER_SIZE];

		outputLength = 0;
		outputCrc32 = 0xFFFFFFFF;

		// Start decompressing immediately
		decompressStream();
	}


	public long getLength() {
		return outputLength;
	}


	public int getCrc32() {
		return ~outputCrc32;
	}


	/* Main decompression methods */

	private void decompressStream() throws IOException, DataFormatException {
		boolean isFinal;
		do {
			isFinal = readBits(1) == 1;
			int type = readBits(2);

			if (type == 0)
				decompressUncompressedBlock();
			else if (type == 1)
				decompressHuffmanBlock(FIXED_LITERAL_LENGTH_CODE_TREE, FIXED_DISTANCE_CODE_TREE);
			else if (type == 2) {
				short[][] codeTrees = decodeHuffmanCodes();
				decompressHuffmanBlock(codeTrees[0], codeTrees[1]);
			} else
				throw new DataFormatException("Invalid block type");

		} while (!isFinal);

		// Adjust over-consumed bytes
		input.reset();
		int skip = inputBufferIndex - inputNextBitsLength / 8;  // Note: A partial byte is considered to be consumed
		assert skip >= 0;
		while (skip > 0) {
			long n = input.skip(skip);
			if (n <= 0)
				throw new EOFException();
			skip -= n;
		}

		// Detatch from streams and free buffers
		input = null;
		output = null;
		inputBuffer = null;
		inputBufferFilled = 0;
		inputBufferIndex = 0;
		inputNextBits = 0;
		inputNextBitsLength = 0;
		dictionary = null;
		dictionaryIndex = 0;
		tempOutputBuffer = null;
	}


	private void decompressUncompressedBlock() throws IOException, DataFormatException {
		// Set up shorter variable name
		byte[] buf = tempOutputBuffer;

		// Read and check length
		readBytes(buf, 4);
		int len = (buf[0] & 0xFF) | (buf[1] & 0xFF) << 8;
		int nlen = (buf[2] & 0xFF) | (buf[3] & 0xFF) << 8;
		if ((len ^ 0xFFFF) != nlen)
			throw new DataFormatException("Invalid length in uncompressed block");
		assert 0 <= len && len <= 65535;

		// Read verbatim bytes, append to dictionary, write to output
		assert 0 <= dictionaryIndex && dictionaryIndex < DICTIONARY_SIZE;
		readBytes(buf, len);
		for (int off = 0; off < len; ) {
			int n = Math.min(DICTIONARY_SIZE - dictionaryIndex, len - off);
			System.arraycopy(buf, off, dictionary, dictionaryIndex, n);
			dictionaryIndex = (dictionaryIndex + n) & DICTIONARY_SIZE_MASK;
			off += n;
		}
		writeOutputBuffer(len);
	}


	private void decompressHuffmanBlock(short[] litLenCodeTree, short[] distCodeTree) throws IOException, DataFormatException {
		// Set up shorter variable names
		assert litLenCodeTree != null;
		byte[] buf = tempOutputBuffer;
		int bufIndex = 0;
		byte[] dict = dictionary;
		int dictIndex = dictionaryIndex;
		assert 0 <= dictIndex && dictIndex < DICTIONARY_SIZE;

		short[] litLenCodeTable = codeTreeToCodeTable(litLenCodeTree);
		while (true) {
			// Decode the next symbol from the literal/run Huffman code
			// This first block of code is equivalent to: int sym = decodeSymbol(litLenCodeTree);
			int bits = (int) inputNextBits;
			int count = inputNextBitsLength;
			int node = 0;
			while (true) {
				if (count >= CODE_TABLE_BITS) {  // Fast path using code table
					int temp = litLenCodeTable[bits & ((1 << CODE_TABLE_BITS) - 1)];
					assert temp >= 0;  // No need to mask off sign extension bits
					int consumed = temp >>> 11;
					bits >>>= consumed;
					count -= consumed;
					node = (temp << 21) >> 21;  // Sign extension from 11 bits
					if (node < 0) {  // Is a leaf symbol
						// Simple write-back
						inputNextBits >>>= inputNextBitsLength - count;
						inputNextBitsLength = count;
						break;  // Goto end
					}
				}

				// Modified version of decodeSymbol()
				while (count > 0) {  // Medium path using buffered bits
					node = litLenCodeTree[node + (bits & 1)];
					bits >>>= 1;
					count--;
					if (node < 0)
						break;
				}
				inputNextBits >>>= inputNextBitsLength - count;
				inputNextBitsLength = count;
				while (node >= 0)  // Slow path reading one bit at a time
					node = litLenCodeTree[node + readBits(1)];
				break;  // Not a real loop
			}
			int sym = ~node;
			assert 0 <= sym && sym <= 285;

			if (sym < 256) {  // Literal byte
				dict[dictIndex] = buf[bufIndex] = (byte) sym;
				dictIndex = (dictIndex + 1) & DICTIONARY_SIZE_MASK;
				bufIndex++;
				if (bufIndex == OUTPUT_BUFFER_SIZE) {
					dictionaryIndex = dictIndex;
					writeOutputBuffer(bufIndex);
					bufIndex = 0;
				}

			} else if (sym > 256) {  // Length and distance for copying
				// Decode symbols
				int len = decodeRunLength(sym);
				assert 3 <= len && len <= 258;
				if (distCodeTree == null)
					throw new DataFormatException("Length symbol encountered with empty distance code");
				int distSym = decodeSymbol(distCodeTree);
				assert 0 <= distSym && distSym <= 29;
				int dist = decodeDistance(distSym);
				assert 1 <= dist && dist <= 32768;

				// Ensure sufficient room in buffer
				if (bufIndex + len > OUTPUT_BUFFER_SIZE) {
					dictionaryIndex = dictIndex;
					writeOutputBuffer(bufIndex);
					bufIndex = 0;
				}

				// Copy bytes to output and dictionary
				int dictReadIndex = (dictIndex - dist) & DICTIONARY_SIZE_MASK;
				for (int bufEnd = bufIndex + len; bufIndex < bufEnd; bufIndex++) {
					dict[dictIndex] = buf[bufIndex] = dict[dictReadIndex];
					dictIndex = (dictIndex + 1) & DICTIONARY_SIZE_MASK;
					dictReadIndex = (dictReadIndex + 1) & DICTIONARY_SIZE_MASK;
				}

			} else  // sym == 256, end of block
				break;
		}

		// Save variable, write output buffer
		dictionaryIndex = dictIndex;
		writeOutputBuffer(bufIndex);
	}


	/* Huffman coding methods */

	private short[][] decodeHuffmanCodes() throws IOException, DataFormatException {
		int numLitLenCodes = readBits(5) + 257;  // hlit  + 257
		int numDistCodes = readBits(5) + 1;  // hdist +   1

		int numCodeLenCodes = readBits(4) + 4;  // hclen +   4
		byte[] codeLenCodeLen = new byte[19];
		for (int i = 0; i < numCodeLenCodes; i++)
			codeLenCodeLen[CODE_LENGTH_CODE_ORDER[i]] = (byte) readBits(3);
		short[] codeLenCodeTree = codeLengthsToCodeTree(codeLenCodeLen);

		byte[] codeLens = new byte[numLitLenCodes + numDistCodes];
		byte runVal = -1;
		int runLen = 0;
		for (int i = 0; i < codeLens.length; ) {
			if (runLen > 0) {
				assert runVal != -1;
				codeLens[i] = runVal;
				runLen--;
				i++;
			} else {
				int sym = decodeSymbol(codeLenCodeTree);
				assert 0 <= sym && sym <= 18;
				if (sym < 16) {
					runVal = codeLens[i] = (byte) sym;
					i++;
				} else if (sym == 16) {
					if (runVal == -1)
						throw new DataFormatException("No code length value to copy");
					runLen = readBits(2) + 3;
				} else if (sym == 17) {
					runVal = 0;
					runLen = readBits(3) + 3;
				} else {  // sym == 18
					runVal = 0;
					runLen = readBits(7) + 11;
				}
			}
		}
		if (runLen > 0)
			throw new DataFormatException("Run exceeds number of codes");

		// Create code trees
		byte[] litLenCodeLen = Arrays.copyOf(codeLens, numLitLenCodes);
		short[] litLenCodeTree = codeLengthsToCodeTree(litLenCodeLen);

		byte[] distCodeLen = Arrays.copyOfRange(codeLens, numLitLenCodes, codeLens.length);
		short[] distCodeTree;
		if (distCodeLen.length == 1 && distCodeLen[0] == 0)
			distCodeTree = null;  // Empty distance code; the block shall be all literal symbols
		else {
			// Get statistics for upcoming logic
			int oneCount = 0;
			int otherPositiveCount = 0;
			for (byte x : distCodeLen) {
				if (x == 1)
					oneCount++;
				else if (x > 1)
					otherPositiveCount++;
			}

			// Handle the case where only one distance code is defined
			if (oneCount == 1 && otherPositiveCount == 0) {
				// Add a dummy invalid code to make the Huffman tree complete
				distCodeLen = Arrays.copyOf(distCodeLen, 32);
				distCodeLen[31] = 1;
			}
			distCodeTree = codeLengthsToCodeTree(distCodeLen);
		}

		return new short[][]{litLenCodeTree, distCodeTree};
	}


	private int decodeSymbol(short[] codeTree) throws IOException {
		int node = 0;

		int count = inputNextBitsLength;
		if (count > 0) {  // Medium path using buffered bits
			// Because of this truncation, the code tree depth needs to be no more than 32
			int bits = (int) inputNextBits;
			do {
				node = codeTree[node + (bits & 1)];
				bits >>>= 1;
				count--;
			} while (count > 0 && node >= 0);
			inputNextBits >>>= inputNextBitsLength - count;
			inputNextBitsLength = count;
		}

		while (node >= 0)  // Slow path reading one bit at a time
			node = codeTree[node + readBits(1)];
		return ~node;  // Symbol encoded in bitwise complement
	}


	private int decodeRunLength(int sym) throws IOException, DataFormatException {
		assert 257 <= sym && sym <= 287;
		if (sym <= 264)
			return sym - 254;
		else if (sym <= 284) {
			int n = (sym - 261) >>> 2;  // Number of extra bits to read
			return ((((sym - 1) & 3) | 4) << n) + 3 + readBits(n);
		} else if (sym == 285)
			return 258;
		else
			throw new DataFormatException("Invalid run length symbol: " + sym);
	}


	private int decodeDistance(int sym) throws IOException, DataFormatException {
		assert 0 <= sym && sym < 32;
		if (sym <= 3)
			return sym + 1;
		else if (sym <= 29) {
			int n = (sym >>> 1) - 1;  // Number of extra bits to read
			return (((sym & 1) | 2) << n) + 1 + readBits(n);
		} else
			throw new DataFormatException("Invalid distance symbol: " + sym);
	}


	/*
	 * Converts the given array of symbol code lengths into a canonical code tree.
	 * A symbol code length is either zero (absent from the tree) or a positive integer.
	 *
	 * A code tree is an array of integers, where each pair represents a node.
	 * Each pair is adjacent and starts on an even index. The first element of
	 * the pair represents the left child and the second element represents the
	 * right child. The root node is at index 0. If an element is non-negative,
	 * then it is the index of the child node in the array. Otherwise it is the
	 * bitwise complement of the leaf symbol. This tree is used in decodeSymbol()
	 * and codeTreeToCodeTable(). Not every element of the array needs to be
	 * used, nor do used elements need to be contiguous.
	 *
	 * For example, this Huffman tree:
	 *        o
	 *       / \
	 *      o   \
	 *     / \   \
	 *   'a' 'b' 'c'
	 * is serialized as this array:
	 *   {2, ~'c', ~'a', ~'b'}
	 * because the root is located at index 0 and the other internal node is
	 * located at index 2.
	 */
	private static short[] codeLengthsToCodeTree(byte[] codeLengths) throws DataFormatException {
		final short UNUSED = 0x7000;
		final short OPENING = 0x7001;
		final short OPEN = 0x7002;

		short[] result = new short[(codeLengths.length - 1) * 2];  // Worst-case allocation if all symbols are present
		Arrays.fill(result, UNUSED);
		result[0] = OPEN;
		result[1] = OPEN;
		int allocated = 2;  // Always even in this algorithm

		int maxCodeLen = 0;
		for (int x : codeLengths)
			maxCodeLen = Math.max(x, maxCodeLen);
		assert maxCodeLen <= 15;

		// Allocate Huffman tree nodes according to ascending code lengths
		for (int curCodeLen = 1; curCodeLen <= maxCodeLen; curCodeLen++) {
			// Loop invariant: Each OPEN child slot in the result array has depth curCodeLen

			// Allocate all symbols of current code length to open slots in ascending order
			int resultIndex = 0;
			int symbol = 0;
			middle:
			while (true) {
				// Find next symbol having current code length
				while (symbol < codeLengths.length && codeLengths[symbol] != curCodeLen) {
					assert codeLengths[symbol] >= 0;
					symbol++;
				}
				if (symbol == codeLengths.length)
					break middle;  // No more symbols to process

				// Find next open child slot
				while (resultIndex < result.length && result[resultIndex] != OPEN)
					resultIndex++;
				if (resultIndex == result.length)  // No more slots left; tree over-full
					throw new DataFormatException("This canonical code does not represent a Huffman code tree");

				// Put the symbol in the slot and increment
				result[resultIndex] = (short) ~symbol;
				resultIndex++;
				symbol++;
			}

			// Take all open slots and deepen them by one level
			for (; resultIndex < result.length; resultIndex++) {
				if (result[resultIndex] == OPEN) {
					// Allocate a new node
					assert allocated + 2 <= result.length;
					result[resultIndex] = (short) allocated;
					result[allocated + 0] = OPENING;
					result[allocated + 1] = OPENING;
					allocated += 2;
				}
			}

			// Do post-processing so we don't open slots that were just opened
			for (resultIndex = 0; resultIndex < result.length; resultIndex++) {
				if (result[resultIndex] == OPENING)
					result[resultIndex] = OPEN;
			}
		}

		// Check for under-full tree after all symbols are allocated
		for (int i = 0; i < allocated; i++) {
			if (result[i] == OPEN)
				throw new DataFormatException("This canonical code does not represent a Huffman code tree");
		}

		return result;
	}


	/*
	 * Converts a code tree array into a fast look-up table that consumes up to
	 * CODE_TABLE_BITS at once. Each entry i in the table encodes the result of
	 * decoding starting from the root and consuming the bits of i starting from
	 * the lowest-order bits.
	 *
	 * Each array element encodes (numBitsConsumed << 11) | (node & 0x7FF), where:
	 * - numBitsConsumed is a 4-bit unsigned integer in the range [1, CODE_TABLE_BITS].
	 * - node is an 11-bit signed integer representing either the current node
	 *   (which is a non-negative number) after consuming all the available bits
	 *   from i, or the bitwise complement of the decoded symbol (so it's negative).
	 * Note that each element is a non-negative number.
	 */
	private static short[] codeTreeToCodeTable(short[] codeTree) {
		assert 1 <= CODE_TABLE_BITS && CODE_TABLE_BITS <= 15;
		short[] result = new short[1 << CODE_TABLE_BITS];
		for (int i = 0; i < result.length; i++) {
			// Simulate decodeSymbol() using the bits of i
			int node = 0;
			int consumed = 0;
			do {
				node = codeTree[node + ((i >>> consumed) & 1)];
				consumed++;
			} while (node >= 0 && consumed < CODE_TABLE_BITS);

			assert 1 <= consumed && consumed <= 15;  // 4 bits unsigned
			assert -1024 <= node && node <= 1023;  // 11 bits signed
			result[i] = (short) (consumed << 11 | (node & 0x7FF));
			assert result[i] >= 0;
		}
		return result;
	}

	private static final int CODE_TABLE_BITS = 9;  // Any integer from 1 to 15 is valid; affects speed


	/* I/O methods */

	// Reads the given number of bits from the input stream, return a non-negative integer in bit little endian
	private int readBits(int n) throws IOException {
		assert 1 <= n && n <= 13;  // n = 13 is the maximum used in DEFLATE, but this method is designed to be valid for n <= 31
		assert 0 <= inputNextBitsLength && inputNextBitsLength <= 63;
		assert inputNextBits >>> inputNextBitsLength == 0;  // Ensure high-order bits are clean

		// Ensure there is enough data in the bit buffer
		while (inputNextBitsLength < n) {
			int i = inputBufferIndex;
			byte[] buf = inputBuffer;

			// Fill bit buffer with as many bytes as possible
			int bytes = Math.min((64 - inputNextBitsLength) >>> 3, inputBufferFilled - i);
			long temp;
			if (bytes == 8)  // ~90% hit rate
				temp = (((buf[i] & 0xFF) | (buf[i + 1] & 0xFF) << 8 | (buf[i + 2] & 0xFF) << 16 | buf[i + 3] << 24) & 0xFFFFFFFFL) | (long) ((buf[i + 4] & 0xFF) | (buf[i + 5] & 0xFF) << 8 | (buf[i + 6] & 0xFF) << 16 | buf[i + 7] << 24) << 32;
			else if (bytes == 7)  // ~5% hit rate
				temp = (((buf[i] & 0xFF) | (buf[i + 1] & 0xFF) << 8 | (buf[i + 2] & 0xFF) << 16 | buf[i + 3] << 24) & 0xFFFFFFFFL) | (long) ((buf[i + 4] & 0xFF) | (buf[i + 5] & 0xFF) << 8 | (buf[i + 6] & 0xFF) << 16) << 32;
			else if (bytes == 6)
				temp = (((buf[i] & 0xFF) | (buf[i + 1] & 0xFF) << 8 | (buf[i + 2] & 0xFF) << 16 | buf[i + 3] << 24) & 0xFFFFFFFFL) | (long) ((buf[i + 4] & 0xFF) | (buf[i + 5] & 0xFF) << 8) << 32;
			else if (bytes > 0) {
				// This slower general logic is valid for 1 <= bytes <= 8
				temp = 0;
				for (int j = 0; j < bytes; i++, j++)
					temp |= (buf[i] & 0xFFL) << (j << 3);
			} else if (bytes == 0) {
				// Fill and retry
				fillInputBuffer();
				continue;
			} else if (bytes == -1 && inputBufferFilled == -1)  // Note: fillInputBuffer() sets inputBufferIndex to 0
				throw new EOFException();  // Previous buffer fill hit the end of stream
			else
				throw new AssertionError();  // Impossible state

			inputNextBits |= temp << inputNextBitsLength;
			inputNextBitsLength += bytes << 3;
			inputBufferIndex += bytes;
		}

		// Extract bits to return
		int result = (int) inputNextBits & ((1 << n) - 1);
		inputNextBits >>>= n;
		inputNextBitsLength -= n;
		assert 0 <= inputNextBitsLength && inputNextBitsLength <= 63;  // Recheck invariants
		assert inputNextBits >>> inputNextBitsLength == 0;
		return result;
	}


	// Reads 'len' bytes into the beginning of 'b'. Only used by decompressUncompressedBlock().
	private void readBytes(byte[] b, int len) throws IOException {
		assert b.length >= len;
		assert 0 <= inputNextBitsLength && inputNextBitsLength <= 63;
		assert inputNextBits >>> inputNextBitsLength == 0;

		// Discard remaining partial bits
		int n = inputNextBitsLength & 7;
		inputNextBits >>>= n;
		inputNextBitsLength -= n;
		assert inputNextBitsLength % 8 == 0;

		// Unpack saved bits first
		int off = 0;
		for (; inputNextBitsLength >= 8 && off < len; off++) {
			b[off] = (byte) inputNextBits;
			inputNextBits >>>= 8;
			inputNextBitsLength -= 8;
		}

		// Read/copy from buffer
		assert 0 <= inputBufferIndex && inputBufferIndex <= Math.max(inputBufferFilled, 0);
		while (off < len) {
			if (inputBufferIndex >= inputBufferFilled)
				fillInputBuffer();
			if (inputBufferFilled == -1)
				throw new EOFException();

			n = Math.min(len - off, inputBufferFilled - inputBufferIndex);
			System.arraycopy(inputBuffer, inputBufferIndex, b, off, n);
			inputBufferIndex += n;
			off += n;
		}
	}


	private void fillInputBuffer() throws IOException {
		if (inputBufferFilled == -1)  // Previous fill already hit EOF
			throw new EOFException();
		if (inputBufferIndex < inputBufferFilled)
			throw new AssertionError("Input buffer not fully consumed yet");

		input.mark(inputBuffer.length);  // Acknowledge all previously read bytes
		inputBufferFilled = input.read(inputBuffer);
		inputBufferIndex = 0;
	}


	private void writeOutputBuffer(int len) throws IOException {
		// Write to underlying stream, update total length
		byte[] b = tempOutputBuffer;
		output.write(b, 0, len);
		outputLength += len;
		assert (outputLength & DICTIONARY_SIZE_MASK) == dictionaryIndex;

		// Update CRC-32 hash
		int crc = outputCrc32;
		for (int i = 0; i < len; i++)
			crc = (crc >>> 8) ^ CRC32_XOR_TABLE[(crc ^ b[i]) & 0xFF];
		outputCrc32 = crc;
	}



	/* Static tables */

	private static final int[] CODE_LENGTH_CODE_ORDER = {16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15};

	private static final short[] FIXED_LITERAL_LENGTH_CODE_TREE;
	private static final short[] FIXED_DISTANCE_CODE_TREE;
	private static final int[] CRC32_XOR_TABLE;

	static {
		// Fixed Huffman code trees (for block type 1)
		try {
			byte[] llcodelens = new byte[288];
			Arrays.fill(llcodelens, 0, 144, (byte) 8);
			Arrays.fill(llcodelens, 144, 256, (byte) 9);
			Arrays.fill(llcodelens, 256, 280, (byte) 7);
			Arrays.fill(llcodelens, 280, 288, (byte) 8);
			FIXED_LITERAL_LENGTH_CODE_TREE = codeLengthsToCodeTree(llcodelens);

			byte[] distcodelens = new byte[32];
			Arrays.fill(distcodelens, (byte) 5);
			FIXED_DISTANCE_CODE_TREE = codeLengthsToCodeTree(distcodelens);
		} catch (DataFormatException e) {
			throw new AssertionError(e);
		}

		// CRC-32 table
		CRC32_XOR_TABLE = new int[256];
		final int POLYNOMIAL = 0xEDB88320;
		for (int i = 0; i < 256; i++) {
			int reg = i;
			for (int j = 0; j < 8; j++)
				reg = (reg >>> 1) ^ ((reg & 1) * POLYNOMIAL);
			CRC32_XOR_TABLE[i] = reg;
		}

		// Test if assertions are on
		try {
			assert false;
		} catch (AssertionError e) {
			System.err.println("Assertions are enabled :)");
		}
	}

}