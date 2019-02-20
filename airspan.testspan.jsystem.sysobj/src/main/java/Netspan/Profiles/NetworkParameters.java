package Netspan.Profiles;

import java.util.ArrayList;
import java.util.List;

import Netspan.EnbProfiles;
import Netspan.API.Enums.EnabledStates;


public class NetworkParameters implements INetspanProfile{
	private String profileName = null;
	private ArrayList<String> MMEIPS=null;
	public ArrayList<Plmn> plmnList= null;
	private CSonConfig cSonConfig = new CSonConfig();
	private EnabledStates backhaulQosAdmin = null;
	private Integer backhaulQosMinReservedForCalls = null;
	private String bhQosBwReservationServerFqdn = null;
	private String bhQosFtpFqdn = null;
	private String bhQosFtpPassword = null;
	private String bhQosFtpUsername = null;
	private List<String> bhQosReservationServerIpAddressList = new ArrayList<String>();
	private EnabledStates etwsEnabled;
	private Integer etwsUploadPeriod;
	private Integer etwsUploadPeriodNoData;
	private String etwsFileServerName;
	private EnabledStates cdrxConnectedMode = null;
	private EnabledStates operateBehindHenbGw = null;
	
	public NetworkParameters(){}
	
	private class CSonConfig{
		Boolean isCSonConfigured = null;
		String cSonIpAddress = null;
		Integer cSonServerPort = null;
		
		@Override
		public String toString() {
			return "CSonConfig [isCSonConfigured=" + isCSonConfigured + ", cSonIpAddress=" + cSonIpAddress
					+ ", cSonServerPort=" + cSonServerPort + "]";
		}
	}
	
	public EnabledStates getEtwsEnabled() {
		return etwsEnabled;
	}
	public void setEtwsEnabled(EnabledStates etwsEnabled) {
		this.etwsEnabled = etwsEnabled;
	}
	public Integer getEtwsUploadPeriod() {
		return etwsUploadPeriod;
	}
	public void setEtwsUploadPeriod(Integer etwsUploadPeriod) {
		this.etwsUploadPeriod = etwsUploadPeriod;
	}
	public Integer getEtwsUploadPeriodNoData() {
		return etwsUploadPeriodNoData;
	}
	public void setEtwsUploadPeriodNoData(Integer etwsUploadPeriodNoData) {
		this.etwsUploadPeriodNoData = etwsUploadPeriodNoData;
	}
	public String getEtwsFileServerName() {
		return etwsFileServerName;
	}
	public void setEtwsFileServerName(String etwsFileServerName) {
		this.etwsFileServerName = etwsFileServerName;
	}
	public EnabledStates getBackhaulQosAdmin() {
		return backhaulQosAdmin;
	}
	public void setBackhaulQosAdmin(EnabledStates backhaulQosAdmin) {
		this.backhaulQosAdmin = backhaulQosAdmin;
	}
	public Integer getBackhaulQosMinReservedForCalls() {
		return backhaulQosMinReservedForCalls;
	}
	public void setBackhaulQosMinReservedForCalls(Integer backhaulQosMinReservedForCalls) {
		this.backhaulQosMinReservedForCalls = backhaulQosMinReservedForCalls;
	}
	public String getBhQosBwReservationServerFqdn() {
		return bhQosBwReservationServerFqdn;
	}
	public void setBhQosBwReservationServerFqdn(String bhQosBwReservationServerFqdn) {
		this.bhQosBwReservationServerFqdn = bhQosBwReservationServerFqdn;
	}
	public String getBhQosFtpFqdn() {
		return bhQosFtpFqdn;
	}
	public void setBhQosFtpFqdn(String bhQosFtpFqdn) {
		this.bhQosFtpFqdn = bhQosFtpFqdn;
	}
	public String getBhQosFtpPassword() {
		return bhQosFtpPassword;
	}
	public void setBhQosFtpPassword(String bhQosFtpPassword) {
		this.bhQosFtpPassword = bhQosFtpPassword;
	}
	public String getBhQosFtpUsername() {
		return bhQosFtpUsername;
	}
	public void setBhQosFtpUsername(String bhQosFtpUsername) {
		this.bhQosFtpUsername = bhQosFtpUsername;
	}
	public List<String> getBhQosReservationServerIpAddressList() {
		return bhQosReservationServerIpAddressList;
	}
	public void setBhQosReservationServerIpAddressList(List<String> bhQosReservationServerIpAddressList) {
		this.bhQosReservationServerIpAddressList = bhQosReservationServerIpAddressList;
	}
	public Boolean getIsCSonConfigured() {
		return cSonConfig.isCSonConfigured;
	}
	public void setIsCSonConfigured(Boolean isCSonConfigured) {
		this.cSonConfig.isCSonConfigured = isCSonConfigured;
	}
	public String getcSonIpAddress() {
		return cSonConfig.cSonIpAddress;
	}
	public void setcSonIpAddress(String cSonIpAddress) {
		this.cSonConfig.cSonIpAddress = cSonIpAddress;
	}
	public Integer getcSonServerPort() {
		return cSonConfig.cSonServerPort;
	}
	public void setcSonServerPort(Integer cSonServerPort) {
		this.cSonConfig.cSonServerPort = cSonServerPort;
	}
	public Boolean getcSonConfig() {
		return cSonConfig.isCSonConfigured;
	}
	public void setcSonConfig(boolean isCSonConfigured, String cSonIpAddress, int cSonServerPort) {
		this.setIsCSonConfigured(isCSonConfigured);
		this.setcSonIpAddress(cSonIpAddress);
		this.setcSonServerPort(cSonServerPort);
	}
	public String getProfileName() {
		return profileName;
	}
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	public void setPLMNList(List<Plmn> listPlmns){
		plmnList = new ArrayList<Plmn>();
		plmnList = (ArrayList<Plmn>) listPlmns;
	}
	public void addPLMN(String mcc,String mnc){
		if(plmnList == null){
			plmnList = new ArrayList<Plmn>();
		}
		Plmn plmnItem = new Plmn();
		plmnItem.setMCC(mcc);
		plmnItem.setMNC(mnc);
		plmnList.add(plmnItem);
	}
	public ArrayList<Plmn> getPlmn(){
		return this.plmnList;
	}
	public static class Plmn{
		
