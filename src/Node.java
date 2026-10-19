import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

import java.util.HashSet;
import java.util.Set;

public abstract class Node {

    private static Sound placedSound;
    private static Sound breakSound;
    private static Sound fixedSound;

    public static void setSounds(Sound placed, Sound broken, Sound fixed) {
        placedSound = placed;
        breakSound = broken;
        fixedSound = fixed;
    }

    // ------------------------------------------------------------- variables
    private float x;                    
    private float y;                    
    private String nodeType;          
    private float userAcceptRate;       
    private float processRate;          
    private float stability;          
    private float simulateErrorRate;   
    private float size;                 
    private Node[] connections;         
    private int userCapacity;           
    private Texture texture;          

    private boolean broken = false;
    private final Set<Node> blockedNodes = new HashSet<>();  

    protected Node(float x, float y, String nodeType, float userAcceptRate, float processRate,
                   float stability, float simulateErrorRate, float size,
                   int connectionSlots, int userCapacity) {
        if (connectionSlots != 4 && connectionSlots != 6) {
            throw new IllegalArgumentException("connectionSlots must be 4 or 6, got " + connectionSlots);
        }
        this.x = x;
        this.y = y;
        this.nodeType = nodeType;
        this.userAcceptRate = userAcceptRate;
        this.processRate = processRate;
        this.stability = stability;
        this.simulateErrorRate = simulateErrorRate;
        this.size = size;
        this.connections = new Node[connectionSlots];
        this.userCapacity = userCapacity;
    }

    // ------------------------------------------------------------- functions
    public void simulateError(float errorRate, float stability) {
    }

    public int processUser(float processRate, float userAcceptRate, int userCapacity) {
        int handled = Math.round(processRate * userAcceptRate);
        return Math.max(0, Math.min(handled, userCapacity));
    }

    public void soundBreak() {
        if (breakSound != null) breakSound.play();
    }

    public void soundFixed() {
        if (fixedSound != null) fixedSound.play();
    }

    public void soundPlaced() {
        if (placedSound != null) placedSound.play();
    }

    public void broadcastBreak(Node[] connections, String nodeType, Node node) {
        for (Node neighbour : connections) {
            if (neighbour != null && neighbour != node) {
                neighbour.recieveBreak(node, neighbour.connections);
            }
        }
    }

    public void recieveBreak(Node node, Node[] connections) {
        if (node == null || node == this) return;
        stopSending(node);
    }

    public void stopSending(Node node) {
        blockedNodes.add(node);
    }

    public boolean canSendTo(Node node) {
        return node != null && !blockedNodes.contains(node);
    }

    public void setConnection(int slot, Node node) {
        connections[slot] = node;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public void setPosition(float x, float y) { this.x = x; this.y = y; }

    public String getNodeType() { return nodeType; }

    public float getUserAcceptRate() { return userAcceptRate; }
    public void setUserAcceptRate(float userAcceptRate) { this.userAcceptRate = userAcceptRate; }

    public float getProcessRate() { return processRate; }
    public void setProcessRate(float processRate) { this.processRate = processRate; }

    public float getStability() { return stability; }
    public void setStability(float stability) { this.stability = stability; }

    public float getSimulateErrorRate() { return simulateErrorRate; }
    public void setSimulateErrorRate(float simulateErrorRate) { this.simulateErrorRate = simulateErrorRate; }

    public float getSize() { return size; }
    public void setSize(float size) { this.size = size; }

    public Node[] getConnections() { return connections; }

    public int getUserCapacity() { return userCapacity; }
    public void setUserCapacity(int userCapacity) { this.userCapacity = userCapacity; }

    public Texture getTexture() { return texture; }
    public void setTexture(Texture texture) { this.texture = texture; }

    public boolean isBroken() { return broken; }
    public void setBroken(boolean broken) { this.broken = broken; }
}