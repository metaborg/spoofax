package org.metaborg.spoofax.meta.core.stratego.primitive;

public class EditInformation {
    
    private int column;
    private int shift;
    
   
    public EditInformation(int column, int shift) {
        this.column = column;
        this.shift = shift;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getShift() {
        return shift;
    }

    public void setShift(int shift) {
        this.shift = shift;
    }
    
    @Override public String toString() {
        return "After column " + column + " shifting " + shift + " spaces.";
    }


}
