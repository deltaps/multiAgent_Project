import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class robot extends Agent {

    private HashMap<String,Float> competences;//Dictionnaire avec en clé le nom de la compétence et en valeur le degrès de la compétence entre 0 et 1.
    private List<produit> produits;//File d'attente des produits que l'agent doit faire.
    private double time;//Temps que l'agent doit passé à faire un produit.
    protected void setup(){
        System.out.println("Hello! Agent "+getAID().getName()+" is ready.");
        this.produits = new ArrayList<>();
        //Assigner aléatoirement des compétences au robot --------------------
        List<String> allCompetences = List.of("souder", "peindre", "assembler");
        this.competences = new HashMap<>();
        for(String comp : allCompetences){
            int rand = (int)(Math.random() * 2) + 1;
            if(rand == 1){
                this.competences.put(comp, (float) Math.random());
            }
        }

        /*
        System.out.println("Agent "+getAID().getName()+" has the following competences :");
        for(String comp : this.competences.keySet()){
            System.out.println(comp+" : "+this.competences.get(comp));
        } */

        //Ajouter une description a l'agent pour chacun de ces comportements
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

        //On récupère le temps de fabrication d'un produit dans le fichier data.txt du dossier data ----
        try {
            FileReader fr = new FileReader("../data/data.txt");
            BufferedReader br = new BufferedReader(fr);
            String ligne;
            while ((ligne = br.readLine()) != null) {
                int index = ligne.indexOf("=");
                String key = (ligne.substring(0, index)).trim();
                String value = (ligne.substring(index + 1)).trim();
                if(key.equals("timeSkill")){
                    this.time = Double.parseDouble(value);
                }
                System.out.println(this.time);
            }
            br.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // --------------------------------------------------------------------------------------------------
        this.addBehaviour(new receptionMessage()); // Ajout du comportement de réception des messages
    }

    private class applySkills extends TickerBehaviour {
        private Agent a;
        public applySkills(Agent a, long period) {
            super(a, period);
            this.a = a;
        }

        @Override
        protected void onTick() {
            if(produits.size() > 0){
                produit p = produits.get(0);
                for(String comp : p.getSkills().keySet()){
                    if(p.getSkills().get(comp)){ // Si la compétence à déjà été appliqué
                        continue;
                    }
                    if(competences.containsKey(comp)){ // Si le robot a la compétence nécessaire
                        System.out.println("Agent "+getAID().getName()+" is applying the competence "+comp+" on the product "+p.getName());
                        //On attend le temps de fabrication d'un produit plus ou moins aléatoirement en fonction du degré de compétence du robot
                        try {
                            Thread.sleep((long) (time * (1 - competences.get(comp))));
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        p.finishSkill(comp);//On indique que la compétence a été appliqué
                    }
                }
                //A partir d'ici, soit le produit est entiérement fabriqué, soit il l'est partiellement, il faut donc informé et renvoyé le produit a l'atelier
                if(p.isDone()){
                    System.out.println("Agent "+getAID().getName()+" has finished the product "+p.getName());
                }
                else{
                    System.out.println("Agent "+getAID().getName()+" has partially finished the product "+p.getName());
                }
                //Envoie le produit par message a l'atelier
                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                message.addReceiver(new AID("bob", AID.ISLOCALNAME));
                try {
                    message.setContentObject(p);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                produits.remove(0);
            }
        }
    }

    private class receptionMessage extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if(msg != null){
                System.out.println("Agent "+getAID().getName()+" received a message from "+msg.getSender().getName());
                //Si la pile des produit est supérieur à trois, on répond que l'on ne peux pas prendre le produit
                if(msg.getContent().equals("newProduct")){
                    if(produits.size() > 3){
                        System.out.println("Agent "+getAID().getName()+" can't take the product because it has already 3 products to do");
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("I can't take this product");
                        send(reply);
                    }
                    else{
                        try {
                            System.out.println("Agent "+getAID().getName()+" is taking the product");
                            produit p = (produit) msg.getContentObject();
                            produits.add(p);
                            //Envoie un message acceptant le produit
                            ACLMessage reply = msg.createReply();
                            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                            reply.setContent("I can take this product");
                            send(reply);
                        } catch (UnreadableException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
}
