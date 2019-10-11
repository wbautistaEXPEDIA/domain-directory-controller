package com.imperva.ddc.core.query;

public class ReplaceModificationDetails extends ModificationDetails {

    private String value;

    public ReplaceModificationDetails(String dn, Field attribute, String value){
        super(dn, attribute, Operation.REPLACE);
        this.value=value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }
}
