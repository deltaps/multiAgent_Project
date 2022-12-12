package old;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.ACLMessage;import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;


public class refereeAgent extends Agent {

    protected void setup(){
        System.out.println("Hello! Referee Agent "+getAID().getName()+" is ready.");
        //Insrcription de l'agent en temps qu'arbitre
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Referee");
        sd.setName(getName());
        template.addServices(sd);
        try{
            DFService.register(this, template);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        //Attendre un message d'un joueur
        ACLMessage message = null;
        while(message == null){
            message = receive();
        }
        this.addBehaviour(new inscriptionPlayerBehaviour(this,message));
    }

    private class inscriptionPlayerBehaviour extends Behaviour{

        private ACLMessage message;
        private Agent agent;

        public inscriptionPlayerBehaviour(Agent agent, ACLMessage message) {
            this.message = message;
            this.agent = agent;
        }

        @Override
        public void action() {

            //Afficher l'emmeteur du message
            System.out.println("Message received from "+message.getSender().getName());
            //Afficher le contenu du message
            System.out.println("Message content: "+message.getContent());
            //Rechercher les équipes qui ont moins de deux agents
            DFAgentDescription template1 = new DFAgentDescription();
            ServiceDescription sd1 = new ServiceDescription();
            sd1.setType("Team");
            sd1.setName("Team1");
            template1.addServices(sd1);

            DFAgentDescription template2 = new DFAgentDescription();
            ServiceDescription sd2 = new ServiceDescription();
            sd2.setType("Team");
            sd2.setName("Team2");
            template2.addServices(sd2);
            int nbTeam1 = 0;
            boolean alreadyInTeam = false;
            try {
                DFAgentDescription[] result = DFService.search(agent,template1); //On récupère la liste des joueurs dans l'équipe 1
                for(DFAgentDescription agent : result){
                    for (Iterator it = agent.getAllServices(); it.hasNext(); ) {
                        ServiceDescription service = (ServiceDescription) it.next();
                        if(agent.getName().getLocalName().equals(message.getSender().getLocalName())){
                            System.out.println("Agent "+agent.getName()+" is already in team "+service.getName());
                            alreadyInTeam = true;
                            break;
                        }
                        nbTeam1++;
                    }
                    if(alreadyInTeam){
                        break;
                    }
                }
                //Maintenant que nous avons le nombre de joueur dans la team1, on peut ajouter le joueur dans l'une des deux team
                if(!alreadyInTeam){
                    if(nbTeam1 < 2){
                        //On ajoute le joueur dans l'équipe 1
                        System.out.println("Agent "+message.getSender().getName()+" is in team 1");
                        try{
                            //On modifie le service de l'agent
                            DFService.modify(agent, template1);
                        }
                        catch (FIPAException fe) {
                            fe.printStackTrace();
                        }
                    } else{
                        //On ajoute le joueur dans l'équipe 2
                        System.out.println("Agent "+message.getSender().getName()+" is in team 2");
                        try{
                            DFService.modify(agent, template2);
                        }
                        catch (FIPAException fe) {
                            fe.printStackTrace();
                        }
                    }
                }
            } catch (FIPAException e) {
                throw new RuntimeException(e);
            }
        }

        public int nbTeam1(){
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Team");
            sd.setName("Team1");
            return countAllType(template, sd);
        }

        public int nbTeam2(){
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Team");
            sd.setName("Team2");
            return countAllType(template, sd);
        }

        private int countAllType(DFAgentDescription template, ServiceDescription sd) {
            template.addServices(sd);
            int nbTeam = 0;
            try {
                DFAgentDescription[] result = DFService.search(agent,template); //On récupère la liste des joueurs dans l'équipe 1
                for(DFAgentDescription agent : result){
                    for (Iterator it = agent.getAllServices(); it.hasNext(); ) {
                        ServiceDescription service = (ServiceDescription) it.next();
                        nbTeam++;
                    }
                }
            } catch (FIPAException e) {
                throw new RuntimeException(e);
            }
            return nbTeam;
        }

        @Override
        public boolean done() {
            return nbTeam1() == 2 && nbTeam2() == 2;
        }
    }
}
