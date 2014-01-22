package ab.JBox2D_Game;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;

public class Game {
    ABContactListener contactListener;
    private int birdsNumber = 0;
    private JsonObject shapes;
    private JsonObject objects;
    private JsonObject materials;
    private JsonObject level;
    private float mod = (float) 20;
    private ArrayList<Body> birds, bodiesToDelete, bodiesToExplode;
    private int chunks = 5;
    private JsonElement materialsElement = null;
    private JsonElement obejctsElement = null;
    private JsonElement shapesElement = null;
    private World world;
    private int stageNumber;
    private int levelNumber;
    private boolean shotFinished;
    private Body activeBird;
    private Vec2 slingPos = new Vec2(0,0);

    public Game(int stage, int level) {
        stageNumber = stage;
        levelNumber = level;
        initWorld();
    }

    public void initWorld() {
        world = new World(new Vec2(0, -9.8f));
        birds = new ArrayList<Body>();
        bodiesToDelete = new ArrayList<Body>();
        bodiesToExplode = new ArrayList<Body>();
        JsonParser parser = new JsonParser();

        try {
            materialsElement = parser.parse(new FileReader(new File("JSON/materials.json")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        materials = materialsElement.getAsJsonObject();

        try {
            obejctsElement = parser.parse(new FileReader(new File("JSON/Objects.json")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        objects = obejctsElement.getAsJsonObject();

        try {
            shapesElement = parser.parse(new FileReader(new File("JSON/Shapes.json")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        shapes = shapesElement.getAsJsonObject();
        JsonElement levelElement = null;
        try {
            levelElement = parser.parse(new FileReader(new File("JSON/Level" + stageNumber + "-" + levelNumber + ".json")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        level = levelElement.getAsJsonObject();
        JsonObject JSONWorld = level.get("world").getAsJsonObject();
        contactListener = new ABContactListener(bodiesToDelete, bodiesToExplode);
        world.setContactListener(contactListener);
        for (Map.Entry<String, JsonElement> entry : JSONWorld.entrySet()) {
            String key = entry.getKey();
            Metadata meta = new Metadata();
            meta.setName(key);
            JsonObject value = entry.getValue().getAsJsonObject();
            // read level
            String id = value.get("id").getAsString();
            float angle = value.get("angle").getAsFloat();
            float x = value.get("x").getAsFloat();
            float y = value.get("y").getAsFloat();
            // read objects
            if (id.toLowerCase().equals("slingshot")) {
                slingPos.set(x,y);
                continue;
            }
            JsonObject object = objects.getAsJsonObject(id);
            String material = object.get("material").getAsString();
            String shape = object.get("shape").getAsString();
            meta.setScore(object.get("score").getAsInt());
            // read material
            meta.setMaterial(material);
            JsonObject materialObject = materials.getAsJsonObject(material);
            String bodyType = materialObject.get("bodyType").getAsString();
            float density = materialObject.get("density").getAsFloat();
            float friction = materialObject.get("friction").getAsFloat();
            float restitution = materialObject.get("restitution").getAsFloat();
            meta.setMaterial(material);
            meta.setStrength(materialObject.get("strength").getAsFloat());
            meta.setDefense(materialObject.get("defense").getAsFloat());
            // read shape
            JsonObject shapeObject = shapes.getAsJsonObject(shape);
            String type = shapeObject.get("type").getAsString();
            BodyDef bd = new BodyDef();
            FixtureDef fd = new FixtureDef();
            if (type.equals("CIRCLE")) {
                float offsetX = shapeObject.get("offsetX").getAsFloat();
                float offsetY = shapeObject.get("offsetY").getAsFloat();
                float radius = shapeObject.get("radius").getAsFloat() / mod;
                CircleShape s = new CircleShape();
                s.m_p.x = offsetX / mod;
                s.m_p.y = offsetY / mod;
                s.setRadius(radius);
                fd.shape = s;
            } else if (type.equals("RECTANGLE")) {
                float width = shapeObject.get("width").getAsFloat();
                float height = shapeObject.get("height").getAsFloat();
                PolygonShape s = new PolygonShape();

                s.setAsBox(width / (2 * mod), height / (2 * mod));
                fd.shape = s;
            } else if (type.equals("POLYGON")) {
                JsonArray vertices = shapeObject.get("vertices").getAsJsonArray();
                PolygonShape s = new PolygonShape();
                ArrayList<Vec2> vertList = new ArrayList<Vec2>();
                for (int i = 0; i < vertices.size(); i++) {
                    JsonArray point = vertices.get(i).getAsJsonArray();
                    float px = point.get(0).getAsFloat();
                    float py = point.get(1).getAsFloat();
                    vertList.add(new Vec2(px, py));
                }
                s.set(vertList.toArray(new Vec2[]{}), vertList.size());
                fd.shape = s;
            }
            bd.type = BodyType.valueOf(bodyType);
            bd.angle = (float) ((-angle) * Math.PI / 180);
            fd.density = density;
            fd.friction = friction;
            fd.restitution = restitution;
            bd.position.set(x, -y);
            Body body = world.createBody(bd);
            body.createFixture(fd);
            body.setUserData(meta);
            if (material.startsWith("BIRD")) {
                birds.add(body);
            }
        }
        BodyDef bd = new BodyDef();
        FixtureDef fd = new FixtureDef();
        bd.type = BodyType.STATIC;
        fd.density = (float) 0.5;
        fd.friction = (float) 0.8;
        fd.restitution = 0;
        PolygonShape s = new PolygonShape();
        s.setAsBox(12500, 1000);
        fd.shape = s;
        bd.position.set(-2500, -1000);
        world.createBody(bd).createFixture(fd);
        birdsNumber = birds.size();
        Body rightmostBird = birds.get(0);
        for (Body bird : birds) {
            if (bird.getPosition().x>rightmostBird.getPosition().x) {
                rightmostBird = bird;
            }
        }
        setActiveBird(rightmostBird);
        UpdateThread updateThread = new UpdateThread(this, bodiesToDelete, bodiesToExplode);
        updateThread.start();
    }

    public void shoot(double theta, Body bird, float taptime) {
        System.out.println("Shoot at "+theta+" degrees, taptime: "+taptime);
        contactListener.setBirdShot(true);
        birdsNumber--;
        float impulse = (float) 30 * bird.getMass();
        theta = theta * Math.PI / 180.;
        // simulate tap
        bird.applyLinearImpulse(new Vec2((float) Math.cos(theta) * impulse, (float) Math.sin(theta) * impulse), bird.getPosition());
        float timeStep = taptime;     //the length of time passed to simulate (seconds)
        int velocityIterations = 8;   //how strongly to correct velocity
        int positionIterations = 3;   //how strongly to correct position
        world.step(timeStep, velocityIterations, positionIterations);
        String material = ((Metadata)bird.getUserData()).getMaterial();
        if (material.equals("BIRD_YELLOW")) {
            yellowBird(bird);
        } else if (material.equals("BIRD_BLUE")) {
            blueBird(bird);
        } else if (material.equals("BIRD_WHITE")) {
            whiteBird(bird);
        }
        // wait for shot to finish
        while (activeBird.getLinearVelocity().length() > 1) {
            timeStep = 5;      //the length of time passed to simulate (seconds)
            velocityIterations = 8;   //how strongly to correct velocity
            positionIterations = 3;   //how strongly to correct position
            world.step(timeStep, velocityIterations, positionIterations);
        }
        if (bird == getActiveBird()) {
            birds.remove(bird);
            world.destroyBody(bird);
            Body rightmostBird = birds.get(0);
            for (Body body : birds) {
                if (body.getPosition().x>rightmostBird.getPosition().x) {
                    rightmostBird = body;
                }
            }
            setActiveBird(rightmostBird);
            shotFinished = true;
        }
        //System.err.println(bird.getLinearVelocity());
    }

    public void explode(Body exploding) {
        for (int i = 0; i < chunks; i++) {
            float angle = (float) ((i / (float) chunks) * 360 * Math.PI / 180);
            Vec2 direction = new Vec2((float) Math.sin(angle), (float) Math.cos(angle));
            float blastPower = 10;
            Metadata bomb = new Metadata();
            bomb.setDefense(0);
            bomb.setStrength(1);
            bomb.setName("Bomb");
            bomb.setMaterial("Bomb");
            BodyDef bd = new BodyDef();
            bd.type = BodyType.DYNAMIC;
            bd.fixedRotation = true;
            bd.bullet = true;
            bd.position = exploding.getPosition(); // start at blast center
            bd.linearVelocity = new Vec2(blastPower * direction.x, blastPower * direction.y);
            bd.userData = bomb;
            bd.angle = 0.f;

            CircleShape circleShape = new CircleShape();
            circleShape.m_radius = (float) 0.5;

            FixtureDef fd = new FixtureDef();
            fd.shape = circleShape;

            fd.density = exploding.m_fixtureList.getDensity();
            fd.friction = exploding.m_fixtureList.getFriction();
            fd.restitution = 0;
            fd.filter.groupIndex = -1;
            fd.userData = bomb;
            this.getWorld().createBody(bd).createFixture(fd);
        }
        bodiesToDelete.add(exploding);
    }

    void whiteBird(Body whiteBird) {
        whiteBirdEgg(whiteBird);
        shoot(30, whiteBird, 0);
    }

    void blueBird(Body blueBird) {
        blueBirdSplit(blueBird, (20));
        blueBirdSplit(blueBird, (-20));
    }

    void yellowBird(Body yellowBird) {
        float impulse = (float) 37.5 * yellowBird.getMass();
        Double theta = Math.atan((yellowBird.getLinearVelocity().y - yellowBird.getPosition().y) / (yellowBird.getLinearVelocity().x - yellowBird.getPosition().x));
        theta = theta * Math.PI / 180.;
        yellowBird.applyLinearImpulse(new Vec2((float) Math.cos(theta) * impulse, (float) Math.sin(theta) * impulse), yellowBird.getPosition());
    }

    public int getEndScore() {
        int r = contactListener.getPoints();
        r += (10000 * birdsNumber);
        return r;
    }

    public Body blueBirdSplit(Body dub, double angle) {

        FixtureDef fd = new FixtureDef();
        BodyDef bd = new BodyDef();
        bd.position = new Vec2(dub.getPosition().x, dub.getPosition().y);
        bd.type = dub.getType();
        Metadata meta = new Metadata();
        Metadata m2 = (Metadata) dub.getUserData();
        meta.setDefense(m2.getDefense());
        meta.setMaterial(m2.getMaterial());
        meta.setName(m2.getName());
        meta.setStrength(m2.getStrength());
        meta.setScore(m2.getScore());
        bd.userData = meta;
        fd.density = dub.getFixtureList().m_density;
        fd.friction = dub.getFixtureList().m_friction;
        fd.restitution = dub.getFixtureList().m_restitution;
        bd.angle = dub.getAngle();
        CircleShape circle = new CircleShape();
        circle.setRadius(dub.getFixtureList().getShape().getRadius());
        fd.shape = circle;
        Body r = this.getWorld().createBody(bd);
        r.createFixture(fd);
        angle = (angle * Math.PI / 180.);
        float x = (float) (dub.getLinearVelocity().x * Math.cos(angle) - dub.getLinearVelocity().y * Math.sin(angle));
        float y = (float) (dub.getLinearVelocity().x * Math.sin(angle) + dub.getLinearVelocity().y * Math.cos(angle));
        r.setLinearVelocity(new Vec2(x, y));
        return r;
    }

    public Body whiteBirdEgg(Body dub) {

        FixtureDef fd = new FixtureDef();
        BodyDef bd = new BodyDef();
        bd.position = new Vec2(dub.getPosition().x, dub.getPosition().y);
        bd.type = dub.getType();
        Metadata meta = new Metadata();
        Metadata m2 = (Metadata) dub.getUserData();
        meta.setDefense(m2.getDefense());
        meta.setMaterial(m2.getMaterial());
        meta.setName("EGG");
        meta.setStrength(m2.getStrength());
        meta.setScore(m2.getScore());
        bd.userData = meta;
        fd.density = dub.getFixtureList().m_density;
        fd.friction = dub.getFixtureList().m_friction;
        fd.restitution = dub.getFixtureList().m_restitution;
        bd.angle = dub.getAngle();
        CircleShape circle = new CircleShape();
        circle.setRadius(dub.getFixtureList().getShape().getRadius());
        fd.shape = circle;
        Body r = this.getWorld().createBody(bd);
        r.createFixture(fd);
        return r;
    }

    public World getWorld() {
        return world;
    }

    public Body getActiveBird() {
        return activeBird;
    }

    public void setActiveBird(Body activeBird) {
        this.activeBird = activeBird;
        activeBird.setType(BodyType.STATIC);
        activeBird.getPosition().set(slingPos);
        activeBird.synchronizeTransform();
        activeBird.setType(BodyType.DYNAMIC);
    }

    public boolean isWon() {
        Body bodylist = world.getBodyList();
        for (int i = 0; i < world.getBodyCount(); ++i) {
            if (bodylist.getUserData()!=null && bodylist.getUserData().getClass()==Metadata.class && ((Metadata)bodylist.getUserData()).getMaterial().startsWith("PIG")) {
                return false;
            }
            bodylist = bodylist.getNext();
        }
        return true;
    }

    public boolean isLost() {
        return birds.size() == 1 && !isWon();
    }
}
