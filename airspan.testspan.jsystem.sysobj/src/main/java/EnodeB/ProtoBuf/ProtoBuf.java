package EnodeB.ProtoBuf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.google.protobuf.GeneratedMessageV3;

import EnodeB.ProtoBuf.LteStatsV1291600016.PbLteStats;
import EnodeB.ProtoBuf.LteStatsV1291600016.PbLteStatsPerEtwsWarningType;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteAnrStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteCellStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteMmeStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteNwElementStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteRfStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteSgwStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteStatus;
import Utils.GeneralUtils;

/**
 * 
 * @author hengoldburd
 *
 */
public class ProtoBuf {

	/**
	 * 
	 * @param protoBuf
	 * @param counterName
	 * @return counter value from protoBuf objects
	 */
	@SuppressWarnings("unused")
	private static double parseCounter(GeneratedMessageV3 targetClass, String counterName) {

		if (isMethodContainsExist(counterName, targetClass.getClass())) {
			return extractCounter(targetClass, counterName);
		}
		else{
			GeneralUtils.printToConsole("Error :: Unknown Counter!");
			return GeneralUtils.ERROR_VALUE;
		}
	}
	

	public static double getStatsPbLteStatsCell(byte[] protoBuf,String counterName){
		GeneratedMessageV3 temp = null;
		  try {
			   //check first Class
			   PbLteStats lte = PbLteStats.parseFrom(protoBuf);
			   if (lte.hasPbLteStatsCell()) {
			    temp = lte.getPbLteStatsCell();
			    if (isMethodContainsExist(counterName, temp.getClass())) 
			     return extractCounter(temp, counterName);  
			   }
			   
			   //if not first class check 2nd
			   if(lte.hasPbLteStatsEnb()){
			    temp = lte.getPbLteStatsEnb();
			    if (isMethodContainsExist(counterName, temp.getClass())) 
			     return extractCounter(temp, counterName); 
			   }
			   
			   PbLteStatsPerEtwsWarningType warn = PbLteStatsPerEtwsWarningType.parseFrom(protoBuf);
			   if(isMethodContainsExist(counterName, warn.getClass())){
				   return extractCounter(warn, counterName);
			   }
			   
			  }catch (Exception e) {
			   e.printStackTrace();
			  }
		  
			  GeneralUtils.printToConsole("Error :: Unknown Counter!");
			  return GeneralUtils.ERROR_VALUE;
	}
	
	public static List<PbLteAnrStatus> getPbLteAnrStatus(byte[] protoBuf){
		try {
			PbLteStatus pbLteStatus = PbLteStatus.parseFrom(protoBuf);
			if (pbLteStatus  != null) {
				return pbLteStatus.getPbLteAnrStatusListList();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<PbLteCellStatus> getPbLteCellStatus(byte[] protoBuf){
		try {
			PbLteStatus pbLteStatus = PbLteStatus.parseFrom(protoBuf);
			if (pbLteStatus  != null) {
				return pbLteStatus.getPbLteCellStatusListList();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<PbLteNwElementStatus> getPbLteNetworkElementStatus(byte[] protoBuf){
		try {
			PbLteStatus pbLteStatus = PbLteStatus.parseFrom(protoBuf);
			if (pbLteStatus  != null) {
				return pbLteStatus.getPbLteNwElementStatusListList();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<PbLteMmeStatus> getPbLteMmeStatus(byte[] protoBuf){
		try {
			PbLteStatus pbLteStatus = PbLteStatus.parseFrom(protoBuf);
			if (pbLteStatus  != null) {
				return pbLteStatus.getPbLteMmeStatusListList();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<PbLteSgwStatus> getPbLteSgwStatus(byte[] protoBuf){
		try {
			PbLteStatus pbLteStatus = PbLteStatus.parseFrom(protoBuf);
			if (pbLteStatus  != null) {
				return pbLteStatus.getPbLteSgwStatusListList();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<PbLteRfStatus> getPbLteRfStatus(byte[] protoBuf){
		try {
			PbLteStatus pbLteStatus = PbLteStatus.parseFrom(protoBuf);
			if (pbLteStatus  != null) {
				return pbLteStatus.getPbLteRfStatusListList();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * InvocationTarget
	 * 
	 * @param msg
	 * @param protoBuf
	 * @param counterName
	 * @return counter valuelte
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * 
	 */
	private static double extractCounter(GeneratedMessageV3 msg, String counterName){
		Method getMethod = getGetMethod(counterName, msg);
		Object returnObj;
		try {
			returnObj = getMethod.invoke(msg);
			return Double.parseDouble(returnObj.toString());
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			
			e.printStackTrace();
			return GeneralUtils.ERROR_VALUE;
		}
		
	}

	/**
	 * 
	 * @param containsString
	 * @param class1
	 * @return if the counter method is exist in the class provided
	 * 
	 */
	private static boolean isMethodContainsExist(String containsString, Class<? extends GeneratedMessageV3> class1) {
		Method[] methodArr = class1.getMethods();
		for (Method method : methodArr) {
			if (method.getName().toLowerCase().contains(containsString.toLowerCase()))
				return true;
		}

		return false;
	}

	/**
	 * 
	 * @param counterName
	 * @param obj
	 * @return Finds the current get method for the counter Name
	 */
	private static Method getGetMethod(String counterName, GeneratedMessageV3 obj) {
		Method[] methodArr = obj.getClass().getMethods();
		for (Method method : methodArr) {
			if (method.getName().toLowerCase().contains("get"))
				if (method.getName().toLowerCase().equals("get" + counterName.toLowerCase()))
					return method;
		}
		return null;
	}

}
