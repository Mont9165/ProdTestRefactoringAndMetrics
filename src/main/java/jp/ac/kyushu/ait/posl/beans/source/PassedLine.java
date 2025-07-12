package jp.ac.kyushu.ait.posl.beans.source;


public class PassedLine implements Comparable{
    /**
     * method signature
     */
    public String signature;
    /**
     * line no
     */
    public Integer lineNo;
    /**
     * selogger type
     */
    public String type;

    public PassedLine(String s1, Integer lineNo) {
        assert (s1 != null);
        this.signature = s1;
        this.lineNo = lineNo;
    }




    public String toString() {
        return signature + "@" + lineNo;
    }

    @Override
    public int compareTo(Object o) {
        return this.toString().compareTo(o.toString());
    }
    @Override
    public boolean equals(Object o){
        return o.toString().equals(this.toString());
    }
}
