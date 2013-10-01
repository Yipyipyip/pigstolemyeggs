package ab.demo;

public abstract class Agent implements Runnable {
public abstract String getName();
public abstract void setCurrent_level(int level);
public abstract int getCurrent_level();
public abstract void setIP(String ip);
public abstract void setID(int id);

}