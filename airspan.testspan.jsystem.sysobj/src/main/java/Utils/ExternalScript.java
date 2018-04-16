package Utils;

import java.io.IOException;

import jsystem.framework.system.SystemObjectImpl;

public abstract class ExternalScript extends SystemObjectImpl {
	protected String macroPath;
	protected String folderPath;
	protected Process[] process;
	public MoxaCom[] children;
	
	@Override
	public void init() throws Exception {
		super.init();
		
		if (children != null)
			process = new Process[children.length];
		else
			process = new Process[1];
	}
	
	public abstract void start(boolean justScript) throws IOException;
	
	public void stop() {
		for (int i = 0; i < process.length; i++) {
			process[i].destroy();
		}
	}
	
	public String getMacroPath() {
		return macroPath;
	}

	public void setMacroPath(String macroPath) {
		this.macroPath = macroPath;
	}

	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}
	
}
