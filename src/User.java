// most of these were generated with the help of Gemini AI, but I did some manual edits to make it more functional and to add some features.
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Texture;

public class User {
    
    private String userType;
    private float rating;
    private float patience;
    private float errorCauseRate;
    private float speed;
    private String soundState;
    private Texture userPlaceholderTexture; // Placeholder for the node texture
    
    // Constructor
    public User(String userType, float rating, float patience, float errorCauseRate, float speed) {
        this.userType = userType;
        this.rating = rating;
        this.patience = patience;
        this.errorCauseRate = errorCauseRate;
        this.speed = speed;
        this.soundState = "moderate";
        this.userPlaceholderTexture = new Texture("src/assets/placeholderUser.png"); // Placeholder texture
    }

    public void setUserPlaceholderTexture(Texture userPlaceholderTexture) {
        this.userPlaceholderTexture = userPlaceholderTexture;
    }

    // Method to move a node towards a target position
    public void moveNode(Vector2 currentNode, Vector2 targetNode, float speed) {
        Vector2 direction = targetNode.cpy().sub(currentNode);
        float distance = direction.len();

        if (distance <= speed) {
            currentNode.set(targetNode);
        } else if (distance > 0f && speed > 0f) {
            currentNode.mulAdd(direction, speed / distance);
        }
    }

    // Method to update the user's rating and patience over time
    public void update(float deltaTime) {
        updateRating(deltaTime);
        updatePatience(deltaTime);
    }

    // Method to handle the loss of rating based on patience
    public void updateRating(float deltaTime) {
        rating -= deltaTime * errorCauseRate; // Decrease rating over time based on errorCauseRate
        if (rating < 0f) {
            rating = 0f; // Ensure rating doesn't go below 0
        }
    }

    // Method to handle the loss of patience over time
    public void updatePatience(float deltaTime) {
        patience -= deltaTime * 0.1f; // Decrease patience over time
        if (patience < 0f) {
            patience = 0f; // Ensure patience doesn't go below 0
        }
    }
    
    // Method to handle the loss of rating based on patience
    public void loseRating(float rating, float patience) {
        float patienceDepletion = Math.max(0f, 1f - patience);
        this.rating = Math.max(0f, rating - patienceDepletion);
    }

    // Methods to change the sound state of the user   
    public void soundDissatisfied() {
        this.soundState = "dissatisfied";
    }

    // Methods to change the sound state of the user
    public void soundModerate() {
        this.soundState = "moderate";
    }

    // Methods to change the sound state of the user
    public void soundContent() {
        this.soundState = "content";
    }

    // Method to broadcast a loss to another user   
    public void broadcastLoss(User user, float rating) {
        if (user == null) {
            return;
        }

        user.loseRating(rating, user.getPatience());
        user.soundDissatisfied();
        System.out.println(this.userType + " broadcast loss to " + user.getUserType() + ": rating=" + user.getRating());
    }

    // Method to broadcast content to another user
    public void broadcastContent(User user, float rating) {
        if (user == null) {
            return;
        }

        user.setRating(Math.max(0f, rating));
        user.soundContent();
        System.out.println(this.userType + " broadcast content to " + user.getUserType() + ": rating=" + user.getRating());
    }

    // Method to broadcast content to all users in a list
    public void broadcastContentToAll(User[] users, float rating) {
        for (User user : users) {
            if (user != null) {
                user.setRating(Math.max(0f, rating));
                user.soundContent();
                System.out.println(this.userType + " broadcast content to " + user.getUserType() + ": rating=" + user.getRating());
            }
        }
    }

    // Method to broadcast content to self
    public void broadcastContent(float rating) {
        this.rating = Math.max(0f, rating);
        this.soundContent();
        System.out.println(this.userType + " broadcast content: rating=" + this.rating);
    }

    // --- Getters and Setters ---
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public float getPatience() { return patience; }
    public void setPatience(float patience) { this.patience = patience; }

    public float getErrorCauseRate() { return errorCauseRate; }
    public void setErrorCauseRate(float errorCauseRate) { this.errorCauseRate = errorCauseRate; }

    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }

    public String getSoundState() { return soundState; }
    public void setSoundState(String soundState) { this.soundState = soundState; }
}