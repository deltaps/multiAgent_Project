package old;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.lang.acl.ACLMessage;

import java.lang.reflect.Array;

public class playerAgent extends Agent{



    protected void setup(){
        System.out.println("Hello! player Agent "+getAID().getName()+" is ready.");

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Team");
        sd.setName("NoTeam");
        template.addServices(sd);
        try{
            DFService.register(this, template);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        int wait = (int)(Math.random() * 1000) + 1;
        this.addBehaviour(new sendMessageInscriptionBehaviour(this, wait));
    }

    private class sendMessageInscriptionBehaviour extends WakerBehaviour{

        public sendMessageInscriptionBehaviour(Agent a, long timeout) {
            super(a, timeout);
        }

        protected void onWake(){
            //On envoie un message à l'arbitre pour s'inscrire
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.addReceiver(new AID("bob", AID.ISLOCALNAME));
            message.setContent("inscription");
            send(message);
        }
    }

}
