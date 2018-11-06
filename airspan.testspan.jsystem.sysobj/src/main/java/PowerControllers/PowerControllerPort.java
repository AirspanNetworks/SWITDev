package PowerControllers;

import java.io.IOException;

import jsystem.framework.system.SystemObjectImpl;

public class PowerControllerPort extends SystemObjectImpl {
    private static final long DEFAULT_RESET_DELAY = 5000L;

    private String port;
    private String description;

    public boolean powerOn() throws NullPointerException, IOException {
        return ((PowerController) getParent()).powerOnPort(this);
    }

    /**
     * power Off
     *
     * @return - true if succeed
     * @throws NullPointerException - NullPointerException
     * @throws IOException          - IOException
     */
    public boolean powerOff() throws NullPointerException, IOException {
        return ((PowerController) getParent()).powerOffPort(this);
    }

    public void restart() throws NullPointerException, IOException {
        restart(DEFAULT_RESET_DELAY);
    }

    public void restart(long delay) throws NullPointerException, IOException {
        powerOff();
        try {
            Thread.sleep(delay);
        } catch (Exception e) {
        }
        powerOn();
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return port;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PowerControllerPort) {
            PowerControllerPort port = (PowerControllerPort) obj;
            return getParent() == port.getParent() && getPort().equals(port.getPort());
        }
        return false;
    }
}
