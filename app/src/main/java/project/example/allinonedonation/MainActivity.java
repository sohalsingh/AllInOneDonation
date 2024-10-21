package project.example.allinonedonation;

import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.activity.compose.setContent;
import androidx.annotation.Nullable;
import androidx.compose.material3.Scaffold;
import androidx.compose.material3.Text;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.tooling.preview.Preview;

import project.example.allinonedonation.ui.theme.AllInOneDonationTheme;

public class MainActivity extends ComponentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableEdgeToEdge();

        setContent {
            AllInOneDonationTheme {
                Scaffold(
                        modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                        Greeting(
                                name = "Android",
                                modifier = Modifier.padding(innerPadding)
                        );
                }
            }
        }
    }

    @Composable
    public void Greeting(String name, Modifier modifier) {
        Text(
                text = "Hello " + name + "!",
                modifier
        );
    }

    @Preview(showBackground = true)
    @Composable
    public void GreetingPreview() {
        AllInOneDonationTheme {
            Greeting("Android", Modifier);
        }
    }
}
