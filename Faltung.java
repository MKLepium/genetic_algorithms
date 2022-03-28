package src;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.ImageIO;

public class Faltung implements Runnable{

  private final String m_SEQ;

  public String getM_Faltung() {
    return m_Faltung;
  }

  public Faltung clone(){
    return new Faltung(this.m_SEQ, this.m_Faltung, this.m_Fitness);
  }

  public void setM_Faltung(String m_Faltung) {
    this.m_Faltung = m_Faltung;
  }
  private Random m_rand = new Random(System.currentTimeMillis());
  private String m_Faltung;
  private final Integer m_length;
  private Float m_Fitness;
  private Integer m_Ueberlappungen;
  private Integer[][][] m_array;
  private int m_x;
  private int m_y;

  public Integer getM_Ueberlappungen() {
    return m_Ueberlappungen;
  }

  public Integer getM_Hydrophobkontakte() {
    return m_Hydrophobkontakte;
  }

  private Integer m_Hydrophobkontakte;

  public Float getM_Fitness() {
    return m_Fitness;
  }
  public Faltung(String SEQ){
    m_SEQ = SEQ;
    m_length = SEQ.length();
    relative_Konformationskoordinaten();
    SetupFitness();
  }
  public Faltung(String SEQ, String Faltung, float Fitness){
    m_SEQ = SEQ;
    m_length = SEQ.length();
    m_Faltung = Faltung;
    m_Fitness = Fitness;
  }

  public void Mutation(){


    for(int i = 0; i < m_Faltung.length() - 1; i++) {
      //System.out.println("Mutation Occured");
      char[] chars = m_Faltung.toCharArray();
      int new_char = m_rand.nextInt(3);
      switch (new_char) {
        case 0 -> chars[i] = 'S';
        case 1 -> chars[i] = 'L';
        case 2 -> chars[i] = 'R';
      }
      m_Faltung = String.valueOf(chars);
      //System.out.println("Mutation occured");
    }
  }



