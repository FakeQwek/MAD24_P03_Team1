package sg.edu.np.mad.inkwell;

public class Message {
    public int id;

    public String message;

    public String type;

    public int getId() { return this.id; }

    public String getMessage() { return this.message; }

    public String getType() { return this.type; }

    public Message(int id, String message, String type) {
        this.id = id;
        this.message = message;
        this.type = type;
    }
}
