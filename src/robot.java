import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.HashMap;
import java.util.List;

public class robot extends Agent {

    private HashMap<String,Float> competences;//Dictionnaire avec en clé le nom de la compétence et en valeur le degrès de la compétence entre 0 et 1.
    private List<produit> produits;//File d'attente des produits que l'agent doit faire.
    protected void setup(){
        System.out.println("Hello! Agent "+getAID().getName()+" is ready.");
        //Assigner aléatoirement des compétences au robot --------------------
        List<String> allCompetences = List.of("souder", "peindre", "assembler");
        this.competences = new HashMap<>();
        for(String comp : allCompetences){
            int rand = (int)(Math.random() * 2) + 1;
            if(rand == 1){
                this.competences.put(comp, (float) Math.random());
            }
        }

        //Ajouter une description a l'agent pour chacun de ces comportements
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Competence");
        for(String comp : competences.keySet()){
            sd.setName(comp);
            template.addServices(sd);
        }
        // ----------------------------------------------------------------

    }
}
