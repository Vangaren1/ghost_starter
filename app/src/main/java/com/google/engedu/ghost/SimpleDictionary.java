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

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

public class SimpleDictionary implements GhostDictionary {
    Random rand;
    private ArrayList<String> words;

    // stores who went first
    private boolean UserFirst;

    public SimpleDictionary(InputStream wordListStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(wordListStream));
        rand = new Random();
        words = new ArrayList<>();
        UserFirst=true;
        String line = null;
        while((line = in.readLine()) != null) {
            String word = line.trim();
            if (word.length() >= MIN_WORD_LENGTH)
              words.add(line.trim());
        }
    }

    public void whoFirst(boolean choice)
    {
        this.UserFirst = choice;
    }

    @Override
    public boolean isWord(String word) {
        return words.contains(word);
    }

    @Override
    public String getAnyWordStartingWith(String prefix) {
        // word must be at least 1 character longer than the prefix

        int plen = prefix.length();
        int wlen = words.size();
        // if prefix is empty, return a random word from the dictionary
        if(plen == 0)
        {
            return words.get(rand.nextInt(wlen));
        }
        // if prefix is not empty, then find a random word starting with that prefix
        // first: add all words containing that prefix to an arraylist, then randomly select one
        String tmpString;
        ArrayList<String> prewords = new ArrayList<>();
        for(int i =0; i< wlen; i++)
        {
            tmpString = words.get(i);
            if(tmpString.length()>plen)
            {
                if(prefix.compareTo(tmpString.substring(0,plen))==0)
                    prewords.add(tmpString);
            }
        }
        // if prewords is empty, there are no words that contain the prefix, then return null
        // otherwise return a random word from that list.
        if(prewords.size()==0)
            return null;
        else
        {
            return prewords.get(rand.nextInt(prewords.size()));
        }
    }

    @Override
    public String getGoodWordStartingWith(String prefix) {
        String selected = null;

        int endIndex=-1;
        // find a word that starts with the prefix
        int frontIndex = findStartIndex(prefix, 0, words.size());



        if(frontIndex==-1)
        {
            // if frontIndex is -1, then the prefix wasn't found, then return null
            return selected;
        }
        // if it is not -1, then the front index exists in the dictionary

        // find the earliest and last instance of the prefix in the dictionary
        frontIndex = slideLeft(prefix, frontIndex);
        endIndex = slideRight(prefix, frontIndex);

        int randomIndex;

        // if there is only one index, just return that word
        if(frontIndex==endIndex)
            return words.get(frontIndex);

        // TODO: sort the words into two columns, even length words and odd length words
        // Randomly select the words from the column that would help the PC the most, depending
        // on who went first.  If PC went first, an even len word, otherwise odd

        // build the even and odd arrays
        ArrayList<String> even = new ArrayList<>();
        ArrayList<String> odd = new ArrayList<>();
        for(int sortI=frontIndex; sortI <= endIndex; sortI++)
        {
            String unsortedWord = words.get(sortI);
            int wLen = unsortedWord.length();
            if(wLen % 2 == 0)
                even.add(unsortedWord);
            else
                odd.add(unsortedWord);
        }

        //pick a random index from either of them
        if(UserFirst)
        {
            randomIndex = rand.nextInt(odd.size());
            selected = odd.get(randomIndex);
        }
        else{
            randomIndex = rand.nextInt(even.size());
            selected = even.get(randomIndex);
        }

//        int randomWordOffset = rand.nextInt(endIndex-frontIndex);
//        randomIndex = randomWordOffset + frontIndex;

        // return the random word selected from the index range
//        selected = words.get(randomIndex);


        Log.d("getGoodWordStartingWith", Integer.toString(frontIndex) + " selected:"+selected);

        return selected;
    }

    private int slideRight(String prefix, int index)
    {

        int wSize = words.size();
        // if we're at the end of the dictionary, or are given an empty prefix string return the last entry
        if(index==(wSize-1))
            return wSize-1;
        if("".compareTo(prefix)==0)
            return wSize-1;
        // if the word no longer contains the prefix or we reach the the end of the dictionary
        // then return the last index we looked at
        int tmpIndex = index;
//        String wordAtIndex = words.get(tmpIndex);
        while(tmpIndex < wSize && (words.get(tmpIndex)).contains(prefix))
            tmpIndex++;

        Log.d("endOftheIndex",Integer.toString(tmpIndex));
        // once we exit the while loop, at the end of the of the list that contains the prefix
        return tmpIndex;

    }

    private int slideLeft(String prefix, int index)
    {
        // if the index is zero, return that, because there's nothing to the left of it
        if(index==0)
            return 0;

        // if the word at the index is shorter than the prefix, it will always be to the
        // alphabetically before the predix, so just return the original index.
        String tmp = words.get(index);
        int tmpLen = tmp.length();
        int preLen = prefix.length();
        if(tmpLen < preLen)
        {
            return index;
        }


        int tmpIndex = index;
        while(tmpIndex > 0 && tmp.compareTo(prefix) > 0 )
        {
            tmpIndex--;
            tmp=words.get(tmpIndex);
        }
        // it would return one before the start, so instead return the next, as long as it can
        Log.d("slideLeftFunc", Integer.toString(tmpIndex+1));
        if(tmpIndex==0)
            return 0;
        else if(tmpIndex!=(words.size()-1))
            return tmpIndex+1;
        return tmpIndex;
    }

    private int findStartIndex(String prefix, int start, int end)
    {

        int middleIndex = (start+end)/2;
        int preLen = prefix.length();
        String middleWord = words.get(middleIndex);

        // if the prefix is empty, then return the middleIndex
        if(preLen==0)
            return start;

        int middleLen = middleWord.length();

        // if the word at the middle index is smaller than the prefix itself,
        // just do the compare on the entire word, otherwise do it on a substring
        if(middleLen > preLen)
        {
            middleWord = middleWord.substring(0,preLen);
        }

        int cmpResult = prefix.compareTo(middleWord);

        if((end-start)==1 && cmpResult!=0)
            return -1;

        Log.d("FindStartIndex", "prefix:"+prefix);
        Log.d("FindStartIndex"," start:"+Integer.toString(start)+" "+Integer.toString(end));
        Log.d("FindStartIndex", middleWord);
        // if you get down to a single cell, check and return the starting cell if its the prefix
        // otherwise return -1 because we didn't find one
        // use compareTo get the numerical result of the prefix compare
        // if -1 or less, it's earlier alphabetically
        // if 1 or great, it's later alphabetically
        // if 0, then it's the exact match to the prefix

        if(cmpResult==0)
        {
            return middleIndex;
        }
        else if(cmpResult > 0)
        {
            return findStartIndex(prefix, middleIndex, end);
        }
        else
        {
            return findStartIndex(prefix, start, middleIndex);
        }
    }
}
