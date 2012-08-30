/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.janinko.test.jenkins;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.AbstractBuild;
import hudson.remoting.Channel;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jbrazdil
 */
class BSPLauncher extends Launcher {
	Launcher parent;
	
	AbstractBuild build;

	public BSPLauncher(Launcher launcher, AbstractBuild build) {
		super(launcher);
		parent = launcher;
		this.build = build;
	}

	@Override
	public Proc launch(Launcher.ProcStarter starter) throws IOException {
		FilePath commands = build.getWorkspace().child(BSPBuildWrapper.COMMANDFILE);
		StringBuilder sb = new StringBuilder(commands.readToString());
		sb.append("\nlaunching command:");
		for(String cmd : starter.cmds()){
			sb.append(" ");
			sb.append(cmd);
		}
		
		sb.append("\nEnvironment:\n######## START Environment ########\n");
		for(String s : starter.envs()){
			sb.append(s);
			sb.append('\n');
		}
		sb.append("########  END Environment  ########\n\n");
		try {
			commands.write(sb.toString(), null);
		} catch (InterruptedException ex) {
			Logger.getLogger(BSPLauncher.class.getName()).log(Level.SEVERE, null, ex);
		}
		return parent.launch(starter);
	}

	@Override
	public Channel launchChannel(String[] cmd, OutputStream out, FilePath workDir, Map<String, String> envVars) throws IOException, InterruptedException {
		return parent.launchChannel(cmd, out, workDir, envVars);
	}

	@Override
	public void kill(Map<String, String> modelEnvVars) throws IOException, InterruptedException {
		parent.kill(modelEnvVars);
	}
	
}
