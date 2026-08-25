package com.example.gestion.model;

public class Prof {

    private Integer codeprof;
    private String nom;
    private String prenom;
    private String grade;

    public Prof() {
    }

    public Prof(Integer codeprof, String nom, String prenom, String grade) {
        this.codeprof = codeprof;
        this.nom = nom;
        this.prenom = prenom;
        this.grade = grade;
    }

    public Integer getCodeprof() {
        return codeprof;
    }

    public void setCodeprof(Integer codeprof) {
        this.codeprof = codeprof;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getNomComplet() {
        if (nom == null && prenom == null) {
            return "";
        }
        return nom + " " + prenom;
    }

    @Override
    public String toString() {
        return "Prof{codeprof=" + codeprof + ", nom='" + nom + "', prenom='" + prenom + "', grade='" + grade + "'}";
    }
}
