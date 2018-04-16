package Ercom.UeSimulator;

public final class Commands {

	static class  TestStorage{
		private static String addressedTo = "tests";
		public static Command getDefinedTests(){
			return new Command("testStorage.getDefinedTests", addressedTo);
		}
	}
	
	static class CurrentExecution{
		private static String addressedTo = "testMgt";
		public static Command getExecutionStateForTest(){
			return new Command("currentExecution.executionStateForTest", addressedTo);
		}
		
		public static Command getExecutionState(){
			return new Command("currentExecution.executionState", addressedTo);
		}
		
		public static Command getCurrentTest(){
			return new Command("currentExecution.currentTest", addressedTo);
		}
		
		public static Command getCurrentExecId(){
			return new Command("currentExecution.currentExecId", addressedTo);
		}
		
		public static Command getStoppingReason(){
			return new Command("currentExecution.stoppingReason", addressedTo);
		}
		
	}
	
	
	static class TestQueue{
		private static String addressedTo = "testMgt";
		
		public static Command performDirectExecution(){
			return new Command("testQueue.directExecution", addressedTo);
		}
		
		public static Command isDirectRunning(){
			return new Command("testQueue.directRunning", addressedTo);
		}
		
		public static Command isDirectAllowed(){
			return new Command("testQueue.directAllowed", addressedTo);
		}
		
		public static Command stopCurrentTest(){
			return new Command("testQueue.stopCurrentTest", addressedTo);
		}
		
		public static Command queueStart(){
			return new Command("testQueue.queueStart", addressedTo);
		}
		
		public static Command getLoopsPerformedOnJobQueue(){
			return new Command("testQueue.loopsPerformedOnJobQueue", addressedTo);
		}
		
		public static Command queueStop(){
			return new Command("testQueue.queueStop", addressedTo);
		}
		
		public static Command addToQueue(){
			return new Command("testQueue.addToQueue", addressedTo);
		}
		
		public static Command isQueueStopAllowed(){
			return new Command("testQueue.queueStopAllowed", addressedTo);
		}
		
		public static Command isQueueRunning(){
			return new Command("testQueue.queueRunning", addressedTo);
		}
	}
}




















