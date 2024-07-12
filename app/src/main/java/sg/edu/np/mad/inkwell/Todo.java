package sg.edu.np.mad.inkwell;

import com.google.type.DateTime;

public class Todo {
    // Class attribues
    public String todoTitle;

    public int todoId;

    public String description;

    public String todoDateTime;

    public String todoStatus;

    public String dateTime;

    // Class get methods
    public String getTodoTitle() { return this.todoTitle; }

    public int getTodoId() { return this.todoId; }

    public String getDescription() { return this.description; }

    public String getTodoDateTime() { return this.todoDateTime; }

    public String getTodoStatus() { return this.todoStatus; }

    public String getDateTime() { return this.dateTime; }

    // Class set methods
    public void setTodoTitle(String todoTitle) {
        this.todoTitle = todoTitle;
    }

    public void setTodoId(int todoId) {
        this.todoId = todoId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTodoDateTime(String todoDateTime) {
        this.todoDateTime = todoDateTime;
    }

    public void setTodoStatus(String todoStatus) {
        this.todoStatus = todoStatus;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    // Class constructor
    public Todo(String todoTitle, int todoId, String description, String todoDateTime, String todoStatus, String dateTime) {
        this.todoTitle = todoTitle;
        this.todoId = todoId;
        this.description = description;
        this.todoDateTime = todoDateTime;
        this.todoStatus = todoStatus;
        this.dateTime = dateTime;
    }
}
