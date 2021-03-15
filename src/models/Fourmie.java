package models;

import java.util.ArrayList;
import controller.MainController;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class Fourmie {

    private Case caseActuelle;
    private ArrayList<Case> chemin = new ArrayList<>();
    private Boolean [][] caseVisite  = new Boolean[MainController.DIMENSION][MainController.DIMENSION];
    private ImageView image = new ImageView();

    private Boolean enRetour;
    private int longueurChemin;
    private double tempsParcours;


    // constructeur
    public Fourmie(){
        this.caseActuelle = new Case(0,0);
        this.enRetour =false;
        this.longueurChemin=0;
        Image temp = new Image("file:src/ressources/fourmie.jpg");
        this.image.setImage(temp);

        resetCaseVisite();
        this.updateChemin();
    }

    // methode reinitialisant la liste des cases visites par la fourmi
    public void resetCaseVisite() {
        for (int i = 0; i < MainController.DIMENSION; i++) {
            for (int j = 0; j < MainController.DIMENSION; j++) {
                caseVisite[i][j] = false;
            }
        }
        caseVisite[0][0] = true;
        this.longueurChemin=0;
    }

    // methode mettant a jour la liste des cases parcourues
    public void updateChemin(){
        if (!caseVisite[caseActuelle.getX()][caseActuelle.getY()]){
            this.longueurChemin+=1;
            caseVisite[caseActuelle.getX()][caseActuelle.getY()] =true;
        }
        chemin.add(caseActuelle);
    }

    // getters et setters
    public Case getCaseActuelle() {
        return caseActuelle;
    }

    public ArrayList<Case> getChemin() {
        return chemin;
    }

    public Boolean getEnRetour() {
        return enRetour;
    }

    public int getLongueurChemin() {
        return longueurChemin;
    }

    public Boolean[][] getCaseVisite() {
        return caseVisite;
    }

    public ImageView getImage() {
        return image;
    }

    public double getTempsParcours() {
        return tempsParcours;
    }

    public void setTempsParcours(double tempsParcours) {
        this.tempsParcours = tempsParcours;
    }

    public void setEnRetour(Boolean enRetour) {
        this.enRetour = enRetour;
    }

    public void setCaseActuelle(Case caseActuelle) {
        this.caseActuelle = caseActuelle;
    }
}