  public void relative_Konformationskoordinaten(){
    StringBuilder Faltung = new StringBuilder();
    for (int i = 0; i < m_length; i++) {
      int randomNum = m_rand.nextInt(3);
      switch (randomNum) {
        case 0 -> Faltung.append("S"); // Geradeaus Straight
        case 1 -> Faltung.append("L"); // Links / Left
        case 2 -> Faltung.append("R"); // Rechts / Right
        default -> System.out.println("Something went wrong with the random numbers");
      }

    }
    m_Faltung = Faltung.toString();
  }
  public void SetupFitness(){
    // 0 Hydrophil/Lipophob ()
    // 1 Hydrophob/Lipophil (Schwarz)
    //
    // Turus Koordinaten anschauen
    // Worst Case bei Konformationskoordinaten wäre 2*n+1 in der Größe des Arrays und n / 4 in der Tiefe
    Integer[][][] Array = new Integer[2 * m_length + 1][2 * m_length + 1][(m_length/4)+1];
    m_Fitness = GetFitness(Array);

    //System.out.println(m_Fitness);


  }
  private Float GetFitness(Integer[][][] array) {
    int x = m_length;
    int y = m_length;
    int Ueberlappungen = 0;

    String Direction = "";
    boolean isHydrophob_new = false;
    boolean isHydrophob_old;
    Integer hydrophobKontakte = 0;

    //array[x][y][0] = Character.getNumericValue(m_SEQ.charAt(0));


    for (int t = 0; t < m_length; t++){
      boolean ueberlappung = false;
      char m = m_Faltung.charAt(t);
      int depth = 0;
      Direction = GetDirection(Direction, m);
      isHydrophob_old = isHydrophob_new;

      isHydrophob_new = m_SEQ.charAt(t) == '1';
      // change x and y according to direction
      switch (Direction) {
        case "UP" -> y += 1;
        case "DOWN" -> y -= 1;
        case "LEFT" -> x -= 1;
        case "RIGHT" -> x += 1;
        default -> System.out.println("Something went wrong in X/Y Update in Direction");
      }
      if(isHydrophob_new){
        // Check all Directions
        hydrophobKontakte += GetAnzahlHydrophober(array[x][y+1]); // Oben
        hydrophobKontakte += GetAnzahlHydrophober(array[x-1][y]); // Links
        hydrophobKontakte += GetAnzahlHydrophober(array[x+1][y]); // Rechts
        hydrophobKontakte += GetAnzahlHydrophober(array[x][y-1]); // Unten
        // If current and last one are Hydrophob then subtract one
        if(isHydrophob_old){
          hydrophobKontakte -= 1;
        }
      }



      // Update Value in Array
      while(array[x][y][depth] != null){
        depth += 1;
        ueberlappung = true;
        m_x = x;
        m_y = y;
      }
      if(ueberlappung){
        Ueberlappungen += 1;
      }

      array[x][y][depth] = Character.getNumericValue(m_SEQ.charAt(t));
    }
    m_Ueberlappungen = Ueberlappungen;
    m_Hydrophobkontakte = hydrophobKontakte;
    Ueberlappungen += 1; // Grundsätzlich eins drauf, damit ich teilen kann
    m_array = array;
    return (float)hydrophobKontakte / (float)Ueberlappungen;
  }
  private Integer GetAnzahlHydrophober(Integer[] m){
    int value = 0;
    for (Integer a : m) {
      // Only 0 and 1
      if(a != null){
        if(a == 1){
          value += 1;
        }
      }
    }
    return  value;
  }
  private String GetDirection(String direction, char m) {
    switch (m) {
      case 'S' -> {
        if (direction.equals("")) {
          direction = "UP"; // Standart Richtung ist einfach nach Oben
        } // else, stays the same
      }
      case 'L' -> {
        switch (direction) {
          case "UP", "" -> direction = "LEFT";
          case "LEFT" -> direction = "DOWN";
          case "DOWN" -> direction = "RIGHT";
          case "RIGHT" -> direction = "UP";
        }
      }
      case 'R' -> {
        switch (direction) {
          case "UP", "" -> direction = "RIGHT";
          case "LEFT" -> direction = "UP";
          case "DOWN" -> direction = "LEFT";
          case "RIGHT" -> direction = "DOWN";
        }
      }
    }
    return direction;
  }
  public void DrawPic(int count){

    int width = 820;
    int height = 820;
    BufferedImage image = new BufferedImage(width, height, TYPE_INT_RGB);
    Graphics2D g2 = image.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    g2.setColor(Color.blue);
    g2.fillRect(0, 0, width, height);

    g2.setColor(Color.BLACK);
    g2.drawString("Überlappungen: "+m_Ueberlappungen.toString(), 20, 20);
    g2.drawString("Hydrophobkontakte: "+m_Hydrophobkontakte.toString(), 20, 40);
    g2.drawString("Fitness: "+m_Fitness.toString(), 20, 60);
    int x = 0;
    int y = 0;
    String Direction = "";
    int rectSize = 20;
    int center_x = width / 2;
    int center_y = height / 2;
    for (int t = 0; t < m_length; t++) {
      char m = m_Faltung.charAt(t);
      Direction = GetDirection(Direction, m);
      g2.setColor(Color.BLACK);
      switch (Direction) {
        case "UP" -> y += 1;
        case "DOWN" -> y -= 1;
        case "LEFT" -> x -= 1;
        case "RIGHT" -> x += 1;
        default -> System.out.println("Something went wrong in X/Y Update in Direction");
      }

      if (m_SEQ.charAt(t) == '1') {
        g2.setColor(Color.BLACK);
      } else {
        g2.setColor(Color.white);
      }
      g2.fillRect(center_x + x * rectSize, center_y + y * rectSize, (rectSize / 2) + 1, (rectSize / 2)+1);
      g2.setColor(Color.MAGENTA);
      g2.drawString(Integer.toString(t), center_x + x * rectSize, center_y + y * rectSize);


    }
    x = 0;
    y = 0;
    Direction = "";
    rectSize = 20;
    for (int t = 0; t < m_length; t++) {
      char m = m_Faltung.charAt(t);
      Direction = GetDirection(Direction, m);
      g2.setColor(Color.red);
      switch (Direction) {
        case "UP" -> {
          if (t != 0) {
            g2.drawLine(
                center_x + x * rectSize + 5,
                center_y + y * rectSize + 5,
                center_x + x * rectSize + 5,
                center_y + y * rectSize + rectSize + 5
            );
          }
          y += 1;
        }
        case "DOWN" -> {
          if (t != 0) {
            g2.drawLine(
                center_x + x * rectSize + 5,
                center_y + y * rectSize + 5,
                center_x + x * rectSize + 5,
                center_y + y * rectSize - rectSize + 5
            );
          }
          y -= 1;
        }
        case "LEFT" -> {
          if (t != 0) {
            g2.drawLine(
                center_x + x * rectSize + 5,
                center_y + y * rectSize + 5,
                center_x + x * rectSize - rectSize + 5,
                center_y + y * rectSize + 5
            );
          }
          x -= 1;
        }
        case "RIGHT" -> {
          if (t != 0) {
            g2.drawLine(
                center_x + x * rectSize + 5,
                center_y + y * rectSize + 5,
                center_x + x * rectSize + rectSize + 5,
                center_y + y * rectSize + 5
            );
          }
          x += 1;
        }
        default -> System.out.println("Something went wrong in X/Y Update in Direction");
      }
    }


    String folder = ".";
    String filename = "Bild"+ count +".png";
    if(!new File(folder).exists()){
      new File(folder).mkdirs();
    }
    try{
      ImageIO.write(image, "png", new File(folder + File.separator + filename));
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * When an object implementing interface {@code Runnable} is used to create a thread, starting the
   * thread causes the object's {@code run} method to be called in that separately executing
   * thread.
   * <p>
   * The general contract of the method {@code run} is that it may take any action whatsoever.
   *
   * @see Thread#run()
   */
  @Override
  public void run() {
    SetupFitness();
  }
}
