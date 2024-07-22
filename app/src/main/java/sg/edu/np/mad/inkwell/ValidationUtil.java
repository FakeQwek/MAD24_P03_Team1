package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;

public class ValidationUtil {
    public static boolean isRequired(EditText editText) {
        return !editText.getText().toString().trim().isEmpty();
    }

    public static boolean isEmailValid(EditText editText) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return editText.getText().toString().trim().matches(emailPattern);
    }
}

