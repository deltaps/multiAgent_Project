import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;


public class myAgent extends Agent {

    protected void setup() {
        System.out.println("Hello! Agent "+getAID().getName()+" is ready.");
        this.addBehaviour(new myBehaviour(this, 1000));
    }
    protected void takeDown() {
        System.out.println("Agent "+getAID().getName()+" terminating.");
    }

    private class myBehaviour extends TickerBehaviour{
        public myBehaviour(Agent a, long period) {
            super(a, period);
        }

        protected void onTick() {
            System.out.println("Hello! Agent "+myAgent.getAID().getName()+" is ready.");
        }
    }
}




