package com.example.gestion.model;

public class Salle {

    private Integer codesal;
    private String designation;

    public Salle() {
    }

    public Salle(Integer codesal, String designation) {
        this.codesal = codesal;
        this.designation = designation;
    }

    public Integer getCodesal() {
        return codesal;
    }

    public void setCodesal(Integer codesal) {
        this.codesal = codesal;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    @Override
    public String toString() {
        return "Salle{codesal=" + codesal + ", designation='" + designation + "'}";
    }
}
