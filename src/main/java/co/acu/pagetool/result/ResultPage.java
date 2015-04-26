package co.acu.pagetool.result;

import com.google.gson.annotations.SerializedName;

/**
 * @author Gregory Kaczmarczyk
 */
public class ResultPage {

    @SerializedName("jcr:path")
    private String jcrPath;

    public ResultPage() {
    }

    public String getJcrPath() {
        return jcrPath;
    }

    public void setJcrPath(String jcrPath) {
        this.jcrPath = jcrPath;
    }

}
