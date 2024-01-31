package eu.europa.esig.dss.web.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class AccountManagementForm {
    @NotNull
    @Pattern(regexp = "(\\+351) *9[0-9]{8}", message = "{error.cmd.userId.wrongInput}")
    private String userId;

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }


}
