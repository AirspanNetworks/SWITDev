package testsNG.Actions.Utils;

public enum TptCalculatorSubFrameEnumNetSpan {
	five ("(5)      90.4   [Km]"),
	seven ("(7)     15.4   [Km]");

	private String frameConfig;

	TptCalculatorSubFrameEnumNetSpan(String frameConfig) {
		this.frameConfig = frameConfig;
	}
		public String getSubFrame(){
			return frameConfig;
		}
	}
