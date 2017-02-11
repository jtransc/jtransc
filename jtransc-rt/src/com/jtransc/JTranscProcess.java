package com.jtransc;

import java.util.List;
import java.util.Map;

abstract public class JTranscProcess extends Process {
	public abstract Process start(List<String> command, Map<String, String> environment, String startDirectory, ProcessBuilder.Redirect stdin, ProcessBuilder.Redirect stdout, ProcessBuilder.Redirect stderr, boolean redirectErrorStream);

	protected String buildCommands(String[] cmds) {
		String out = "";
		// @TODO: Check espacing works fine.
		for (String cmd : cmds) {
			out += " \"" + cmd.replace("\"", "\\\"") + "\"";
		}
		return out;
	}
}