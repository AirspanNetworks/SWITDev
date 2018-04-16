package Netspan.NBI_15_5;

import java.util.HashMap;

import EnodeB.EnodeB;
import Netspan.API.Enums.NetworkElementStatus;
import Netspan.Profiles.MultiCellParameters;
import Utils.FileServer;

public interface Netspan_15_5_abilities {
	public boolean getMultiCellProfile(EnodeB node, String cloneFromName);
	public boolean getNeighborManagementProfile(EnodeB node, String cloneFromName);
	public boolean isFileServerExists(String fileServerName);
	public boolean createFileServer(FileServer fileServer);
	public MultiCellParameters getMultiCellProfileObject(EnodeB node, String profileName);
	public HashMap<String, NetworkElementStatus> getMMEStatuses(EnodeB enb);
}
