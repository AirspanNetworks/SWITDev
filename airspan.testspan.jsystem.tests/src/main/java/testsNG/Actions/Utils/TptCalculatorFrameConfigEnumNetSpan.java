package testsNG.Actions.Utils;

public enum TptCalculatorFrameConfigEnumNetSpan {
one ("(1)    DL  60% : UL 40%"),
two ("(2)    DL  80% : UL 20%");

private String frameConfig;

TptCalculatorFrameConfigEnumNetSpan(String frameConfig) {
	this.frameConfig = frameConfig;
}
	public String getFrameConfig(){
		return frameConfig;
	}
}
