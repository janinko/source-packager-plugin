package eu.janinko.test.jenkins;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Project;
import hudson.model.Run.RunnerAbortedException;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class BSPBuildWrapper extends BuildWrapper {
	public static final String COMMANDFILE="SP-build-Commands";
	public static final String SOURCESFILE="SP-sources.zip";
	
	
	
	@DataBoundConstructor
	public BSPBuildWrapper(){
	}

	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		FilePath commands = build.getWorkspace().child(COMMANDFILE);
		if(commands.exists()) commands.delete();
		commands.touch(0);
		
		List<Builder> builders = ((Project)build.getProject()).getBuilders();
		for(Builder b : builders){
			if(b instanceof Shell){
				printShellScript((Shell) b, commands);
			}
		}

		BSPDirScanner scanner = new BSPDirScanner();
		for(String path : DESCRIPTOR.ignorelists.split("\n")){
			try{
				scanner.readIgnoreFromPath(build.getWorkspace(), path);
			}catch (IOException ex) {
				Logger.getLogger(BSPBuildWrapper.class.getName()).log(Level.SEVERE, "failed to read file " + path, ex);
			} catch (InterruptedException ex){
				Logger.getLogger(BSPBuildWrapper.class.getName()).log(Level.SEVERE, "failed to read file " + path, ex);
			}
		}
		
		build.getWorkspace().zip(build.getWorkspace().child(SOURCESFILE).write(),scanner);
		return new Environment() {};
	}
	
	
	
	private void printShellScript(Shell sh, FilePath commands) throws IOException{
		StringBuilder sb = new StringBuilder(commands.readToString());
		sb.append("\nShell script:\n######## START SCRIPT ########\n");
		sb.append(sh.getCommand());
		sb.append("\n########  END SCRIPT  ########\n\n");
		try {
			commands.write(sb.toString(), null);
		} catch (InterruptedException ex) {
			Logger.getLogger(BSPBuildWrapper.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public Launcher decorateLauncher(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException, RunnerAbortedException {
		return new BSPLauncher(launcher, build);
	}
 
	@Override
    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }
	
	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
	
    public static class DescriptorImpl extends BuildWrapperDescriptor {
		String ignore;
		String ignoreall;
		String ignorelists;

        public DescriptorImpl() {
            super(BSPBuildWrapper.class);
            load();
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Source packager";
        }
		
		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			ignore = formData.getString("ignore");
			ignoreall = formData.getString("ignoreall");
			ignorelists = formData.getString("ignorelists");
            save();
            return super.configure(req,formData);
        }
    }

	
}

