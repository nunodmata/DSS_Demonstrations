package eu.europa.esig.dss.web.model;

import eu.europa.esig.dss.web.validation.Base64;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class CMDSignatureDigestForm extends SignatureDigestForm {


	@NotNull
	@Pattern(regexp = "(\\+351) *9[0-9]{8}", message = "{error.cmd.userId.wrongInput}")
	private String userId;

	@NotNull
	@Pattern(regexp = "[0-9]{4,8}", message = "{error.cmd.userPin.wrongInput}")
	private String userPin;

	private String processId;



	public String getUserId() {
		return userId;
	}

	public String getProcessId() {
		return processId;
	}

	public String getUserPin() {
		return userPin;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public void setUserPin(String userPin) {
		this.userPin = userPin;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
