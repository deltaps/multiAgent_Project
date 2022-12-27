
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
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
        this.addBehaviour(new sendProduct(this, 10));
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
                HashMap<String,Float> agentsScore = new HashMap<>();
                DFAgentDescription template1 = new DFAgentDescription();
                try {
                    DFAgentDescription[] result = DFService.search(this.a,template1);
                    for(DFAgentDescription agent : result){
                        agentsScore.put(agent.getName().getLocalName(), 0.0f);
                        for (Iterator it = agent.getAllServices(); it.hasNext(); ) {
                            ServiceDescription service = (ServiceDescription) it.next();
                            //System.out.println("Agent "+agent.getName()+" as the competence "+service.getType()+" with a level of "+service.getName());
                            if(p.getSkills().containsKey(service.getType()) && !p.getSkills().get(service.getType())){ // Si le produit contient la compétence (non effectué) de l'agent
                                agentsScore.put(agent.getName().getLocalName(), agentsScore.get(agent.getName().getLocalName()) + Float.parseFloat(service.getName()));//On augmente son score par la valeur de sa compétence
                            }
                        }
                    }
                } catch (FIPAException e) {
                    throw new RuntimeException(e);
                }
                boolean notSend = true;
                while(notSend){
                    //On récupère l'agent avec le meilleur score
                    String sendAgent = "";
                    float maxScore = 0.0f;
                    for(String agent : agentsScore.keySet()){
                        if(agentsScore.get(agent) > maxScore){
                            maxScore = agentsScore.get(agent);
                            sendAgent = agent;
                        }
                    }
                    agentsScore.remove(sendAgent);
                    //On envoie le message à l'agent
                    ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                    message.addReceiver(new AID(sendAgent, AID.ISLOCALNAME));
                    try {
                        message.setContentObject(p);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    this.a.send(message);
                    //Attente de la réponse
                    ACLMessage reply = this.a.blockingReceive();
                    if(reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
                        System.out.println("Agent "+this.a.getLocalName()+" l'agent "+sendAgent+ " a accepté de fabriquer le produit "+p.getName());
                        produits.remove(p);
                        notSend = false;
                    }
                    else if(agentsScore.size() == 0){
                        notSend = false;
                    }
                }
            }
            else{
                this.stop();
            }

        }
    }

}
