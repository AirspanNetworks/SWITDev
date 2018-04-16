package Utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class FTPServer extends FileServer {
	private int port = 21;
	protected FTPClient ftpClient;

	@Override
	public void init() throws Exception {
		super.init();
		super.setProtocolType("FTP");
		ftpClient = new FTPClient();
	}

	public void close() {
		try {
			disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.close();
	}

	@Override
	public void connect() {

		try {
			ftpClient.connect(ipAddress, port);
			ftpClient.login(username, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void disconnect() {
		try {
			ftpClient.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fetches file from remote url (of the ftp server directory) and copies it
	 * to destination folder.
	 * 
	 * @param remoteFileName
	 * @param localName
	 * @throws IOException
	 */
	@Override
	public boolean getFile(String remoteFileName, String localName) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(localName);
			return ftpClient.retrieveFile(remoteFileName, fos);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();				
			}
		}
		return false;
	}

	@Override
	public ArrayList<String> listFiles(String path) {
		FTPFile[] files;
		try {
			if (path.isEmpty())
				files = ftpClient.listFiles();
			else
				files = ftpClient.listFiles(path);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		ArrayList<String> fileNames = new ArrayList<>();
		int i = 0;
		for (FTPFile file : files) {
			file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION, true);
			fileNames.add(files[i].getName());
			i++;
		}
		return fileNames;
	}

	/**
	 * Puts file source to url (The url of the ftp server) the directory of the
	 * ftp server.
	 * 
	 * @param localfileName
	 * @param remoteName
	 * @throws IOException
	 */
	@Override
	public boolean putFile(String localfileName, String remoteName) {
		FileInputStream fis = null;
		report.report("Put " + localfileName + " in " + remoteName);
		try {
			fis = new FileInputStream(localfileName);
			return ftpClient.storeFile(remoteName, fis);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				report.report("Failed to put " + localfileName + " in " + remoteName);
				fis.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Rename a file in remote machine. The path of the file, relates to the ftp
	 * server directory. url - The url of the ftp server directory - The
	 * directory of the ftp server.
	 * 
	 * @param from
	 * @param to
	 * @throws IOException
	 */
	@Override
	public boolean moveFile(String from, String to) {
		report.report("Move file from:" + from + " to:" + to);
		try {
			return ftpClient.rename(from, to);
		} catch (IOException e) {
			report.report("Failed to move file from:" + from + " to:" + to);
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Deletes a file from remote machine. The path of the file, relates to the
	 * ftp server directory. url - The url of the ftp server directory - The
	 * directory of the ftp server.
	 * 
	 * @param pathname
	 * @throws IOException
	 */
	@Override
	public boolean deleteFile(String pathname) {
		boolean isDeleted;
		report.report("Delete From FTP: " + pathname);
		try {
			isDeleted = ftpClient.deleteFile(pathname);
			return isDeleted;
		} catch (IOException e) {
			report.report("Failed to delete From FTP: " + pathname);
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * makes a new directory url (The url of the ftp server).
	 * 
	 * @param newDir
	 * @throws Exception
	 */
	@Override
	public boolean makeDirectory(String newDir) {
		report.report("Make directory " + newDir);
		try {
			return ftpClient.makeDirectory(newDir);
		} catch (IOException e) {
			report.report("Failed to make directory " + newDir);
			e.printStackTrace();
			return false;
		}
	}

	public void changeToBinary() throws IOException {
		ftpClient.setFileType(org.apache.commons.net.ftp.FTPClient.BINARY_FILE_TYPE);
	}

	public void changeToAscii() throws IOException {
		ftpClient.setFileType(org.apache.commons.net.ftp.FTPClient.ASCII_FILE_TYPE);
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

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
