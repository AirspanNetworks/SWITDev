package Netspan.Profiles;

public class AccessBarringAdvanced {
	private AccessBarringFactor barringFactor;
	private AccessBarringTimeInSec barringTime;
	private Boolean barringClass_11;
	private Boolean barringClass_12;
	private Boolean barringClass_13;
	private Boolean barringClass_14;
	private Boolean barringClass_15;
	
	public AccessBarringAdvanced(AccessBarringFactor _barringFactor, AccessBarringTimeInSec _BarringTime, Boolean _barringClass_11, Boolean _barringClass_12, Boolean _barringClass_13, Boolean _barringClass_14, Boolean _barringClass_15) {
		barringFactor = _barringFactor;
		barringTime = _BarringTime;
		barringClass_11 = _barringClass_11;
		barringClass_12 = _barringClass_12;
		barringClass_13 = _barringClass_13;
		barringClass_14 = _barringClass_14;
		barringClass_15 = _barringClass_15;
	}
	
	public enum AccessBarringFactor { 
		FIVE("0.05"), 
		TEN("0.10"), 
		FIFTEEN("0.15"), 
		TWENTY("0.20"), 
		TWENTYFIVE("0.25"), 
		THIRTY("0.30"), 
		FORTY("0.40"), 
		FIFTY("0.50"), 
		SIXTY("0.60"), 
		SEVENTY("0.70"), 
		SEVENTYFIVE("0.75"), 
		EIGHTY("0.80"),
		EIGHTYFIVE("0.85"),
		NINETY("0.90"), 
		NINETYFIVE("0.95");
		private final String value;
		 
		AccessBarringFactor(String v) {
		        value = v;
		    }

		    public String value() {
		        return value;
		    }

		    public static AccessBarringFactor fromValue(String v) {
		        for (AccessBarringFactor c: AccessBarringFactor.values()) {
		            if (c.value.equals(v)) {
		                return c;
		            }
		        }
		        throw new IllegalArgumentException(v);
		    }
	}
	
	public enum AccessBarringTimeInSec { 
		_4 ("4"), 
		_8("8"), 
		_16("16"), 
		_32("32"), 
		_64("64"), 
		_128("128"), 
		_256("256"), 
		_512("512");
		 private final String value;
		 
		 AccessBarringTimeInSec(String v) {
		        value = v;
		    }

		    public String value() {
		        return value;
		    }

		    public static AccessBarringTimeInSec fromValue(String v) {
		        for (AccessBarringTimeInSec c: AccessBarringTimeInSec.values()) {
		            if (c.value.equals(v)) {
		                return c;
		            }
		        }
		        throw new IllegalArgumentException(v);
		    }
	}
	
	public AccessBarringFactor getBarringFactor() {
		return barringFactor;
	}

	public void setBarringFactor(AccessBarringFactor barringFactor) {
		this.barringFactor = barringFactor;
	}

	public AccessBarringTimeInSec getBarringTime() {
		return barringTime;
	}

	public void setBarringTime(AccessBarringTimeInSec barringTime) {
		barringTime = barringTime;
	}
	
	public Boolean isBarringClass_11() {
		return barringClass_11;
	}

	public void setBarringClass_11(boolean barringClass_11) {
		this.barringClass_11 = barringClass_11;
	}

	public Boolean isBarringClass_12() {
		return barringClass_12;
	}

	public void setBarringClass_12(boolean barringClass_12) {
		this.barringClass_12 = barringClass_12;
	}

	public Boolean isBarringClass_13() {
		return barringClass_13;
	}

	public void setBarringClass_13(boolean barringClass_13) {
		this.barringClass_13 = barringClass_13;
	}

	public Boolean isBarringClass_14() {
		return barringClass_14;
	}

	public void setBarringClass_14(boolean barringClass_14) {
		this.barringClass_14 = barringClass_14;
	}

	public Boolean isBarringClass_15() {
		return barringClass_15;
	}

	public void setBarringClass_15(boolean barringClass_15) {
		this.barringClass_15 = barringClass_15;
	}
}
