package atelier;

import jade.core.Agent;

import java.util.List;

public class atelier extends Agent {

    private List<produit> produits;
    protected void setup(){
        System.out.println("Hello! Agent "+getAID().getName()+" is ready.");
        //On crée une liste de produit à fabriquer
        produit p1 = new produit("p1", List.of("souder", "peindre"));
        produit p2 = new produit("p2",List.of("assembler", "peindre"));
        produit p3 = new produit("p3",List.of("assembler"));
        produit p4 = new produit("p4",List.of("assembler", "souder", "peindre"));
        this.produits.add(p1);
        this.produits.add(p2);
        this.produits.add(p3);
        this.produits.add(p4);

        //On envoie un message a un robot par compétence en leur informant qu'il sont responsable d'une compétence.
    }
}
