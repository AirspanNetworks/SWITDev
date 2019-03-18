package Utils.ConnectionManager.terminal;

import java.util.regex.Pattern;

public interface IPrompt {

	String getPrompt();

	void setPrompt(String prompt);

	boolean isRegularExpression();

	void setRegularExpression(boolean regularExpression);

	boolean equals(Object o);

	boolean isScrallEnd();

	void setScrallEnd(boolean scrallEnd);

	String getStringToSend();

	void setStringToSend(String stringToSend);

	boolean isAddEnter();

	void setAddEnter(boolean addEnter);

	boolean isCommandEnd();

	void setCommandEnd(boolean commandEnd);

	Pattern getPattern();

}