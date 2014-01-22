package ab.JBox2D_Game;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.BodyType;


public class Objects {
    private String name;
    private String type;
    private Material material;
    private int maxHealth;
    private Shape shape;
    private float width = 0;
    private float height = 0;
    private float radius = 0;
    private int score;
    private int damagedFrameCount;
    private boolean circular;

    public Objects(String name, String type, Material material,
                   int maxHealth, Shape shape, float width, float height,
                   int score, int damagedFrameCount) {
        super();
        this.name = name;
        this.type = type;
        this.material = material;
        this.maxHealth = maxHealth;
        this.shape = shape;
        this.width = width;
        this.height = height;
        this.score = score;
        this.damagedFrameCount = damagedFrameCount;
        this.circular = false;
    }

    public Objects(String name, String type, Material material,
                   int maxHealth, Shape shape, float radius,
                   int score, int damagedFrameCount) {
        super();
        this.name = name;
        this.type = type;
        this.material = material;
        this.maxHealth = maxHealth;
        this.shape = shape;
        this.radius = radius;
        this.width = radius * 2;
        this.height = radius * 2;
        this.score = score;
        this.damagedFrameCount = damagedFrameCount;
        this.circular = true;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }


    public boolean isCircular() {
        return circular;
    }

    public void setCircular(boolean circular) {
        this.circular = circular;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getDamagedFrameCount() {
        return damagedFrameCount;
    }

    public void setDamagedFrameCount(int damagedFrameCount) {
        this.damagedFrameCount = damagedFrameCount;
    }

}
