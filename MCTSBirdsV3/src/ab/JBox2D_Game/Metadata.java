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
	private float damageMultiplierIce=1;
	private float damageMultiplierWood=1;
	private float damageMultiplierSnow=1;
	private float damageMultiplierStone=1;
	private float damageMultiplierSnowStatic=1;
	private float velocityMultiplierWood=1;
	private float velocityMultiplierIce=1;
	private float velocityMultiplierSnow=1;
	private float velocityMultiplierStone=1;
	private float velocityMultiplierSnowStatic=1;
	private float velocityMultiplierPigSmall=1;
	private float velocityMultiplierPigMeduim=1;
	private float velocityMultiplierPigBig=1;
	private float velocityMultiplierPigKing=1;
	private float velocityMultiplierPigMustache=1;
	private float velocityMultiplierPigHelmet=1;
    private int points=0;

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
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

	public void setdamageMultiplierIce(float damageMultiplierIce){
		this.damageMultiplierIce = damageMultiplierIce;
	}
	public float getdamageMultiplierIce(){
		return damageMultiplierIce;
	}
	public void setdamageMultiplierWood(float damageMultiplierWood){
		this.damageMultiplierWood = damageMultiplierWood;
	}
	public float getdamageMultiplierWood(){
		return damageMultiplierWood;
	}
	public void setdamageMultiplierSnow(float damageMultiplierSnow){
		this.damageMultiplierSnow = damageMultiplierSnow;
	}
	public float getdamageMultiplierSnow(){
		return damageMultiplierSnow;
	}
	
	public void setdamageMultiplierSnowStatic(float damageMultiplierSnowStatic){
		this.damageMultiplierSnowStatic = damageMultiplierSnowStatic;
	}
	public float getdamageMultiplierSnowStatic(){
		return damageMultiplierSnow;
	}
	
	public void setdamageMultiplierStone(float damageMultiplierStone){
		this.damageMultiplierStone = damageMultiplierStone;
	}
	public float getdamageMultiplierStone(){
		return damageMultiplierStone;
	}
	public void setvelocityMultiplierWood(float velocityMultiplierWood){
		this.velocityMultiplierWood = velocityMultiplierWood;
	}
	public float getvelocityMultiplierWood(){
		return velocityMultiplierWood;
	}
	public void setvelocityMultiplierIce(float velocityMultiplierIce){
		this.velocityMultiplierIce = velocityMultiplierIce;
	}
	public float getvelocityMultiplierIce(){
		return velocityMultiplierIce;
	}
	public void setvelocityMultiplierSnow(float velocityMultiplierSnow){
		this.velocityMultiplierSnow = velocityMultiplierSnow;
	}
	public float getvelocityMultiplierSnow(){
		return velocityMultiplierSnow;
	}
	public void setvelocityMultiplierStone(float velocityMultiplierStone){
		this.velocityMultiplierStone = velocityMultiplierStone;
	}
	public float getvelocityMultiplierStone(){
		return velocityMultiplierStone;
	}
	public void setvelocityMultiplierSnowStatic(float  velocityMultiplierSnowStatic){
		this. velocityMultiplierSnowStatic =  velocityMultiplierSnowStatic;
	}
	public float getvelocityMultiplierSnowStatic(){
		return  velocityMultiplierSnowStatic;
	}
	public void setvelocityMultiplierPigSmall(float velocityMultiplierPigSmall){
		this.velocityMultiplierPigSmall = velocityMultiplierPigSmall;
	}
	public float getdvelocityMultiplierPigSmall(){
		return velocityMultiplierPigSmall;
	}
	public void setvelocityMultiplierPigMeduim(float velocityMultiplierPigMeduim){
		this.velocityMultiplierPigMeduim = velocityMultiplierPigMeduim;
	}
	public float getvelocityMultiplierPigMeduim(){
		return velocityMultiplierPigMeduim;
	}
	public void setvelocityMultiplierPigBig(float velocityMultiplierPigBig){
		this.velocityMultiplierPigBig = velocityMultiplierPigBig;
	}
	public float getdvelocityMultiplierPigBig(){
		return velocityMultiplierPigBig;
	}
	public void setdvelocityMultiplierPigKing(float velocityMultiplierPigKing){
		this.velocityMultiplierPigKing = velocityMultiplierPigKing;
	}
	public float getvelocityMultiplierPigKing(){
		return velocityMultiplierPigKing;
	}
	public void setvelocityMultiplierPigMustache(float velocityMultiplierPigMustache){
		this.velocityMultiplierPigMustache = velocityMultiplierPigMustache;
	}
	public float getvelocityMultiplierPigMustache(){
		return velocityMultiplierPigMustache;
	}
	public void setvelocityMultiplierPigHelmet(float velocityMultiplierPigHelmet){
		this.velocityMultiplierPigHelmet = velocityMultiplierPigHelmet;
	}
	public float getvelocityMultiplierPigHelmet(){
		return velocityMultiplierPigHelmet;
	}
	
    public void setScore(int score)
    {
   	 points=score;
    }
    public int getScore()
    {
   	 return points;
    }
}
