/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.janinko.test.jenkins;

import hudson.FilePath;
import hudson.Util;
import hudson.util.DirScanner;
import hudson.util.FileVisitor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.HashSet;

/**
 *
 * @author jbrazdil
 */
public class BSPDirScanner extends DirScanner{
	private static final long serialVersionUID = 1L;
	HashSet<String> ignore;
	HashSet<String> ignoreall;
	
	BSPDirScanner(){
		ignore = new HashSet<String>();
		ignoreall = new HashSet<String>();
		
		for(String s : BSPBuildWrapper.DESCRIPTOR.ignore.split("\n")){
			while(s.endsWith("/")){ s = s.substring(0,s.length()-1);}
			ignore.add(s);
		}
		for(String s : BSPBuildWrapper.DESCRIPTOR.ignoreall.split("\n")){
			while(s.endsWith("/")){ s = s.substring(0,s.length()-1);}
			ignoreall.add(s);
		}
		for(String s : BSPBuildWrapper.DESCRIPTOR.ignorelists.split("\n")){
			while(s.endsWith("/")){ s = s.substring(0,s.length()-1);}
			ignore.add(s);
		}
		
		ignore.add(BSPBuildWrapper.COMMANDFILE);
		ignore.add(BSPBuildWrapper.SOURCESFILE);
	}
	
	/**
	 * Add ignored file from stream (eg: .gitignore). Each file on single line.
	 * @param is Stream with ignored file names.
	 * @throws IOException 
	 */
	public void addIgnoreFromStream(InputStream is) throws IOException{
		BufferedReader br =  new BufferedReader(new InputStreamReader(is));
		String line;
		while((line = br.readLine()) != null){
			while(line.endsWith("/")){ line = line.substring(0,line.length()-1);}
			ignore.add(line);
		}
	}

	/**
	 * Add ignored file from file (eg: .gitignore). Each file on single line.
	 * @param act Base directory.
	 * @param path Path relative to the base directory.
	 * @throws IOException 
	 * @throws InterruptedException
	 */
	public void readIgnoreFromPath(FilePath act, String path) throws IOException, InterruptedException {
		for (String file : path.split("/")) {
			if (!act.child(file).exists()) {
				return;
			}
			act = act.child(file);
		}
		if (!act.isDirectory()) {
			this.addIgnoreFromStream(act.read());
		}
	}

	public boolean accept(String path, String name) {
		String pathname = path + name;
		if(pathname.startsWith("workspace/")) pathname = pathname.substring(10);
		
		//System.out.println("Asked for: " + pathname + " filter? " + ignore.contains(pathname) + " all?" + ignoreall.contains(name));
		return !(ignore.contains(pathname) || ignoreall.contains(name));
	}

	@Override
	public void scan(File dir, FileVisitor visitor) throws IOException {
		scan(dir,visitor,"");
	}
	
	public void scan(File f, FileVisitor visitor, String path) throws IOException {
		if (f.canRead() && accept(path, f.getName())) {
			if (visitor.understandsSymlink()) {
				try {
					String target = Util.resolveSymlink(f);
					if (target != null) {
						visitor.visitSymlink(f, target, path + f.getName());
						return;
					}
				} catch (InterruptedException e) {
					throw (IOException) new InterruptedIOException().initCause(e);
				}
			}
			visitor.visit(f, path + f.getName());
			if (f.isDirectory()) {
				for (File child : f.listFiles()) {
					scan(child, visitor, path + f.getName() + '/');
				}
			}
		}
	}
	
}
