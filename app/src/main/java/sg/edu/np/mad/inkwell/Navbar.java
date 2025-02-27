//This class holds functionality for redirecting to activities based on button clicked

package sg.edu.np.mad.inkwell;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class Navbar {
    private Activity CurrentActivity;
    private String currentActivityName;
    public Navbar(Activity ReceivedActivity){

        this.CurrentActivity = ReceivedActivity;
        this.currentActivityName = ReceivedActivity.getClass().getSimpleName();
    }
    public Intent redirect(int id, boolean isMain) {
        if (id == R.id.nav_home && isMain){
            Toast toast = Toast.makeText(CurrentActivity.getBaseContext(), "Already in Homepage", Toast.LENGTH_SHORT);
            toast.show();
        }
        return null;
    }

    public Intent redirect(int id) {

        if (id == R.id.nav_notes) {
            Intent newActivity = new Intent(CurrentActivity, NotesActivity.class);
            Log.d( "Alert", "Opening notes");
            return newActivity;
        }
        else if (id == R.id.nav_todos) {
            Intent newActivity = new Intent(CurrentActivity, TodoActivity.class);
            Log.d("Alert", "Opening todo list");
            return newActivity;
        }
        else if (id == R.id.nav_mind_map) {
            Intent newActivity = new Intent(CurrentActivity, MindMapActivity.class);
            Log.d("Alert", "Opening mind map");
            return newActivity;
        }
        else if (id == R.id.nav_flashcards) {
            Intent newActivity = new Intent(CurrentActivity, FlashcardActivity.class);
            Log.d("Alert", "Opening flashcards");
            return newActivity;
        }
        else if (id == R.id.nav_calendar) {
            Intent newActivity = new Intent(CurrentActivity, calendarpage.class);
            Log.d("Alert", "Opening calendar");
            return newActivity;
        }

        else if (id == R.id.nav_home) {
            Intent newActivity = new Intent(CurrentActivity, MainActivity.class);
            Log.d("Alert", "Opening homepage");
            return newActivity;
        }
        else if (id == R.id.nav_logout) {
            Intent newActivity = new Intent(CurrentActivity, LoginActivity.class);
            Log.d("Alert", "Logging out");
            return newActivity;
        }
        else if (id == R.id.nav_timetable) {
            Intent newActivity = new Intent(CurrentActivity, TimetableActivity.class);
            Log.d("Alert", "Opening timetable");
            return newActivity;
        }
        else if (id == R.id.nav_profile) {
            Intent newActivity = new Intent(CurrentActivity, ProfileActivity.class);
            Log.d("Alert", "Opening profile");
            return newActivity;
        }
        else if (id == R.id.nav_friends) {
            Intent newActivity = new Intent(CurrentActivity, FriendsActivity.class);
            Log.d("Alert", "Opening profile");
            return newActivity;
        }
        else if (id == R.id.nav_community) {
            Intent newActivity = new Intent(CurrentActivity, CommunityActivity.class);
            Log.d("Alert", "Opening profile");
            return newActivity;
        }
        else if (id == R.id.nav_settings) {
            Intent newActivity = new Intent(CurrentActivity, SettingsActivity.class);
            Log.d("Alert", "Opening settings");
            return newActivity;
        }

        else if(id == R.id.nav_dictionary) {
            Intent newActivity = new Intent(CurrentActivity, KeywordSearchActivity.class);
            Log.d("Alert", "Opening Dictionary");
            return newActivity;
        }
        else if(id == R.id.nav_LanguageTranslator){
            Intent newActivity = new Intent(CurrentActivity, LanguageTranslator.class);
            Log.d("Alert", "Opening LanguageTranslator");
            return newActivity;
        }
        else if(id == R.id.nav_readingList){
            Intent newActivity = new Intent(CurrentActivity, ReadingListActivity.class);
            Log.d("Alert","Opening ReadingList");
            return newActivity;
        }

        else if (id == R.id.nav_drawing) {
            Intent newActivity = new Intent(CurrentActivity, IntermediateActivity.class);
            Log.d("Alert", "Opening drawing activity");
            return newActivity;
        }
        else if (id == R.id.nav_essay) {
            Intent newActivity = new Intent(CurrentActivity, EssayActivity.class);
            Log.d("Alert", "Opening essay tool");
            return newActivity;
        }
        else if (id == R.id.nav_calculator){
            Intent newActivity = new Intent(CurrentActivity, CalculatorActivity.class);
            Log.d("Alert", "Opening calculator");
            return newActivity;
        }


        else {
           Log.d("Alert", "Unknown page!");
        }

        Intent newActivity = null;
        return newActivity;
    }



}
