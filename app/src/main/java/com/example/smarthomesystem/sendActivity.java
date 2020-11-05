package com.example.smarthomesystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.List;

/**
 * Interfata ce apare atunci cand utilizatorul doreste sa selecteze daca actiunea sa va deschide un tab nou sau va inchide chrome-ul
 *
 * @author Manel
 * @since 06/02/2020-16:24
 */
public class sendActivity extends AppCompatActivity {

    //<editor-fold desc="Variabile" defaultstate="collapsed">
    Button newTab;
    Button killChrome;
    pcControl control;
    List<String> date;
    //</editor-fold>

    void openNewTab(final String cautare) throws IOException, NullPointerException {
        //<editor-fold desc="body" defaultstate="collapsed">
        control.pcOutputStream.write((App.COMMAND_OPEN_LINK_NEW_TAB + " " + cautare + "\n").getBytes());
        Toast.makeText(sendActivity.this, "Link trimis", Toast.LENGTH_SHORT).show();
        finish();
        //</editor-fold>
    }

    void killChrome(final String cautare) throws IOException, NullPointerException {
        //<editor-fold desc="body" defaultstate="collapsed">
        control.pcOutputStream.write((App.COMMAND_OPEN_LINK + " " + cautare + "\n").getBytes());
        Toast.makeText(sendActivity.this, "Link trimis", Toast.LENGTH_SHORT).show();
        finish();
        //</editor-fold>
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //<editor-fold desc="body" defaultstate="collapsed">
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        Intent intent = getIntent();
        final String cautare = intent.getStringExtra(Intent.EXTRA_TEXT);
        control = MainActivity.getPC();

        try {
            date = control.loadPC();
            String x = "";
            for (String dd : date) {
                x += dd + " ";
            }
            Log.println(Log.ASSERT, "date share", "Am incarcat datele " + x);
            if (!Boolean.valueOf(date.get(2))) {
                boolean killCh = Boolean.valueOf(date.get(4));
                boolean newTab = Boolean.valueOf(date.get(3));
                if (killCh == newTab) {
                    killCh = true;
                    newTab = false;
                }
                Log.println(Log.ASSERT, "share app", "Nu ar trebui sa mai apara frame");
                if (killCh) {
                    try {
                        killChrome(cautare);
                    } catch (IOException | NullPointerException e) {
                        Toast.makeText(control.main, "Reconectare..", Toast.LENGTH_SHORT).show();
                        try {
                            control.connect();

                            try {
                                killChrome(cautare);
                            } catch (Exception ex) {
                                Toast.makeText(control.main, "Nu am putut trimite comanda " + ex.toString(), Toast.LENGTH_SHORT)
                                        .show();
                            }

                        } catch (IOException ex) {
                            Toast.makeText(control.main, "Reconectare esuata " + ex.toString(), Toast.LENGTH_SHORT)
                                    .show();
                        } catch (ClassNotFoundException ex) {
                            Toast.makeText(control.main, "Reconectare esuata " + ex.toString(), Toast.LENGTH_SHORT)
                                    .show();
                        }

                    }
                } else {
                    try {
                        openNewTab(cautare);
                    } catch (IOException | NullPointerException e) {
                        Toast.makeText(control.main, "Reconectare..", Toast.LENGTH_SHORT).show();
                        try {
                            control.connect();

                            try {
                                openNewTab(cautare);
                            } catch (Exception ex) {
                                Toast.makeText(control.main, "Nu am putut trimite comanda " + ex.toString(), Toast.LENGTH_SHORT)
                                        .show();
                            }

                        } catch (IOException ex) {
                            Toast.makeText(control.main, "Reconectare esuata " + ex.toString(), Toast.LENGTH_SHORT)
                                    .show();
                        } catch (ClassNotFoundException ex) {
                            Toast.makeText(control.main, "Reconectare esuata " + ex.toString(), Toast.LENGTH_SHORT)
                                    .show();
                        }

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        newTab = findViewById(R.id.new_tab);
        killChrome = findViewById(R.id.kill_chrome);

        newTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    openNewTab(cautare);
                } catch (IOException | NullPointerException e) {
                    Toast.makeText(control.main, "Reconectare..", Toast.LENGTH_SHORT).show();
                    try {
                        control.connect();

                        try {
                            openNewTab(cautare);
                        } catch (Exception ex) {
                            Toast.makeText(control.main, "Nu am putut trimite comanda " + ex.toString(), Toast.LENGTH_SHORT)
                                    .show();
                        }

                    } catch (IOException ex) {
                        Toast.makeText(control.main, "Reconectare esuata " + ex.toString(), Toast.LENGTH_SHORT)
                                .show();
                    } catch (ClassNotFoundException ex) {
                        Toast.makeText(control.main, "Reconectare esuata " + ex.toString(), Toast.LENGTH_SHORT)
                                .show();
                    }

                }
            }
        });

        killChrome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    killChrome(cautare);
                } catch (IOException | NullPointerException e) {
                    Toast.makeText(control.main, "Reconectare..", Toast.LENGTH_SHORT).show();
                    try {
                        control.connect();

                        try {
                            killChrome(cautare);
                        } catch (Exception ex) {
                            Toast.makeText(control.main, "Nu am putut trimite comanda " + ex.toString(), Toast.LENGTH_SHORT)
                                    .show();
                        }

                    } catch (IOException ex) {
                        Toast.makeText(control.main, "Reconectare esuata " + ex.toString(), Toast.LENGTH_SHORT)
                                .show();
                    } catch (ClassNotFoundException ex) {
                        Toast.makeText(control.main, "Reconectare esuata " + ex.toString(), Toast.LENGTH_SHORT)
                                .show();
                    }

                }
            }
        });
        //</editor-fold>
    }
}
