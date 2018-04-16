package Ercom.UeSimulator;

import java.util.HashMap;
import java.util.Map;

public class ControlBlade extends Blade{

	
	/* Always add to the end of the queue*/
	private static final int PLACE_IN_QUEUE = -1;
	
	public String IdOfPlatform = "";
	
	@ Override
	public void init() throws Exception {
		super.init();
	}
	
	@ Override
	public void close() {
		super.close();
	}
	
	/**
	 *  default constructor
	 */
	public ControlBlade(){
		
	}
	
	
	public Map<String,String> getDefinedTests(){
		Map<String,String> name2id = new  HashMap<String,String>();
		Object[] obj =(Object[])sendCommand(IdOfPlatform, Commands.TestStorage.getDefinedTests(),"");
  	    for (Object object : obj) {
			@SuppressWarnings("unchecked")
			Map<String,Object> temp = (Map<String,Object>)object;
			name2id.put((String)temp.get("name"), (String)temp.get("uniqId"));
		}
  	    return name2id;
	}

	
	public String performDirectExecution(String testIniqId){
		return (String)sendCommand(IdOfPlatform, Commands.TestQueue.performDirectExecution(),testIniqId);
	}
	
	public boolean isDirectRunning(){
		return (boolean)sendCommand(IdOfPlatform, Commands.TestQueue.isDirectRunning(),"");
	}
	
	public boolean isDirectAllowed(){
		return (boolean)sendCommand(IdOfPlatform, Commands.TestQueue.isDirectAllowed(),"");
	}
	
	public boolean stopCurrentlyExecutedTest(){
		return (boolean)sendCommand(IdOfPlatform, Commands.TestQueue.stopCurrentTest(),"");
	}
	
	public String getPlatformExecutionState(){
		return (String)sendCommand(IdOfPlatform, Commands.CurrentExecution.getExecutionState(),"");
	}
	
	public String getExecutionStateForTest(String testIniqId){
		return (String)sendCommand(IdOfPlatform, Commands.CurrentExecution.getExecutionStateForTest(),testIniqId);
	}
	
	public String getCurrentlyExecutedTest(){
		return (String)sendCommand(IdOfPlatform, Commands.CurrentExecution.getCurrentTest(),"");
	}
	
	public String getCurrentExecId(){
		return (String)sendCommand(IdOfPlatform, Commands.CurrentExecution.getCurrentExecId(),"");
	}
	
	public String getStoppingReasonForLastExecution(){
		return (String)sendCommand(IdOfPlatform, Commands.CurrentExecution.getStoppingReason(),"");
	}

	
	public void startQueue(boolean loop){
		sendCommand(IdOfPlatform, Commands.TestQueue.queueStart(),loop);
	}
	
	public int getLoopsPerformedOnJobQueue(){
		return (int)sendCommand(IdOfPlatform, Commands.TestQueue.getLoopsPerformedOnJobQueue(),"");
	}
	
	public void stopQueue(boolean loop){
		sendCommand(IdOfPlatform, Commands.TestQueue.queueStop(),loop);
	}
	
	public void addToQueue(String testIniqId){
		sendCommand(IdOfPlatform, Commands.TestQueue.addToQueue(),testIniqId,PLACE_IN_QUEUE);
	}
	
	public boolean isQueueStopAllowed(){
		return (boolean)sendCommand(IdOfPlatform, Commands.TestQueue.isQueueStopAllowed(),"");
	}
	
	public boolean isQueueRunning(){
		return (boolean)sendCommand(IdOfPlatform, Commands.TestQueue.isQueueRunning(),"");
	}
	
	
	//----------------------------- getters -----------------------------

	public String getIdOfPlatform() {
		return IdOfPlatform;
	}

	public void setIdOfPlatform(String idOfPlatform) {
		IdOfPlatform = idOfPlatform;
	}
	
	

}
