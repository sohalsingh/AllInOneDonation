package project.example.allinonedonation.ui.theme;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

public class Theme {

    private static final int[] DARK_COLOR_SCHEME = {
            Color.PURPLE_80,
            Color.PURPLE_GREY_80,
            Color.PINK_80
    };

    private static final int[] LIGHT_COLOR_SCHEME = {
            Color.PURPLE_40,
            Color.PURPLE_GREY_40,
            Color.PINK_40
    };

    public static void applyTheme(@NonNull Activity activity, boolean darkTheme) {
        if (darkTheme) {
            activity.setTheme(R.style.ThemeOverlay_AppCompat_Dark);
        } else {
            activity.setTheme(R.style.ThemeOverlay_AppCompat_Light);
        }

        // Additional theming logic can go here based on your requirements
    }

    public static int[] getColorScheme(boolean darkTheme) {
        return darkTheme ? DARK_COLOR_SCHEME : LIGHT_COLOR_SCHEME;
    }

    public static void setDynamicTheme(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Implement dynamic theming logic here, if applicable
            // This would involve accessing a color palette based on the device theme
        }
    }
}

