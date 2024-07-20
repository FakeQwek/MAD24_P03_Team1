package sg.edu.np.mad.inkwell;

public class User {
    private String email;
    private String fingerprintToken;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, String fingerprintToken) {
        this.email = email;
        this.fingerprintToken = fingerprintToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFingerprintToken() {
        return fingerprintToken;
    }

    public void setFingerprintToken(String fingerprintToken) {
        this.fingerprintToken = fingerprintToken;
    }
}
