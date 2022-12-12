import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.util.leap.Iterator;


public class playerAgent extends Agent {
    //Enregistrer l'agent dans les page jaune, dans une équipe qui n'est pas pleine (qui comporte moins de deux agents)

    protected void setup(){
        //On s'inscrit dans les pages jaunes, et on ajoute les comportement (on peu aussi faire des comportement qui font l'inscription)

        System.out.println("Hello! Agent "+getAID().getName()+" is ready.");
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Team");
        int team = (int)(Math.random() * 2) + 1;
        if(team == 1){
            sd.setName("Team1");
        } else{
            sd.setName("Team2");
        }
        template.addServices(sd);
        try{
            DFService.register(this, template);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        int wait = (int)(Math.random() * 1000) + 1;
        this.addBehaviour(new inscriptionBehaviour(this, wait));
    }

    private class inscriptionBehaviour extends WakerBehaviour{
        private Agent a;
        public inscriptionBehaviour(Agent a, long period) {
            super(a, period);
            this.a = a;
        }

        protected void onWake(){
            //On cherche les équipes qui ont moins de deux agents
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
            try{
                int nbTeam1 = 0;
                DFAgentDescription[] result = DFService.search(a,template1);
                for(DFAgentDescription agent : result){
                    for (Iterator it = agent.getAllServices(); it.hasNext(); ) {
                        ServiceDescription service = (ServiceDescription) it.next();
                        //System.out.println("Agent "+agent.getName()+" is in team "+service.getName());
                        nbTeam1++;
                    }
                }
                //System.out.println(nbTeam1);
                if(nbTeam1 > 2){
                    try{
                        DFService.deregister(a);
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }
                    System.out.println("Go team 2");
                    sd2.setName("Team2");
                    template2.addServices(sd2);
                    try{
                        DFService.register(a, template2);
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }
                }
            } catch (FIPAException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
