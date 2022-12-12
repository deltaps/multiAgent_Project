package atelier;

import jade.core.Agent;
import java.util.HashMap;
import java.util.List;

public class robot extends Agent {

    private HashMap<String,Float> competences;//Dictionnaire avec en clé le nom de la compétence et en valeur le degrès de la compétence entre 0 et 1.
    private List<produit> produits;//File d'attente des produits que l'agent doit faire.
    protected void setup(){
        System.out.println("Hello! Agent "+getAID().getName()+" is ready.");
        //Assigner aléatoirement des compétences au robot --------------------
        List<String> allCompetences = List.of("souder", "peindre", "assembler");
        for(String comp : allCompetences){
            int rand = (int)(Math.random() * 2) + 1;
            if(rand == 1){
                competences.put(comp, (float) Math.random());
            }
        }
        // ----------------------------------------------------------------

    }
}
