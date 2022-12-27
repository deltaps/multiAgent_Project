
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class atelier extends Agent {

    private List<produit> produits;
    private int nbProduits;
    protected void setup(){
        System.out.println("Hello! Agent "+getAID().getName()+" is ready.");
        //On crée une liste de produit à fabriquer
        produit p1 = new produit("p1", List.of("souder", "peindre"));
        produit p2 = new produit("p2",List.of("assembler", "peindre"));
        produit p3 = new produit("p3",List.of("assembler"));
        produit p4 = new produit("p4",List.of("assembler", "souder", "peindre"));
        this.produits = new ArrayList<>();
        this.produits.add(p1);
        this.produits.add(p2);
        this.produits.add(p3);
        this.produits.add(p4);
        this.nbProduits = this.produits.size();

        //On envoie un message a un robot par compétence en leur informant qu'il sont responsable d'une compétence.
        /* TODO : déléger les tâches pour ne pas surcharger l'atelier
        for(String competence : List.of("souder", "peindre", "assembler")){
            //On envoie un message au robot qui a la compétence

        } */
        this.addBehaviour(new sendProduct(this, 1000));
    }

    private class sendProduct extends TickerBehaviour {
        private Agent a;
        public sendProduct(Agent a, long period) {
            super(a, period);
            this.a = a;
        }

        protected void onTick() {
            if(produits.size() > 0){
                produit p = produits.get(0);
                HashMap<String,Integer> agentsScore = new HashMap<>();
                DFAgentDescription template1 = new DFAgentDescription();
                try {
                    DFAgentDescription[] result = DFService.search(this.a,template1);
                    for(DFAgentDescription agent : result){
                        agentsScore.put(agent.getName().getLocalName(), 0);
                        for (Iterator it = agent.getAllServices(); it.hasNext(); ) {
                            ServiceDescription service = (ServiceDescription) it.next();
                            //System.out.println("Agent "+agent.getName()+" as the competence "+service.getType()+" with a level of "+service.getName());
                            if(p.getSkills().containsKey(service.getType())){ // Si le produit contient la compétence de l'agent
                                agentsScore.put(agent.getName().getLocalName(), agentsScore.get(agent.getName().getLocalName()) + Integer.parseInt(service.getName()));//On augmente son score par la valeur de sa compétence
                            }
                        }
                    }
                } catch (FIPAException e) {
                    throw new RuntimeException(e);
                }
                //TODO : On envoie le produit au robot qui a le meilleur score
            }
            else{
                this.stop();
            }

        }
    }

}
