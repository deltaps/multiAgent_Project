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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class atelier extends Agent {

    private List<produit> produits;
    private List<produit> finishedProduits;
    private List<produit> trashProduits;
    private int nbProduits;
    private HashMap<String,HashMap<String,Float>> agentScores;
    protected void setup(){
        System.out.println("Hello! Agent "+getAID().getName()+" is ready.");
        //On récupère la liste des produit dans le fichier de configuration ---
        HashMap<String, ArrayList<String>> products = new HashMap<>();
        try {
            FileReader fr = new FileReader("../configurations/configuration.txt");
            BufferedReader br = new BufferedReader(fr);
            String ligne;
            while ((ligne = br.readLine()) != null) {
                int index = ligne.indexOf("=");
                String key = (ligne.substring(0, index)).trim();
                String value = (ligne.substring(index + 1));
                if(key.equals("produit")){
                    Pattern pattern = Pattern.compile("\\b(\\w+):\\(([^)]+)\\)");
                    Matcher matcher = pattern.matcher(value);
                    while(matcher.find()){
                        String productName = matcher.group(1);
                        String competencesString = matcher.group(2);
                        String[] competencesArray = competencesString.split(",");
                        ArrayList<String> competences = new ArrayList<>(Arrays.asList(competencesArray));
                        products.put(productName, competences);
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // --------------------------------------------------------------------
        //On crée une liste de produit à fabriquer en fonction du fichier de configuration ----
        this.produits = new ArrayList<>();
        for(String productName : products.keySet()){
            this.produits.add(new produit(productName, products.get(productName)));
        }
        this.nbProduits = this.produits.size();
        this.finishedProduits = new ArrayList<>();
        this.trashProduits = new ArrayList<>();
        this.agentScores = new HashMap<>();
        //Comportements ------
        //Comportement qui envoie les produits au robot de manière intéligente
        this.addBehaviour(new sendProduct(this, 100));
        //Comportement pour recevoir les message des robot
        this.addBehaviour(new receptionMessage(this));
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
                        if(agentsScore.get(agent.getName().getLocalName()) == 0.0f){
                            agentsScore.remove(agent.getName().getLocalName());
                        }
                    }
                } catch (FIPAException e) {
                    throw new RuntimeException(e);
                }
                if(agentsScore.size() == 0) { // Dans ce cas aucun agent ne peut faire le produit
                    System.out.println("Aucun agent ne peut effectuer le produit " + p.getName());
                    trashProduits.add(p);
                    produits.remove(p);
                }
                else{
                    agentScores.put(p.getName(), agentsScore);
                    //On regarde le robot qui est le plus apt a faire le produit
                    String sendAgent = "";
                    float maxScore = 0.0f;
                    for(String agent : agentsScore.keySet()){
                        if(agentsScore.get(agent) > maxScore){
                            maxScore = agentsScore.get(agent);
                            sendAgent = agent;
                        }
                    }
                    agentsScore.remove(sendAgent);
                    agentScores.remove(sendAgent);
                    produits.remove(p);
                    //On envoie le message à l'agent
                    ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                    message.addReceiver(new AID(sendAgent, AID.ISLOCALNAME));
                    try {
                        message.setContentObject(p);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    this.a.send(message);
                }
            }
            else{
                if(finishedProduits.size() + trashProduits.size() == nbProduits){
                    System.out.println("Tous les produits ont été fabriqués");
                    System.out.println("Produits finis :");
                    for(produit p : finishedProduits){
                        System.out.println(p.getName());
                    }
                    if(trashProduits.size() > 0){
                        System.out.println("Les produits suivants n'ont pas pu être fabriqués :");
                        for(produit p : trashProduits){
                            System.out.println(p.getName());
                        }
                    }
                    this.stop();
                }
            }
        }
    }

    private class receptionMessage extends CyclicBehaviour {

        private Agent a;
        public receptionMessage(Agent a) {
            this.a = a;
        }
        @Override
        public void action() {
            ACLMessage msg = receive();
            if(msg != null){
                if(msg.getPerformative() == ACLMessage.REFUSE){
                    produit p;
                    try {
                        p = (produit) msg.getContentObject();
                    } catch (UnreadableException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Agent " + msg.getSender().getLocalName() + " refuse de fabriquer le produit " + p.getName());
                    //On récupère l'agent avec le meilleur score
                    String sendAgent = "";
                    float maxScore = 0.0f;
                    for(String agent : agentScores.get(p.getName()).keySet()){
                        if(agentScores.get(p.getName()).get(agent) > maxScore){
                            maxScore = agentScores.get(p.getName()).get(agent);
                            sendAgent = agent;
                        }
                    }
                    if(sendAgent != ""){
                        //On envoie le message à l'agent
                        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                        message.addReceiver(new AID(sendAgent, AID.ISLOCALNAME));
                        try {
                            message.setContentObject(p);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        agentScores.remove(p.getName());
                        System.out.println("Envoi du produit "+p.getName()+" à l'agent "+sendAgent);
                        this.a.send(message);
                    }
                    else{
                        System.out.println("Aucun agent n'a pu prendre le produit "+p.getName());
                        agentScores.remove(p.getName());
                        produits.add(p);
                    }
                }
                else if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
                    produit p = null;
                    try {
                        p = (produit) msg.getContentObject();
                    } catch (UnreadableException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Agent "+this.a.getLocalName()+" l'agent "+msg.getSender().getLocalName()+ " a accepté de fabriquer le produit "+p.getName());
                    //if(produits.size() != 0){
                        //produits.remove(0);
                    //}
                    agentScores.remove(p.getName());
                }
                else{
                    System.out.println("Agent "+getAID().getName()+" received a message from "+msg.getSender().getName());
                    //On récupère le produit contenue dans le message
                    produit produit;
                    try {
                        produit = (produit) msg.getContentObject();
                    } catch (UnreadableException e) {
                        throw new RuntimeException(e);
                    }
                    //On vérifie si le produit est terminé
                    if(produit.isDone()){
                        System.out.println("le produit "+produit.getName()+" est terminé");
                        finishedProduits.add(produit);
                    }
                    else{
                        System.out.println("le produit "+produit.getName()+" n'est pas terminé");
                        produits.add(produit);
                    }
                }
            }
        }
    }

}
