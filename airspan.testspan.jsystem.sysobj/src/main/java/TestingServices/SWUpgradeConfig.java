package TestingServices;

import jsystem.framework.system.SystemObjectImpl;

/**
 * Created by owiesel on 22-May-16.
 */
public class SWUpgradeConfig extends SystemObjectImpl {

    private String formalLocation = "P:\\BS\\Development\\Air4G_LTE\\Builds";

    private String internalLocation = "P:\\BS\\Development\\Air4G_LTE\\Internal\\Builds";

    private String build;

    private String absolutPath;


    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getAbsolutPath() {
        return absolutPath;
    }

    public void setAbsolutPath(String absolutPath) {
        if (absolutPath == ""){
            this.absolutPath = null;
        }else {
            this.absolutPath = absolutPath;
        }
    }

    public String getFormalLocation() {
        return formalLocation;
    }

    public String getInternalLocation() {
        return internalLocation;
    }
}