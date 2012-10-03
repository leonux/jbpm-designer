package org.jbpm.designer.assets.persistence;

public enum AssetType {
    IMAGE(1), FORM(2), BPMN(3);
    
    private int value;
    AssetType(int value) { 
        this.value = value;
    }
}
