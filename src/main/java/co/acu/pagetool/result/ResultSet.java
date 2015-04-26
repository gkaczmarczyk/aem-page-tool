package co.acu.pagetool.result;

import java.util.ArrayList;

/**
 * @author Gregory Kaczmarczyk
 */
public class ResultSet {

    boolean success;
    int results;
    int total;
    int offset;
    ArrayList<ResultPage> hits;

    public ResultSet() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getResults() {
        return results;
    }

    public void setResults(int results) {
        this.results = results;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public ArrayList<ResultPage> getHits() {
        return hits;
    }

    public void setHits(ArrayList<ResultPage> hits) {
        this.hits = hits;
    }

}
