package Netspan.NBI_15_5;

import java.util.HashMap;

import EnodeB.EnodeB;
import Netspan.API.Enums.NetworkElementStatus;
import Netspan.NBI_15_2.Netspan_15_2_abilities;
import Netspan.Profiles.MultiCellParameters;
import Utils.FileServer;

public interface Netspan_15_5_abilities extends Netspan_15_2_abilities {
	boolean getMultiCellProfile(EnodeB node, String cloneFromName);
	boolean getNeighborManagementProfile(EnodeB node, String cloneFromName);
	boolean isFileServerExists(String fileServerName);
	boolean createFileServer(FileServer fileServer);
	MultiCellParameters getMultiCellProfileObject(EnodeB node, String profileName);
	HashMap<String, NetworkElementStatus> getMMEStatuses(EnodeB enb);
}
