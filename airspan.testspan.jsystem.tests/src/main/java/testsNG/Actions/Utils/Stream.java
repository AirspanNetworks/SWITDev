package testsNG.Actions.Utils;

public class Stream {
	public String[] arrayOfStreams;
	final int ueCategoryInt = 0;
	final int duplexModeInt = 1;
	final int bandWidthInt = 2;
	final int fcInt = 3;
	final int frameInt = 4;
	final int ulPtpInt = 5;
	final int ulPtmpInt = 6;
	final int dlPtpInt = 7;
	final int dlPtmpInt = 8;
	final int cfiInt = 9;
	final int labelInt = 10;

	public String getUeCategory() {
		return arrayOfStreams[ueCategoryInt];
	}

	public void setUeCategory(String ueCategory) {
		this.arrayOfStreams[ueCategoryInt] = ueCategory;
	}

	public String getDuplexMode() {
		return arrayOfStreams[duplexModeInt];
	}

	public void setDuplexMode(String duplexMode) {
		this.arrayOfStreams[duplexModeInt] = duplexMode;
	}

	public String getBandWidth() {
		return arrayOfStreams[bandWidthInt];
	}

	public void setBandWidth(String bandWidth) {
		this.arrayOfStreams[bandWidthInt] = bandWidth;
	}

	public String getFc() {
		return arrayOfStreams[fcInt];
	}

	public void setFc(String fc) {
		this.arrayOfStreams[fcInt] = fc;
	}

	public String getFrame() {
		return arrayOfStreams[frameInt];
	}

	public void setFrame(String frame) {
		this.arrayOfStreams[frameInt] = frame;
	}

	public String getUlPtp() {
		return arrayOfStreams[ulPtpInt];
	}

	public void setUlPtp(String ulPtp) {
		this.arrayOfStreams[ulPtpInt] = ulPtp;
	}

	public String getUlPtmp() {
		return arrayOfStreams[ulPtmpInt];
	}

	public void setUlPtmp(String ulPtmp) {
		this.arrayOfStreams[ulPtmpInt] = ulPtmp;
	}

	public String getDlPtp() {
		return arrayOfStreams[dlPtpInt];
	}

	public void setDlPtp(String dlPtp) {
		this.arrayOfStreams[dlPtpInt] = dlPtp;
	}

	public String getDlPtmp() {
		return arrayOfStreams[dlPtmpInt];
	}

	public void setDlPtmp(String dlPtmp) {
		this.arrayOfStreams[dlPtmpInt] = dlPtmp;
	}

	public String getCfi() {
		return arrayOfStreams[cfiInt];
	}

	public void setCfi(String cfi) {
		this.arrayOfStreams[cfiInt] = cfi;
	}

	public String getLabel() {
		return arrayOfStreams[labelInt];
	}

	public void setLabel(String label) {
		this.arrayOfStreams[labelInt] = label;
	}

	public Stream(int size) {
		arrayOfStreams = new String[size];
	}

	@Override
	public String toString() {
		return this.getBandWidth() + " " + this.getCfi() + " " + this.getDuplexMode() + " " + this.getFc() + " "
				+ this.getFrame() + " " + this.getUeCategory() + " " + this.getLabel();
	}

}
