/*
 * TwitFeedBot is licenced under the The MIT License (MIT)
 * 
 * Copyright (c) 2017 Brett daloonik@gmail.com
 * 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package twitfeedbot;

import java.io.IOException;
import java.io.FileNotFoundException;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.Trends;
import twitter4j.User;

import java.util.Random;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jeremybrooks.knicker.KnickerException;
import net.jeremybrooks.knicker.WordApi;
import net.jeremybrooks.knicker.WordsApi;


@SuppressWarnings("serial")

public class BotherSomeoneServlet extends HttpServlet
 {
 //Replace these values with your own
 public static final String feedsFile = "WEB-INF/StaticFiles/feeds";
 public static final String CONSUMER_KEY = "your consumer key";
 public static final String CONSUMER_SECRET = "your consumer secret";
 public static final String ACCESS_TOKEN = "your access code";
 public static final String ACCESS_SECRET = "your access secret";
 public static final String WORDNIK_KEY = "your wordnik key";
 @Override
 public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException 
   {
   Twitter twit;
   StringBuilder builder = new StringBuilder();
   User victim = null;  
   long cursor = -1;
   long[] friendIDs, victimIDs;
   Random r = new Random();   

   resp.setContentType("text/html");
   resp.getWriter().println("<html>");
   resp.getWriter().println("<body>");
   try 
     {
     ConfigurationBuilder twitterConfigBuilder = new ConfigurationBuilder();		
     twitterConfigBuilder.setDebugEnabled(false);

     System.setProperty("WORDNIK_API_KEY", WORDNIK_KEY);
     twitterConfigBuilder.setOAuthConsumerKey(CONSUMER_KEY);
     twitterConfigBuilder.setOAuthConsumerSecret(CONSUMER_SECRET);
     twitterConfigBuilder.setOAuthAccessToken(ACCESS_TOKEN);
     twitterConfigBuilder.setOAuthAccessTokenSecret(ACCESS_SECRET);

     twit = new TwitterFactory(twitterConfigBuilder.build()).getInstance();

     Trends t = twit.getPlaceTrends(1);  //global trends
     friendIDs = twit.getFollowersIDs(twit.getId(), cursor).getIDs();
     if(friendIDs.length == 0)
        resp.getWriter().println("Cannot find any followers to bother");
     else
       {  
       victimIDs = twit.getFollowersIDs(friendIDs[r.nextInt(friendIDs.length)], cursor).getIDs();
       if(victimIDs.length == 0)
         resp.getWriter().println("Cannot find any followers to bother");
       else
         {
         victim = twit.showUser(victimIDs[r.nextInt(victimIDs.length)]);
         builder.append("@" + victim.getScreenName())
                .append(" ");
         //Append Wordnik example sentence
         builder.append(WordApi.topExample(WordsApi.randomWord().getWord()).getText());
         if(builder.length() > 140) //Tweets are maximum 140 characters
            {
            if(builder.lastIndexOf(";",110) > 0)
               builder.setLength(builder.lastIndexOf(";",110)); 
            else if(builder.lastIndexOf(":",110) > 0)
               builder.setLength(builder.lastIndexOf(":", 110));  
            else if(builder.lastIndexOf(",",110) > 0)
               builder.setLength(builder.lastIndexOf(",", 110));
            else builder.setLength(110);
            }
         builder.append(" " + t.getTrends()[r.nextInt(t.getTrends().length)].getName());
         
         if(builder.length() > 140)
            builder.setLength(140); //Tweets are limited to 140 characters

         twit.updateStatus(builder.toString());
         resp.getWriter().println("Tweet posted: "+builder.toString() +"<br>"); 
         }
       } 
     }
   catch(FileNotFoundException e)
     {
     e.printStackTrace(System.err);
     resp.getWriter().println("Input file(s) not found<br>");
     resp.getWriter().println("<pre>");
     e.printStackTrace(resp.getWriter());
     resp.getWriter().println("</pre>");
     }
   catch(TwitterException e)
     {
     resp.getWriter().println("Problem with Twitter <br>");
     resp.getWriter().println("<pre>");
     e.printStackTrace(resp.getWriter());
     resp.getWriter().println("</pre>");
     }
   catch(KnickerException e)
      {
      e.printStackTrace(System.err);
      resp.getWriter().println("Problem with Wordnik <br>");
      resp.getWriter().println("<pre>");
      e.printStackTrace(resp.getWriter());
      resp.getWriter().println("</pre>");
      }
   catch(Exception e)
     {
     e.printStackTrace(System.err);
     resp.getWriter().println("<pre>");
     e.printStackTrace(resp.getWriter());
     resp.getWriter().println("</pre>");
     }
   }
 }

