package project.example.allinonedonation.ui.theme;

import android.content.Context;
import androidx.core.content.res.ResourcesCompat;
import android.graphics.Typeface;
import android.widget.TextView;

public class Type {

    // Define default text styles
    public static void setBodyLarge(TextView textView) {
        textView.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        textView.setTextSize(16); // Set text size in SP
        textView.setLineSpacing(0, 1.5f); // Set line height
        // Set letter spacing if necessary, requires API level 21+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            textView.setLetterSpacing(0.5f);
        }
    }

    // Additional typography styles can be defined similarly
    public static void setTitleLarge(TextView textView) {
        textView.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        textView.setTextSize(22); // Set text size in SP
        textView.setLineSpacing(0, 1.27f); // Set line height
    }

    public static void setLabelSmall(TextView textView) {
        textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textView.setTextSize(11); // Set text size in SP
        textView.setLineSpacing(0, 1.45f); // Set line height
    }
}
