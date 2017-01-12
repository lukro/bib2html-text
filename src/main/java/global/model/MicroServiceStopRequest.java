package global.model;

import java.io.Serializable;

/**
 * Created by pc on 12.01.2017.
 */
public class MicroServiceStopRequest implements Serializable {

    private final String microServiceIDToStop;

    public MicroServiceStopRequest(String microServiceIDToStop) {
        this.microServiceIDToStop = microServiceIDToStop;
    }

    public String getMicroServiceIDToStop() {
        return microServiceIDToStop;
    }

}
