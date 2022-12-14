import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class produit implements Serializable {

    private String name;
    private boolean free;//Est-ce que le produit est en cours de fabrication
    private boolean done;//Est-ce que le produit est fini
    private HashMap<String,Boolean> skills;//Liste des compétences nécessaires pour fabriquer le produit

    public produit(String name,List<String> skills){
        this.name = name;
        this.skills = new HashMap<>();
        for(String skill : skills){
            this.skills.put(skill, false);
        }
        this.free = true;
        this.done = false;
    }

    public boolean isFree() {
        return free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    public boolean isDone() {// Le produit est fini si toutes les compétences sont terminées
        for (String skill : skills.keySet()) {
            if(!skills.get(skill)){
                return false;
            }
        }
        this.done = true;
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public HashMap<String,Boolean> getSkills() {
        return skills;
    }

    public void setSkills(HashMap<String,Boolean> skills) {
        this.skills = skills;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void finishSkill(String skill){
        this.skills.put(skill, true);
    }
}
