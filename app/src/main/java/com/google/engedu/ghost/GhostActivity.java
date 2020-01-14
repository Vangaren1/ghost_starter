/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.ghost;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;


public class GhostActivity extends AppCompatActivity {
    private static final String COMPUTER_TURN = "Computer's turn";
    private static final String USER_TURN = "Your turn";
    private GhostDictionary dictionary;
    private boolean userTurn = false;
    private boolean gameRunning = false;
    private Random random = new Random();
    private String wordFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghost);
        AssetManager assetManager = getAssets();

        try {
            InputStream inputStream = assetManager.open("words.txt");
            dictionary = new SimpleDictionary(inputStream);
        }
        catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }

        wordFragment="";

        onStart(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ghost, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handler for the "Reset" button.
     * Randomly determines whether the game starts with a user turn or a computer turn.
     * @param view
     * @return true
     */
    public boolean onStart(View view) {
        wordFragment="";
        userTurn = random.nextBoolean();
        dictionary.whoFirst(userTurn);
        TextView text = (TextView) findViewById(R.id.ghostText);
        text.setText("");
        TextView label = (TextView) findViewById(R.id.gameStatus);
        gameRunning=true;
        if (userTurn) {
            label.setText(USER_TURN);
        } else {
            label.setText(COMPUTER_TURN);
            computerTurn();
        }
        return true;
    }

    private void computerTurn() {
        TextView label = (TextView) findViewById(R.id.gameStatus);
        TextView ghostText = (TextView)findViewById(R.id.ghostText);
        // Do computer turn stuff then make it the user's turn again
        Log.d("compTurn", "It's the PC's turn");

        // check if the wordFragment is a word, if it is, declare victory for the PC!
        if(dictionary.isWord(wordFragment))
        {
            gameRunning=false;
            label.setText("PC WINS! You made a word");
        }
        else
        {
            //String possible = dictionary.getAnyWordStartingWith(wordFragment);
            String possible = dictionary.getGoodWordStartingWith(wordFragment);
            if(possible==null)
            {
                Log.d("possibleWord", "there is no possible word");
                label.setText("No word, PC WINS!");
                gameRunning=false;
            }
            else
            {
                // if there is a word that can be built from that fragment, place the next
                // letter onto the fragment, and let the user go next
                Log.d("possibleWord", possible);

                int len = wordFragment.length();
                wordFragment = possible.substring(0,len+1);
                ghostText.setText(wordFragment);
            }
        }

        userTurn = true;
       // label.setText(USER_TURN);
    }

    public boolean challenge(View view)
    {
        if(gameRunning) {
            Log.d("challengeFunction", "challenging word");
            TextView label = (TextView) findViewById(R.id.gameStatus);
            TextView ghostText = (TextView) findViewById(R.id.ghostText);
            // check if the word fragment is a word,
            if (dictionary.isWord(wordFragment) && wordFragment.length() > 3) {
                label.setText("It IS a word, You WIN!");
                gameRunning=false;
            } else {
                String possible = dictionary.getAnyWordStartingWith(wordFragment);
                if (possible != null) {
                    label.setText("No possible words, You WIN!");
                } else {
                    label.setText("PC Wins! Prefix for the word: " + possible);
                }
            }
        }
        return true;
    }

    /**
     * Handler for user key presses.
     * @param keyCode
     * @param event
     * @return whether the key stroke was handled.
     */
    /*
     * If the key that the user pressed is not a letter, default to returning the value of
     * super.onKeyUp(). Otherwise, add the letter to the word fragment. Also check whether
     * the current word fragment is a complete word and, if it is, update the game status
     * label to indicate so (this is not the right behavior for the game but will allow you
     * to verify that your code is working for now).
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        char[] lowerA = lowercase.toCharArray();
        TextView label = (TextView)findViewById(R.id.gameStatus);
        TextView ghostText = (TextView)findViewById(R.id.ghostText);

        char pressed = event.getMatch(lowerA);
        // if the key that the user pressed is not a letter, skip over this if statement

        //make sure the user can't play on the PC's turn
        if(userTurn && gameRunning) {
            if (lowercase.contains(Character.toString(pressed))) {
                // add the letter to the word fragment
                wordFragment += Character.toString(pressed);

                ghostText.setText(wordFragment);

//                if (!wordBuilt()) {
                    computerTurn();
//                }
            }
            Log.d("onKeyUp1", "eventGetChars: " + pressed);
            Log.d("onKeyUp2", "wordfrag: " + wordFragment);
            Log.d("onKeyUp2", "isWord: " + Boolean.toString(dictionary.isWord(wordFragment)));
        }
        if(gameRunning)
            Log.d("gameRunning", "game is running");

        return super.onKeyUp(keyCode, event);
    }

    public boolean wordBuilt()
    {
        TextView label = (TextView)findViewById(R.id.gameStatus);
        boolean exists = dictionary.isWord(wordFragment);
        if(dictionary.isWord(wordFragment))
        {
            label.setText("word valid");
        }
        else
        {
            label.setText("not a word");
        }
        return exists;
    }

    public boolean isChar(String testWord)
    {
        if(testWord==null)
            return false;
        String lowercase = "abcdefghijklmnopqrstuvwxyz";

        testWord.toLowerCase();

        return lowercase.contains(testWord);
    }
}
