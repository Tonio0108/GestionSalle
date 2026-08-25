package com.example.gestion.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Occuper {

    private Integer codeprof;
    private Integer codesal;
    private LocalDate date;
    private LocalTime heure;
    private String nomProf;
    private String designationSalle;

    public Occuper() {
    }

    public Occuper(Integer codeprof, Integer codesal, LocalDate date, LocalTime heure) {
        this.codeprof = codeprof;
        this.codesal = codesal;
        this.date = date;
        this.heure = heure;
    }

    public Occuper(Integer codeprof, Integer codesal, LocalDate date, LocalTime heure,
                   String nomProf, String designationSalle) {
        this.codeprof = codeprof;
        this.codesal = codesal;
        this.date = date;
        this.heure = heure;
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

    public LocalTime getHeure() {
        return heure;
    }

    public void setHeure(LocalTime heure) {
        this.heure = heure;
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
                + ", date=" + date + ", heure=" + heure + "}";
    }
}
