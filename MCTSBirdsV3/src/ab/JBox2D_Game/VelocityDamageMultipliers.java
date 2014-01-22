package ab.JBox2D_Game;

public class VelocityDamageMultipliers {
private Material type;
private float multiplier;
private boolean damage;
public Material getType() {
	return type;
}
public void setType(Material type) {
	this.type = type;
}
public float getMultiplier() {
	return multiplier;
}
public void setMultiplier(float multiplier) {
	this.multiplier = multiplier;
}
public boolean isDamage() {
	return damage;
}
public void setDamage(boolean damage) {
	this.damage = damage;
}
public VelocityDamageMultipliers(Material type, float multiplier, boolean damage) {
	super();
	this.type = type;
	this.multiplier = multiplier;
	this.damage = damage;
}
}
