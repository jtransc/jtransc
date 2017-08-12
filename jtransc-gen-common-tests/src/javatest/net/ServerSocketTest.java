package javatest.net;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerSocketTest {
	static public void main(String[] args) {
		try {
			ServerSocket serverSocket = new ServerSocket();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
