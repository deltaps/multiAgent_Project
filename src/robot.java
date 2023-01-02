import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class robot extends Agent {

    private HashMap<String,Float> competences;//Dictionnaire avec en clé le nom de la compétence et en valeur le degrès de la compétence entre 0 et 1.
    private List<produit> produits;//File d'attente des produits que l'agent doit faire.
    private double time;//Temps que l'agent doit passer à faire un produit.
    protected void setup(){
        System.out.println("Hello! Agent "+getAID().getName()+" is ready.");

        //On récupère le temps de fabrication d'un produit, ainsi que les différentes compétences dans le fichier configuration.txt du dossier data ----
        List<String> allCompetences = new ArrayList<>();
        try {
            FileReader fr = new FileReader("../configurations/configuration.txt");
            BufferedReader br = new BufferedReader(fr);
            String ligne;
            while ((ligne = br.readLine()) != null) {
                int index = ligne.indexOf("=");
                String key = (ligne.substring(0, index)).trim();
                String value = (ligne.substring(index + 1)).trim();
                if(key.equals("timeSkill")){
                    this.time = Double.parseDouble(value);
                }
                if(key.equals("competences")){
                    String[] competencesList = value.split(",");
                    for (String s : competencesList) {
                        allCompetences.add(s.trim());
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // --------------------------------------------------------------------------------------------------
        this.produits = new ArrayList<>(); // Iinitialisation de la file d'attente des produits.
        //Assigner aléatoirement des compétences au robot --------------------
        this.competences = new HashMap<>();
        for(String comp : allCompetences){
            int rand = (int)(Math.random() * 2) + 1;
            if(rand == 1){
                this.competences.put(comp, (float) Math.random());
            }
        }
        System.out.println("Robot " + this.getAID().getLocalName() + " a les compétences : " + this.competences.toString());

        //Ajouter une description à l'agent pour chacun de ces comportements
        DFAgentDescription template = new DFAgentDescription();
        for(String comp : competences.keySet()){
            ServiceDescription sd = new ServiceDescription();
            sd.setType(comp);
            sd.setName(this.competences.get(comp).toString());
            template.addServices(sd);
        }
        try {
            DFService.register(this, template);
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }
        // ----------------------------------------------------------------

        this.addBehaviour(new receptionMessage()); // Ajout du comportement de réception des messages
        this.addBehaviour(new applySkills(this, 1000));// Ajout du comportement d'application des compétences pour la liste des produits
    }

    private class applySkills extends TickerBehaviour {
        public applySkills(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            if(produits.size() > 0){ // Si l'agent a des produits à faire
                produit p = produits.get(0);
                for(String comp : p.getSkills().keySet()){ // On va faire le maximum de compétence possible pour le produit, donc on boucle sur toutes les compétences du produit
                    if(p.getSkills().get(comp)){ // Si la compétence a déjà été appliqué, on passe à la suivante
                        continue;
                    }
                    if(competences.containsKey(comp)){ // Si le robot a la compétence nécessaire
                        System.out.println("Agent "+getAID().getName()+" is applying the competence "+comp+" on the product "+p.getName());
                        //On attend le temps de fabrication d'un produit plus ou moins longtemps en fonction du degré de compétence du robot
                        try {
                            Thread.sleep((long) (time * (1 - competences.get(comp)))); // Le temps est plus ou moins long en fonction du niveau de compétence du robot
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        p.finishSkill(comp);//On indique que la compétence a été appliqué
                    }
                }
                //À partir d'ici, soit le produit est entièrement fabriqué, soit il l'est partiellement, il faut donc informer et renvoyé le produit à l'atelier
                if(p.isDone()){
                    System.out.println("Agent "+getAID().getName()+" has finished the product "+p.getName());
                }
                else{
                    System.out.println("Agent "+getAID().getName()+" has partially finished the product "+p.getName());
                }
                //Envoie le produit par message à l'atelier
                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                message.addReceiver(new AID("eva", AID.ISLOCALNAME));
                try {
                    message.setContentObject(p);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                send(message);
                produits.remove(0);//On retire le produit de la file d'attente
                System.out.println("Agent "+getAID().getName()+" has sent the product "+p.getName()+" to the workshop");
            }
        }
    }

    private class receptionMessage extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if(msg != null){ // Le robot ne peut recevoir qu'un type de message de l'atelier, ce message est une demande de produit
                System.out.println("Agent "+getAID().getName()+" received a message from "+msg.getSender().getName());
                //Si la pile des produits est supérieure à trois, on répond que l'on ne peut pas prendre le produit
                if(produits.size() >= 3){
                    System.out.println("Agent "+getAID().getName()+" can't take the product because it has already 3 products to do");
                    produit p;
                    try {
                        p = (produit) msg.getContentObject();
                    } catch (UnreadableException e) {
                        throw new RuntimeException(e);
                    }
                    ACLMessage reply = new ACLMessage(ACLMessage.REFUSE); // On envoie un message de refus
                    reply.addReceiver(new AID("eva", AID.ISLOCALNAME));
                    try {
                        reply.setContentObject(p);//On ajoute le produit à refuser
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    send(reply);
                }
                else{ // Si le robot peut prendre le produit
                    try {
                        System.out.println("Agent "+getAID().getName()+" is taking the product");
                        produit p = (produit) msg.getContentObject();
                        produits.add(p); // On ajoute le produit à la file d'attente
                        //Envoie un message acceptant le produit
                        ACLMessage reply = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                        reply.addReceiver(new AID("eva", AID.ISLOCALNAME));
                        reply.setContentObject(p);
                        send(reply);
                    } catch (UnreadableException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
    protected void takeDown(){
        System.out.println("Agent "+getAID().getName()+" terminating.");
    }
}
