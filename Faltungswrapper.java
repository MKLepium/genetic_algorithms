package src;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Faltungswrapper implements Runnable {
  public final Integer m_ChildrenPerGeneration;
  public final Integer m_Generations;
  public final String m_SEQ;
  public static Boolean m_Break = false;
  public final Float m_MutationChanceMax;
  public final Float m_MutationChanceMin;
  public static AtomicInteger m_Output = new AtomicInteger();
  public final Float m_CrossoverChance;
  public final boolean m_Turnier;


  public Faltungswrapper(String SEQ, Integer ChildsPerGeneration, Integer Generations, Float MutationMax, Float MutationMin, Float CrossOverChance, boolean TurnierSelektion) {
    m_SEQ = SEQ;
    m_ChildrenPerGeneration = ChildsPerGeneration;
    m_Generations = Generations;
    m_MutationChanceMax = MutationMax;
    m_MutationChanceMin = MutationMin;
    m_CrossoverChance = CrossOverChance;
    m_Turnier = TurnierSelektion;
  }

  public ArrayList<Faltung> FitnesProportionaleSelektion(ArrayList<Faltung> Alte_Generation){
    ArrayList<Faltung> Neue_Generation = new ArrayList<>();
    ArrayList<Float> Verteilung = new ArrayList<>();
    Float TotalFitness = 0.0f;

    for (Faltung f : Alte_Generation) {
      TotalFitness += f.getM_Fitness();
    }
    for (Faltung f : Alte_Generation){
      Verteilung.add( f.getM_Fitness() / TotalFitness );
    }
    ArrayList<Float> Kumulative_Verteilung = new ArrayList<>();
    // Cumulative Verteilung
    Float Kumulativ = 0.0f;
    for (Float f: Verteilung) {
      Kumulativ += f;
      Kumulative_Verteilung.add(Kumulativ);
    }
    Random rand = new Random(System.currentTimeMillis());
    // Idee: https://stackoverflow.com/a/1892335
    while(Neue_Generation.size() != Alte_Generation.size() ){
      float random = rand.nextFloat();
      for (int i = 0; i < Alte_Generation.size(); i++) {
        if( random <= Kumulative_Verteilung.get(i)){
          Neue_Generation.add(Alte_Generation.get(i).clone());
          break;
        }
      }
    }
    return Neue_Generation;
  }


  public ArrayList<Faltung> TurnierSelektion(ArrayList<Faltung> Alte_Generation){
    Random rand = new Random(System.currentTimeMillis());
    ArrayList<Faltung> Neue_Generation = new ArrayList<>();
    int Competitors = 3;
    while(Neue_Generation.size() != Alte_Generation.size()){
      ArrayList<Faltung> Turnier_Teilnehmer = new ArrayList<>();
      // Auswahl der Turnier Teilnehmer
      for(int i = 0; i <Competitors; i++){
        int random = rand.nextInt(Alte_Generation.size());
        Faltung F = Alte_Generation.get(random).clone();
        Turnier_Teilnehmer.add(F);
      }
      Faltung Turnier_Gewinner = null;

      //Turnier "Veranstaltung
      for (Faltung F : Turnier_Teilnehmer) {
        if(Turnier_Gewinner == null){
          Turnier_Gewinner = F; // Eventuell hier auch Clonen
        }
        else if(F.getM_Fitness() > Turnier_Gewinner.getM_Fitness()){
            Turnier_Gewinner = F; // Eventuell hier auch Clonen
        }
      }
      // David gegen Goliath
      if(rand.nextFloat() <= 0.05){
        int random = rand.nextInt(Turnier_Teilnehmer.size());
        Turnier_Gewinner = Turnier_Teilnehmer.get(random);
      }

      // Gewinner wird zur Neuen Generation hinzugefügt
      Neue_Generation.add(Turnier_Gewinner.clone());

    }


    return Neue_Generation;
  }

  public void Crossover(Faltung f1, Faltung f2){
    String s = f1.getM_Faltung();
    String s2 = f2.getM_Faltung();
    String sub_s;
    String sub_s2;
    Random rand = new Random();
    int WhereToCut = rand.nextInt(s.length());
    if(WhereToCut != 0 && WhereToCut != s.length()){
      sub_s = s.substring(WhereToCut);
      s = s.substring(0, WhereToCut);
      sub_s2 = s2.substring(WhereToCut);
      s2 = s2.substring(0, WhereToCut);
      //Crossover
      s = s + sub_s2;
      s2 = s2 + sub_s;
      // Zuweisung
      f1.setM_Faltung(s);
      f2.setM_Faltung(s2);
    }
  }


  /*
  Mutationsrate erhöhen wenn die Durchschnittsfittness stagniert

   */
  public void GenetischerAlgorithmus(){


    String Seperator = ";";
    String pattern = "#,##0.00";
    StringBuilder CSV = new StringBuilder(
        "#Generationsnummer; Durchschnittliche Fitness dieser Generation; "
            + "Fitness des besten in dieser Generation; Fitness des besten in allen Generationen; "
            + "Anzahl der hydrophob Kontakte des bisher gefundenen besten in allen Generationen; "
            + "Anzahl der Überlappungen im besten bisher gefundenen in allen Generationen "
            + "Mutationsrate \n");

    Faltung Loesungskandidat = null;
    ArrayList<Faltung> Latest_Generation = new ArrayList<>();
    ArrayList<Faltung> New_Generation = null;
    Float MutationRate = m_MutationChanceMax;
    Float groessteFitness = 0.0f;
    Boolean TimeBreak = false;
    Runnable run = new Runnable() {
      public void run() {
        try {
            Thread.sleep(20000);
            System.out.print("Wait over");
            Faltungswrapper.m_Break = true;
        } catch (InterruptedException e) {
          System.out.println(" interrupted");
        }
      }
    };
    Thread thread = new Thread(run);
    thread.start();

    for (int Generation = 0; Generation < m_Generations; Generation++) {
      if(Faltungswrapper.m_Break){
        break;
      }




      Float groessteFitness_current = 0.0f;
      float average_Fitness = 0;
      if(Generation % 2 == 0){
        System.out.println("SEQ" + m_SEQ.length() + " Generation: " + Generation);
      }


      for (int child = 0; child < this.m_ChildrenPerGeneration; child++) {
        if(Faltungswrapper.m_Break){
          break;
        }

        Faltung Faltung;
        if(Generation == 0){
          Faltung = new Faltung(this.m_SEQ);
        } else {
          /*
          Sofern die Faltungsobjekte nicht neu erstellt werden (1. Generation) müssen neue anhand der alten erstellt werden.
           */

          if(New_Generation.isEmpty()){
            if(m_Turnier){
              New_Generation = TurnierSelektion(Latest_Generation);
            } else {
              New_Generation = FitnesProportionaleSelektion(Latest_Generation);
            }
            Latest_Generation = new ArrayList<>();
            Random rand = new Random();
            // Wie häufig Mutation
            // Anpassen der Mutation
            /*
            f(x) = ax+c
            c = 0.5
            f(max=Anzahl an Generationen) = 0.001
            f(1) = -0.499*x+0.5
            x = aktuelle Generation / Maximale Generation
             */
            MutationRate = 0.06f;
            //System.out.println(MutationRate);
            int Mutation_count = (int) (New_Generation.size() * MutationRate)+1;
            int Crossover_count = (int) (New_Generation.size() * m_CrossoverChance)+1;
            for (int i = 0; i < Mutation_count; i++) {
              int next_index = rand.nextInt(New_Generation.size());
              New_Generation.get(next_index).Mutation();
            }
            for(int i = 0; i < Crossover_count; i++){
              int next_index = rand.nextInt(New_Generation.size());
              int next_index2 = rand.nextInt(New_Generation.size());
              Crossover(New_Generation.get(next_index), New_Generation.get(next_index2));
            }



          }
          Faltung = New_Generation.get(child);

          // Da ich vorher nen clone aufgerufen habe ist die Fitness
          // und die zugehörigen Daten noch nicht initialisiert

          Faltung.SetupFitness();
        }

        average_Fitness += Faltung.getM_Fitness();
        if(groessteFitness_current < Faltung.getM_Fitness()){
          groessteFitness_current = Faltung.getM_Fitness();
          if(groessteFitness < Faltung.getM_Fitness()){
            Loesungskandidat = Faltung;
            groessteFitness = Faltung.getM_Fitness();
          }
        }
        Latest_Generation.add(Faltung);
      }
      New_Generation = new ArrayList<>();
      DecimalFormat df = new DecimalFormat();
      df.setMaximumFractionDigits(2); // 2 Nachkommastellen
      df.applyPattern(pattern); // Komma Statt Punkt


      CSV.append(Generation).append(Seperator); // Generationsnummer
      Float d = average_Fitness/this.m_ChildrenPerGeneration;
      String str = df.format(d);
      //System.out.println(Generation + ": " + d);
      CSV.append(str).append(Seperator); // Durchschnitt der Generation
      str = df.format(groessteFitness_current);
      CSV.append(str).append(Seperator);	// Groeßte Fitness dieser Generation
      assert Loesungskandidat != null;
      str = df.format(groessteFitness);
      CSV.append(str).append(Seperator); // Groeßte Fitness overall
      CSV.append(Loesungskandidat.getM_Hydrophobkontakte()).append(Seperator); // Hydrophob Kontakte
      CSV.append(Loesungskandidat.getM_Ueberlappungen()).append(Seperator);
      CSV.append(MutationRate);


      CSV.append("\n"); // Überlappungen
      //System.out.println(Generation + " " + Loesungskandidat.getM_Fitness());
    }


    int count = m_Output.incrementAndGet();
    assert Loesungskandidat != null;
    Loesungskandidat.DrawPic(count);
    try {
      FileWriter writer = new FileWriter("./"+ count +".csv");
      writer.write(CSV.toString());
      writer.flush();
      writer.close();
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
    GenetischerAlgorithmus();
  }
}