		public Plmn(){
		}
		String MCC = null;
		String MNC = null;
		
		public void setMCC(String mcc){
			this.MCC = mcc;
		}
		public void setMNC(String mnc){
			this.MNC = mnc;
		}
		public String getMCC(){
			return this.MCC;
		}
		public String getMNC(){
			return this.MNC;
		}
		@Override
		public String toString() {
			return "Plmn [MCC=" + MCC + ", MNC=" + MNC + "]";
		}
		public String show() {
			return "(" + MCC + "," + MNC + ")";
		}
		public boolean equal(Plmn plmn2){
			return this.MCC.equals(plmn2.MCC);
		}
	}
	public ArrayList<String> getMMEIPS() {
		return MMEIPS;
	}
	public void setMMEIPS(String... mmeips) {
		MMEIPS = new ArrayList<>();
		for(String mmeip : mmeips)
			MMEIPS.add(mmeip);
	}
	public String getMMEIP(){
		if(MMEIPS != null){
			return MMEIPS.get(0);
		}
		return null;
	}
	public void setMMEIP(String mmeip) {
		setMMEIPS(mmeip);
	}
	
	public EnabledStates getCdrxConnectedMode() {
		return cdrxConnectedMode;
	}
	public void setCdrxConnectedMode(EnabledStates cdrxConnectedMode) {
		this.cdrxConnectedMode = cdrxConnectedMode;
	}
	
	public EnabledStates getOperateBehindHenbGw() {
		return operateBehindHenbGw;
	}
	
	public void setOperateBehindHenbGw(EnabledStates operateBehindHenbGw) {
		this.operateBehindHenbGw = operateBehindHenbGw;
	}
	
	@Override
	public String toString() {
		return "NetworkParameters [profileName=" + profileName + ", MMEIP=" + getMMEIP() + ", plmnList=" + plmnList
				+ ", cSonConfig=" + cSonConfig + "]";
	}
	@Override
	public EnbProfiles getType() {
		return EnbProfiles.Network_Profile;
	}
	
}