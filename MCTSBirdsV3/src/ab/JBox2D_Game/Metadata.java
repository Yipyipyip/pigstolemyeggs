package ab.JBox2D_Game;

/**
 * Class Metadata
 * Author: Michael Tannenbaum
 * Date: 13.01.14
 * Time: 16:59
 */
public class Metadata {
    private String material, name = "";
    private float defense, strength = 0;
    private int score = 0;

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getDefense() {
        return defense;
    }

    public void setDefense(float defense) {
        this.defense = defense;
    }

    public float getStrength() {
        return strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }
}
