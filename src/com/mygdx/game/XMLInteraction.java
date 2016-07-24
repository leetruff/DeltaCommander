package com.mygdx.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class XMLInteraction {
	
	public XMLInteraction(){
		
	}
	

	public static XMLInstructions loadGame(){
		if(Gdx.files.local("savedata/savegame.xml").exists()){
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();;
				Document doc = db.parse(Gdx.files.local("savedata/savegame.xml").read());
				
				int health = Integer.parseInt(doc.getElementsByTagName("health").item(0).getTextContent());
				int lives = Integer.parseInt(doc.getElementsByTagName("lives").item(0).getTextContent());
				int ammo = Integer.parseInt(doc.getElementsByTagName("ammo").item(0).getTextContent());
				int score = Integer.parseInt(doc.getElementsByTagName("score").item(0).getTextContent());
				int laserAmmo = Integer.parseInt(doc.getElementsByTagName("laserammo").item(0).getTextContent());
				int tripleAmmo = Integer.parseInt(doc.getElementsByTagName("tripleammo").item(0).getTextContent());
				String id = doc.getElementsByTagName("id").item(0).getTextContent();
				
				/*Cheatschutz (Score kann man immernoch cheaten)
				if(health > 100 || lives > 3){
					health = 1;
					lives = 1;
				}
				*/
				
				return new XMLInstructions(health, lives, ammo, score, laserAmmo, tripleAmmo, id);
				
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		} 
		//Nur falls kein savegame existiert
		return new XMLInstructions();
		
	}
	
	public static void saveGame(Hero hero, String levelID){
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			Document doc = db.newDocument();
			
			Element rootElement = doc.createElement("savegame");
			doc.appendChild(rootElement);
			
			/*------------------------------------------------------------------------------------*/
			
			//Daten des Hero speichern
			Element heroElement = doc.createElement("hero");
			rootElement.appendChild(heroElement);
			
			Element health = doc.createElement("health");
			health.appendChild(doc.createTextNode(Integer.toString(hero.getHealth())));
			heroElement.appendChild(health);
			
			Element lives = doc.createElement("lives");
			lives.appendChild(doc.createTextNode(Integer.toString(hero.getLives())));
			heroElement.appendChild(lives);
			
			Element ammo = doc.createElement("ammo");
			ammo.appendChild(doc.createTextNode(Integer.toString(hero.getAmmo())));
			heroElement.appendChild(ammo);
			
			Element score = doc.createElement("score");
			score.appendChild(doc.createTextNode(Integer.toString(hero.getSavedScore())));
			heroElement.appendChild(score);
			
			Element laserammo = doc.createElement("laserammo");
			laserammo.appendChild(doc.createTextNode(Integer.toString(hero.getLaserAmmo())));
			heroElement.appendChild(laserammo);
			
			Element tripleammo = doc.createElement("tripleammo");
			tripleammo.appendChild(doc.createTextNode(Integer.toString(hero.getTripleAmmo())));
			heroElement.appendChild(tripleammo);
			
			/*------------------------------------------------------------------------------------*/
			
			//Daten des Levels speichern
			Element levelElement = doc.createElement("level");
			rootElement.appendChild(levelElement);
			
			Element id = doc.createElement("id");
			id.appendChild(doc.createTextNode(levelID));
			levelElement.appendChild(id);
			
			/*------------------------------------------------------------------------------------*/
			
			//Level speichern
			FileHandle lvl = Gdx.files.local("savedata/savegame.xml");
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(lvl.file());
			transformer.transform(source, result);
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
	}
	
	public static void saveScore(String levelname, String username, int points){
		System.out.println("assert");
		FileHandle highscores = Gdx.files.local("savedata/scores.txt");
		highscores.writeString((levelname + ";" + username + ";" + points + "\r\n"), true);
	}
	
	public static String[][] loadScores(String levelname){
		FileHandle highscores = Gdx.files.local("savedata/scores.txt");
		if(!highscores.exists()){
			return null;
		}
		//Datei einlesen
		String[] scoresStrings = highscores.readString().split("\r\n");
		//Anzahl Scores des Levels zaehlen
		int count = 0;
		for(int i = 0; i < scoresStrings.length; i++){
			if(scoresStrings[i].startsWith(levelname)){
				count++;
			}
		}
		if(count == 0){
			return null;
		}
		//Scores des Levels ausgeben
		String[][] scoreTable = new String[count][2];
		int j = 0;
		for(int i = 0; i < scoresStrings.length; i++){
			if(scoresStrings[i].startsWith(levelname)){
				scoreTable[j][0] = scoresStrings[i].split(";")[1];
				scoreTable[j][1] = scoresStrings[i].split(";")[2];
				j++;
			}
		}
		Arrays.sort(scoreTable, new Comparator<String[]>() {
            @Override
            public int compare(final String[] entry1, final String[] entry2) {
                final int score1 = Integer.parseInt(entry1[1]);
                final int score2 = Integer.parseInt(entry2[1]);
                return score2-score1;
            }
        });
		return scoreTable;
	}
	
	public static void saveSettings(String id, String value){
		
		FileHandle settings = Gdx.files.local("savedata/settings.txt");
		if(!settings.exists()){
			settings.writeString("0 \r\n 1 \r\n 1 \r\nDefault", false);
		}
		String[] settingsStrings = settings.readString().split("\r\n");
		//[MusicVolume, SFXVolume, Crosshair, Username]
		
		if(id.equals("MusicVolume")){
			settings.writeString((value + "\r\n" + settingsStrings[1] + "\r\n" + settingsStrings[2] + "\r\n" + settingsStrings[3]), false);
		} else if (id.equals("SFXVolume")){
			settings.writeString((settingsStrings[0] + "\r\n" + value + "\r\n" + settingsStrings[2] +  "\r\n" + settingsStrings[3]), false);
		} else if (id.equals("Crosshair")){
			settings.writeString((settingsStrings[0] + "\r\n" + settingsStrings[1] + "\r\n" + value + "\r\n" +settingsStrings[3]), false);
		} else if (id.equals("Username")){
			settings.writeString((settingsStrings[0] + "\r\n" + settingsStrings[1] + "\r\n" + settingsStrings[2] + "\r\n" + value), false);
		}
	}
	
	public static Float loadMusicVolume(){
		FileHandle settings = Gdx.files.local("savedata/settings.txt");
		if(!settings.exists()){
			settings.writeString("0 \r\n 1 \r\n 1 \r\nDefault", false);
			return 0f;
		}
		String[] settingsStrings = settings.readString().split("\r\n");
		return Float.parseFloat(settingsStrings[0]);
	}
	public static Float loadSFXVolume(){
		FileHandle settings = Gdx.files.local("savedata/settings.txt");
		if(!settings.exists()){
			settings.writeString("0 \r\n 1 \r\n 1 \r\nDefault", false);
			return 1f;
		}
		String[] settingsStrings = settings.readString().split("\r\n");
		return Float.parseFloat(settingsStrings[1]);
	}
	public static Float loadCrosshair(){
		FileHandle settings = Gdx.files.local("savedata/settings.txt");
		if(!settings.exists()){
			settings.writeString("0 \r\n 1 \r\n 1 \r\nDefault", false);
			return 1f;
		}
		String[] settingsStrings = settings.readString().split("\r\n");
		return Float.parseFloat(settingsStrings[2]);
	}
	public static String loadName(){
		FileHandle settings = Gdx.files.local("savedata/settings.txt");
		if(!settings.exists()){
			settings.writeString("0 \r\n 1 \r\n 1 \r\nDefault", false);
			return "Default";
		}
		String[] settingsStrings = settings.readString().split("\r\n");
		return settingsStrings[3];
	}
}
