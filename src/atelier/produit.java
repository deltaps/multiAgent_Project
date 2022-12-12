package atelier;

import java.util.List;

public class produit {

    private String name;
    private boolean free;//Est-ce que le produit est en cours de fabrication
    private boolean done;//Est-ce que le produit est fini
    private List<String> skills;//Liste des compétences nécessaires pour fabriquer le produit

    public produit(String name,List<String> skills){
        this.name = name;
        this.skills = skills;
        this.free = true;
        this.done = false;
    }

    public boolean isFree() {
        return free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
