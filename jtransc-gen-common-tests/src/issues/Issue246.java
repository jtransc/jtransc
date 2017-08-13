package issues;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import java.awt.*;
import java.net.ServerSocket;
import java.nio.MappedByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Issue246 {
	static public void main(String[] args) {
		// Can't find class org.lwjgl.opengles.GLESCapabilities
		System.out.println("Issue246.main (Missing classes for pure libgdx #246)");
		desktop(null);
		functional(null, null, null);
		mappedByteBuffer(null);
		soundSampled(null, null, null, null);
		socket(null, null);
		nioFile(null, null, null, null, null, null, null, null, null, null, null, null, null);
		//Desktop.getDesktop();
		//Path path = Paths.get("/");
	}

	static private void desktop(
		Desktop desktop
	) {
	}

	static private void functional(
		BiPredicate p,
		Consumer c,
		Stream s
	) {
	}

	static private void mappedByteBuffer(
		MappedByteBuffer p
	) {
	}

	static private void soundSampled(
		TargetDataLine tdl,
		AudioFormat.Encoding afe,
		AudioFormat af,
		AudioSystem as
	) {
	}

	static private void socket(
		ServerSocket ss,
		SocketChannel sc
	) {

	}

	static private void nioFile(
		FileSystem fs,
		StandardCopyOption sco,
		PosixFilePermission pfp,
		DirectoryStream ds,
		DirectoryStream.Filter filter,
		FileStore fst,
		FileAttributeView fav,
		BasicFileAttributes bfa,
		UserPrincipal up,
		FileTime ft,
		FileVisitOption fvo,
		FileVisitor fv,
		Paths paths
	) {
	}
}
