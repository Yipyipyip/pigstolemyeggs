package ab.JBox2D_Game;

import java.util.ArrayList;

import org.jbox2d.dynamics.BodyType;


public class Material {
private String name;
private BodyType bodyType;
private float defense;
private float density;
private float friction;
private float restitution;
private float strength;
public Material(String name, BodyType bodyType, float defense, float density, float friction, float restitution, float strength)
{
	this.setName(name);
	this.bodyType=bodyType;
	this.defense=defense;
	this.density=density;
	this.friction=friction;
	this.restitution=restitution;
	this.strength=strength;
}
public BodyType getBodyType() {
	return bodyType;
}
public void setBodyType(BodyType dynamic) {
	this.bodyType = dynamic;
}
public float getDefense() {
	return defense;
}
public void setDefense(float defense) {
	this.defense = defense;
}
public float getDensity() {
	return density;
}
public void setDensity(float density) {
	this.density = density;
}
public float getFriction() {
	return friction;
}
public void setFriction(float friction) {
	this.friction = friction;
}
public float getRestitution() {
	return restitution;
}
public void setRestitution(float restitution) {
	this.restitution = restitution;
}
public float getStrength() {
	return strength;
}
public void setStrength(float strength) {
	this.strength = strength;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
}
