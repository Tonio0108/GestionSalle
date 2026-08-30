package com.example.gestion.model;

import java.time.LocalDate;

public class Occuper {

    private Integer codeprof;
    private Integer codesal;
    private LocalDate date;
    private String nomProf;
    private String designationSalle;

    public Occuper() {
    }

    public Occuper(Integer codeprof, Integer codesal, LocalDate date) {
        this.codeprof = codeprof;
        this.codesal = codesal;
        this.date = date;
    }

    public Occuper(Integer codeprof, Integer codesal, LocalDate date,
                   String nomProf, String designationSalle) {
        this.codeprof = codeprof;
        this.codesal = codesal;
        this.date = date;
        this.nomProf = nomProf;
        this.designationSalle = designationSalle;
    }

    public Integer getCodeprof() {
        return codeprof;
    }

    public void setCodeprof(Integer codeprof) {
        this.codeprof = codeprof;
    }

    public Integer getCodesal() {
        return codesal;
    }

    public void setCodesal(Integer codesal) {
        this.codesal = codesal;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNomProf() {
        return nomProf;
    }

    public void setNomProf(String nomProf) {
        this.nomProf = nomProf;
    }

    public String getDesignationSalle() {
        return designationSalle;
    }

    public void setDesignationSalle(String designationSalle) {
        this.designationSalle = designationSalle;
    }

    @Override
    public String toString() {
        return "Occuper{codeprof=" + codeprof + ", codesal=" + codesal
                + ", date=" + date + "}";
    }
}
