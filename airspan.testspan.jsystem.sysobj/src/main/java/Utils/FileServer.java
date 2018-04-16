package Utils;

import java.io.IOException;
import java.util.ArrayList;

import jsystem.framework.system.SystemObjectImpl;

public abstract class FileServer extends SystemObjectImpl {
	protected String name;
	protected String ipAddress;
	protected String username;
	protected String password;
	protected String protocolType;

	public void init() throws Exception {
		super.init();
	}

	public abstract void connect();

	public abstract void disconnect();

	/**
	 * Fetches file from remote url (of the ftp server directory) and copies it
	 * to destination folder.
	 * 
	 * @param remoteFileName
	 * @param localName
	 * @return
	 * @throws IOException
	 */
	public abstract boolean getFile(String remoteFileName, String localName);

	/**
	 * Puts file source to url (The url of the ftp server) the directory of the
	 * ftp server.
	 * 
	 * @param localfileName
	 * @param remoteName
	 * @return
	 * @throws IOException
	 */

	public abstract boolean putFile(String localfileName, String remoteName);

	/**
	 * Rename a file in remote machine. The path of the file, relates to the ftp
	 * server directory. url - The url of the ftp server directory - The
	 * directory of the ftp server.
	 * 
	 * @param from
	 * @param to
	 * @return
	 * @throws IOException
	 */

	public abstract boolean moveFile(String from, String to);

	/**
	 * Deletes a file from remote machine. The path of the file, relates to the
	 * ftp server directory. url - The url of the ftp server directory - The
	 * directory of the ftp server.
	 * 
	 * @param pathname
	 * @return
	 * @throws IOException
	 */
	public abstract boolean deleteFile(String pathname);
	
	/**
	 * makes a new directory url (The url of the ftp server).
	 * 
	 * @param newDir
	 * @return
	 * @throws Exception
	 */
	public abstract boolean makeDirectory(String newDir);
	
	public abstract ArrayList<String> listFiles(String path);

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(String protocolType) {
		this.protocolType = protocolType;
	}

}
