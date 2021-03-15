package controller;

import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import models.Case;
import models.Fourmie;

import java.util.ArrayList;
import java.util.List;

public class MainController {
    @FXML
    private Pane pane;
    @FXML
    private Label cheminPlusCourtLabel;
    @FXML
    private Label nbIterationsLabel;
    @FXML
    private ComboBox<Integer> choixDimension;
    @FXML
    private ComboBox<Integer> choixNbFourmis;
    @FXML
    private ComboBox<Integer> choixIterationsMax;
    @FXML
    private ComboBox<Integer> choixAlpha;
    @FXML
    private ComboBox<Integer> choixBeta;
    @FXML
    private Button boutonGrille;
    @FXML
    private CheckBox togglePheromones;
    @FXML
    private Slider tempsAnimation;

    private List<Fourmie> listeFourmis;

    public static int DIMENSION;
    private int scale;
    private Rectangle [][] map;
    private double [][] pheromones;
    private double [][] longueurChemin;

    private double valeurAlpha;
    private double valeurBeta;
    private double nombreFourmis;
    private double stockPheromones;
    private double tempsMaxParcours;
    private double iterationsMax;
    private double fourmisArrivees;
    private int cheminPlusCourt;

    private int nbIterations = 0;
    private double probaCaseVisite = 3.0;

    final double evaporationPheromones = 0.5;
    final double facteurAleatoire =0.01;

    // initialisation du panneau de configuration des parametres de la simulation
    public void initialize(){
        this.nbIterationsLabel.setText(this.nbIterations+"");
        this.choixDimension.getItems().addAll(8,16,24);
        this.choixDimension.getSelectionModel().select(0);

        this.choixNbFourmis.getItems().addAll(10,25,50,100);
        this.choixNbFourmis.getSelectionModel().select(2);

        this.choixIterationsMax.getItems().addAll(10,25,50,100,200);
        this.choixIterationsMax.getSelectionModel().select(2);

        this.choixAlpha.getItems().addAll(1,2,3,4,5,10);
        this.choixAlpha.getSelectionModel().select(2);

        this.choixBeta.getItems().addAll(1,2,3,4,5,10);
        this.choixBeta.getSelectionModel().select(3);

        this.tempsAnimation.setValue(15);
    }

