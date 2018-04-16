package Netspan.Profiles;

import java.util.ArrayList;

import Netspan.EnbProfiles;
import Netspan.API.Enums.EnabledDisabledStates;
import Netspan.API.Enums.NrtHoTypes;

public class NeighbourManagementParameters implements INetspanProfile{
	public String profileName;
	public NrtDefaultConfig nrtDefaultConfig;
	public NrtBandList nrtBandList;
	public NrtEarfcnList nrtEarfcnList;
	public HomeEnbDefaultConfig homeEnbDefaultConfig;
	public HomeEnbBandList homeEnbBandList;
	public HomeEnbEarfcnList homeEnbEarfcnList;
	
	public class NrtDefaultConfig{
		public ArrayList<NrtPci> NrtDefaultConfig;
		public NrtDefaultConfig(){
			NrtDefaultConfig = new ArrayList<NrtPci>();
		}
	}
	
	public class NrtBandList{
		public ArrayList<NrtBand> NrtBandList;
		public NrtBandList(){
			NrtBandList = new ArrayList<NrtBand>();
		}
	}
	
	public class NrtEarfcnList{
		public ArrayList<NrtEarfcn> NrtEarfcnList;
		public NrtEarfcnList(){
			NrtEarfcnList = new ArrayList<NrtEarfcn>();
		}
	}

	public class NrtBand{
		public int band;
		public ArrayList<NrtPci> pciList;
		public NrtBand(){
			pciList = new ArrayList<NrtPci>();
		}
	}
	
	public class NrtEarfcn{
		public int earfcn;
		public ArrayList<NrtPci> pciList;
		public NrtEarfcn(){
			pciList = new ArrayList<NrtPci>();
		}
	}
	
	public class NrtPci{
		public int pciStart;
		public int pciEnd;
		public EnabledDisabledStates allowX2;
		public NrtHoTypes hoType;
	}
	
	public class HomeEnbDefaultConfig{
		public ArrayList<HomeEnbPci> HomeEnbDefaultConfig;
		public HomeEnbDefaultConfig(){
			HomeEnbDefaultConfig = new ArrayList<HomeEnbPci>();
		}
	}
	
	public class HomeEnbBandList{
		public ArrayList<HomeEnbBand> HomeEnbBandList;
		public HomeEnbBandList(){
			HomeEnbBandList = new ArrayList<HomeEnbBand>();
		}
	}
	
	public class HomeEnbEarfcnList{
		public ArrayList<HomeEnbEarfcn> HomeEnbEarfcnList;
		public HomeEnbEarfcnList(){
			HomeEnbEarfcnList = new ArrayList<HomeEnbEarfcn>();
		}
	}

	public class HomeEnbBand{
		public int band;
		public ArrayList<HomeEnbPci> pciList;
		public HomeEnbBand(){
			pciList = new ArrayList<HomeEnbPci>();
		}
	}
	
	public class HomeEnbEarfcn{
		public int earfcn;
		public ArrayList<HomeEnbPci> pciList;
		public HomeEnbEarfcn(){
			pciList = new ArrayList<HomeEnbPci>();
		}
	}
	
	public class HomeEnbPci{
		public int pciStart;
		public int pciEnd;
	}

	@Override
	public EnbProfiles getType() {
		return EnbProfiles.Neighbour_Management_Profile;
	}

	@Override
	public String getProfileName() {
		return  this.profileName;
	}

	@Override
	public void setProfileName(String profileName) {
		 this.profileName = profileName;
		
	}
}
