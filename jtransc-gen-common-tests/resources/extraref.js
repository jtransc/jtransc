function _getBufferArray(buffer) {
	return {% SMETHOD java.nio.internal.BufferInternalUtils:getByteBufferByteArray %}(buffer);
}