    @FXML
    public void initGrille(){
        this.boutonGrille.setDisable(true);
        DIMENSION = this.choixDimension.getValue();

        // assignation des differentes variables en fonction de la taille de la grille et des parametres de simulation
        this.scale = 800/DIMENSION;
        this.map = new Rectangle [DIMENSION][DIMENSION];
        this.pheromones = new double[DIMENSION][DIMENSION];
        this.longueurChemin = new double[DIMENSION][DIMENSION];
        this.cheminPlusCourt=DIMENSION*DIMENSION;
        this.iterationsMax = this.choixIterationsMax.getValue();
        this.valeurAlpha = this.choixAlpha.getValue();
        this.valeurBeta = this.choixBeta.getValue();
        this.nombreFourmis = this.choixNbFourmis.getValue();
        this.fourmisArrivees = this.nombreFourmis;
        this.stockPheromones = 0.8*DIMENSION/nombreFourmis;
        this.nbIterations=0;
        this.tempsMaxParcours = DIMENSION * tempsAnimation.getValue() * 60;
        this.listeFourmis = new ArrayList<>();

        // creation de la grille et des cases
        for (int i = 0; i < DIMENSION; i++){
            for (int j = 0; j < DIMENSION; j++){
                map[i][j] = new Rectangle();
                map[i][j].setX(i * scale);
                map[i][j].setY(j * scale);
                map[i][j].setWidth(scale);
                map[i][j].setHeight(scale);
                map[i][j].setFill(Color.WHITE);
                pane.getChildren().add(map[i][j]);
                longueurChemin[i][j] = ((i+j) +1.0)/10.0;
                pheromones[i][j] = 0.01;
            }
        }

        map[0][0].setFill(Color.GREEN);
        map[DIMENSION-1][DIMENSION-1].setFill(Color.RED);

        //creation des fourmis
        for (int i =0;i < nombreFourmis;i++){
            this.listeFourmis.add(new Fourmie());
            pane.getChildren().add(this.listeFourmis.get(i).getImage());
            this.listeFourmis.get(i).getImage().toFront();
            this.listeFourmis.get(i).getImage().setX(0);
            this.listeFourmis.get(i).getImage().setY(0);
            this.listeFourmis.get(i).getImage().setFitHeight(scale);
            this.listeFourmis.get(i).getImage().setFitWidth(scale);
        }

        // mouseEvent pour la creation des obstacles
        pane.setOnMouseDragged(mouseEvent ->{
            int mouseX = (int)mouseEvent.getSceneX()/scale;
            int mouseY = (int)mouseEvent.getSceneY()/scale;
            if (mouseX< DIMENSION && mouseX>=0  && mouseY < DIMENSION && mouseY>=0
                && !(mouseX == 0 && mouseY ==0) && !(mouseX == DIMENSION-1 && mouseY ==DIMENSION-1)){
                map[mouseX][mouseY].setDisable(true);
                map[mouseX][mouseY].setFill(Color.DARKVIOLET);
            }
        });

        // listener pour l'affichage ou non des pheromones
        togglePheromones.selectedProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue){
                        for (int i = 0; i < DIMENSION; i++) {
                            for (int j = 0; j < DIMENSION; j++) {
                                applicationCouleur(i, j);
                            }
                        }
                    }
                    else {
                        for (int i = 0; i < DIMENSION; i++) {
                            for (int j = 0; j < DIMENSION; j++) {
                                if (!map[i][j].isDisable() && !(i ==0 && j ==0) &&!(i==DIMENSION-1 && j== DIMENSION-1)){
                                    map[i][j].setFill(Color.WHITE);
                                }
                            }
                        }
                    }
                }
        );

        // listener pour la vitesse des foumis
        tempsAnimation.valueChangingProperty().addListener(
                (observable, oldValue, newValue) ->
                        this.tempsMaxParcours = DIMENSION * tempsAnimation.getValue() * 60
        );
    }

    // methode lancer lors du clique sur le bouton start de l'apllication
    // methode lancant le deplacement de toutes les fourmis
    @FXML
    public void moveAll(){
        if (fourmisArrivees == nombreFourmis && nbIterations < this.iterationsMax){
            this.fourmisArrivees = 0;
            this.nbIterations++;
            this.nbIterationsLabel.setText(this.nbIterations+"");
            // application de l'evaporation des pheromones pour chaque cases de la grille
            for (int i = 0; i < DIMENSION; i++) {
                for (int j = 0; j < DIMENSION; j++) {
                    if (nbIterations < 10){
                        pheromones[i][j] =Math.max(1/(nombreFourmis*DIMENSION), pheromones[i][j] * evaporationPheromones);
                    }else{
                        pheromones[i][j] *= evaporationPheromones;
                    }
                    // application de la couleur des cases en fonction du depot de pheromones
                    if (this.togglePheromones.selectedProperty().getValue()){
                        applicationCouleur(i, j);
                    }
                }
            }
            // deplacement de chaque fourmis
            for (Fourmie fourmi : this.listeFourmis) {
                fourmi.setTempsParcours(System.currentTimeMillis());
                this.moveFourmie(fourmi);
            }
        }
    }

    // methode gerant le deplacement d'une fourmi jusqu'a la case d'arrivee
    public void moveFourmie(Fourmie fourmie) {
            fourmie.setEnRetour(false);
            double temps = System.currentTimeMillis() - fourmie.getTempsParcours();
            if ((fourmie.getCaseActuelle().getX() < DIMENSION - 1 || fourmie.getCaseActuelle().getY() < DIMENSION - 1 )&& temps < tempsMaxParcours ) {
                PauseTransition p1 = new PauseTransition(Duration.millis(tempsAnimation.getValue()));
                p1.play();

                double rand1 = Math.random();
                p1.setOnFinished(
                        event -> {
                            if (rand1<=this.facteurAleatoire ){
                                double rand2 = Math.random();
                                if (rand2 <= 0.35) {
                                    this.moveDroite(fourmie);
                                } else if (rand2 <= 0.7) {
                                    this.moveBas(fourmie);
                                } else if (rand2 <= 0.85) {
                                    this.moveHaut(fourmie);
                                } else {
                                    this.moveGauche(fourmie);
                                }
                            }else{

                                double totalPheromones = 0.0;

                                double probaCaseDroite = 0.0;
                                double probaCaseBas = 0.0;
                                double probaCaseGauche= 0.0;
                                double probaCaseHaut = 0.0;

                                int x =fourmie.getCaseActuelle().getX();
                                int y =fourmie.getCaseActuelle().getY();


                                // calcul des depots de pheromones adjacents
                                if (x !=DIMENSION-1) {
                                    totalPheromones += Math.pow(pheromones[x + 1][y], valeurAlpha) *Math.pow(longueurChemin[x + 1][y], valeurBeta);
                                }
                                if (y !=DIMENSION-1 ) {
                                    totalPheromones += Math.pow(pheromones[x][y+1], valeurAlpha)*Math.pow(longueurChemin[x][y+1], valeurBeta);
                                }
                                if (x!=0 ) {
                                    totalPheromones += Math.pow(pheromones[x - 1][y], valeurAlpha)*Math.pow(longueurChemin[x -1][y], valeurBeta);
                                }
                                if (y !=0 ) {
                                    totalPheromones += Math.pow(pheromones[x][y-1], valeurAlpha)*Math.pow(longueurChemin[x ][y-1], valeurBeta);
                                }

                                // calcul des probabilités des cases adjacentes
                                if (x !=DIMENSION-1){
                                    if ( !map[x+1][y].isDisable()){
                                        probaCaseDroite = (Math.pow(pheromones[x + 1][y], valeurAlpha) *Math.pow(longueurChemin[x + 1][y], valeurBeta))/totalPheromones;
                                        if (fourmie.getCaseVisite()[x+1][y] ){
                                            probaCaseDroite = probaCaseDroite/probaCaseVisite;
                                        }
                                    }else{
                                        probaCaseDroite = 0.0;
                                    }   
                                }
                                if (y !=DIMENSION-1){
                                    if ( !map[x][y+1].isDisable()){
                                        probaCaseBas = (Math.pow(pheromones[x][y+1], valeurAlpha)*Math.pow(longueurChemin[x][y+1], valeurBeta))/totalPheromones;
                                        if (fourmie.getCaseVisite()[x][y+1] ){
                                            probaCaseBas = probaCaseBas/probaCaseVisite;
                                        }
                                    }else{
                                        probaCaseBas = 0.0;
                                    }
                                }
                                if (x !=0){
                                    if (!map[x-1][y].isDisable()){
                                        probaCaseGauche = (Math.pow(pheromones[x - 1][y], valeurAlpha)*Math.pow(longueurChemin[x -1][y], valeurBeta))/totalPheromones;
                                        if (fourmie.getCaseVisite()[x-1][y] ){
                                            probaCaseGauche = probaCaseGauche/probaCaseVisite;
                                        }
                                    }else{
                                        probaCaseGauche = 0.0;
                                    }
                                }
                                if (y !=0){
                                    if (!map[x][y-1].isDisable() ){
                                        probaCaseHaut = (Math.pow(pheromones[x][y-1], valeurAlpha)*Math.pow(longueurChemin[x ][y-1], valeurBeta))/totalPheromones;
                                        if (fourmie.getCaseVisite()[x][y-1] ){
                                            probaCaseHaut = probaCaseHaut/probaCaseVisite;
                                        }
                                    }else{
                                        probaCaseHaut = 0.0;
                                    }
                                }

                                double totalProba = probaCaseDroite + probaCaseBas + probaCaseGauche +probaCaseHaut;
                                // choix de la direction proportionel à la proba de chaque case adjacente
                                if(totalProba !=0.0){
                                    double rand3 = Math.random() * totalProba;
                                    if (rand3 < probaCaseDroite){
                                        this.moveDroite(fourmie);
                                    }else if (rand3 < probaCaseBas + probaCaseDroite){
                                        this.moveBas(fourmie);
                                    }else if (rand3 < probaCaseDroite + probaCaseBas + probaCaseGauche){
                                        this.moveGauche(fourmie);
                                    }else {
                                        this.moveHaut(fourmie);
                                    }
                                }
                            }
                            this.moveFourmie(fourmie);
                        });
            }
            // lorsque la fourmi arrive à l'arrivee ou est trop longue
            else {
                fourmie.setEnRetour(true);
                this.cheminRetour(fourmie, temps);
            }
    }

    // methode retracant le chemin de la fourmi pour son retour au depart
    public void cheminRetour(Fourmie fourmie, double temps){
            int taille = fourmie.getChemin().size();
            if (taille > 1){
                int x = fourmie.getChemin().get(taille-1).getX() - fourmie.getChemin().get(taille-2).getX() ;
                int y = fourmie.getChemin().get(taille-1).getY() - fourmie.getChemin().get(taille-2).getY() ;

                PauseTransition p1 = new PauseTransition(Duration.millis(tempsAnimation.getValue()));
                p1.play();
                p1.setOnFinished(
                        event -> {
                            if (x == 1 ){
                                this.moveGauche(fourmie);
                            }
                            else if(x == -1){
                                this.moveDroite(fourmie);
                            }else if (y ==1){
                                this.moveHaut(fourmie);
                            }else{
                                this.moveBas(fourmie);
                            }
                            fourmie.getChemin().remove(taille-1);
                            this.cheminRetour(fourmie,temps);
                        });
            }else{
                // lorsque la fourmi retourne au depart
                if ( temps < tempsMaxParcours){
                    // application des pheromones si elle avait atteint l'arrivee
                    this.applicationPheromones(fourmie);
                }
                fourmie.resetCaseVisite();
                this.fourmisArrivees ++;
                this.moveAll();
            }
    }

    public void moveDroite(Fourmie fourmie){
        if (fourmie.getCaseActuelle().getX() < DIMENSION-1
                && !this.map[fourmie.getCaseActuelle().getX() +1][fourmie.getCaseActuelle().getY()].isDisable()){
            TranslateTransition ttFourmi = new TranslateTransition(Duration.millis(tempsAnimation.getValue()), fourmie.getImage());
            ttFourmi.setByX(scale);
            ttFourmi.play();
            fourmie.setCaseActuelle(new Case(fourmie.getCaseActuelle().getX() +1, fourmie.getCaseActuelle().getY()));
            if (!fourmie.getEnRetour()){
                fourmie.updateChemin();
            }
        }
    }

    public void moveBas(Fourmie fourmie){
        if (fourmie.getCaseActuelle().getY() < DIMENSION-1
                && !this.map[fourmie.getCaseActuelle().getX()][fourmie.getCaseActuelle().getY() +1].isDisable()
        ){            TranslateTransition ttFourmi = new TranslateTransition(Duration.millis(tempsAnimation.getValue()), fourmie.getImage());
            ttFourmi.setByY(scale);
            ttFourmi.play();
            fourmie.setCaseActuelle(new Case(fourmie.getCaseActuelle().getX(), fourmie.getCaseActuelle().getY() + 1));
            if (!fourmie.getEnRetour()){
                fourmie.updateChemin();
            }
        }
    }

    public void moveGauche(Fourmie fourmie){
        if (fourmie.getCaseActuelle().getX() > 0
                && !this.map[fourmie.getCaseActuelle().getX() -1][fourmie.getCaseActuelle().getY()].isDisable()
        ){
            TranslateTransition ttFourmi = new TranslateTransition(Duration.millis(tempsAnimation.getValue()), fourmie.getImage());
            ttFourmi.setByX(-scale);
            ttFourmi.play();
            fourmie.setCaseActuelle(new Case(fourmie.getCaseActuelle().getX() -1 , fourmie.getCaseActuelle().getY()));
            if (!fourmie.getEnRetour()){
                fourmie.updateChemin();
            }
        }   
    }

    public void moveHaut(Fourmie fourmie){
        if (fourmie.getCaseActuelle().getY() > 0
                && !this.map[fourmie.getCaseActuelle().getX()][fourmie.getCaseActuelle().getY() -1].isDisable()
        ){
            TranslateTransition ttFourmi = new TranslateTransition(Duration.millis(tempsAnimation.getValue()), fourmie.getImage());
            ttFourmi.setByY(-scale);
            ttFourmi.play();
            fourmie.setCaseActuelle(new Case(fourmie.getCaseActuelle().getX(), fourmie.getCaseActuelle().getY() - 1));
            if (!fourmie.getEnRetour()){
                fourmie.updateChemin();
            }
        }
    }

    // methode appliquant les pheromones a toutes les cases visites par la fourmi
    public void applicationPheromones(Fourmie fourmie){
        double contribution = stockPheromones / (fourmie.getLongueurChemin() *2);
        // si la fourmi realise un chemin plus court
        // ou egale au chemin le plus court actuel alors les pheromones deposes sont doubles
        if (fourmie.getLongueurChemin() <= this.cheminPlusCourt){
            this.cheminPlusCourt = fourmie.getLongueurChemin();
            this.cheminPlusCourtLabel.setText(this.cheminPlusCourt+"");
            contribution *= 2;

            probaCaseVisite += 2/nombreFourmis;
        }
        for (int i = 0; i < DIMENSION; i++) {
            for (int j = 0; j < DIMENSION; j++) {
                if (fourmie.getCaseVisite()[i][j]){
                    pheromones[i][j] += contribution;
                }
                if (this.togglePheromones.selectedProperty().getValue()){
                    applicationCouleur(i, j);
                }
            }
        }
    }

    private void applicationCouleur(int i, int j) {
        if (!map[i][j].isDisable() && !(i ==0 && j ==0) &&!(i==DIMENSION-1 && j== DIMENSION-1)){
            double tempColor  = pheromones[i][j];
            Color temp = new Color(1,1-tempColor,1-tempColor,1);
            map[i][j].setFill(temp);
        }
    }

    @FXML
    public void infosAco(){
        Alert alert = new Alert(Alert.AlertType.NONE, " ", ButtonType.OK);
        alert.setContentText("Les algorithmes de colonies de fourmis sont des algorithmes inspirés " +
                "du comportement des fourmis, ou d'autres espèces formant un superorganisme, et qui constituent une famille " +
                "d’algorithme d’optimisation pour lesquelles on ne connait pas de méthodes classiques plus efficaces." +
                "\n\nSur le chemin de retour après avoir atteint la nourriture, la fourmi va déposer des phéromones qui " +
                "attireront les prochaines fourmis, ainsi au fil des itérations les fourmis vont converger vers un chemin unique." +
                "\n\nPlus le chemin est court plus le nombre de phéromones déposées est important, au fil du temps," +
                " les fourmis finiront par trouver le chemin le plus court");
        alert.getDialogPane().setMinWidth(600);
        alert.show();
    }
}