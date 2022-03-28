package src;
import java.util.ArrayList;

public class Main {

	public static final String SEQ20 = "10100110100101100101";
	public static final String SEQ24 = "110010010010010010010011";
	public static final String SEQ25 = "0010011000011000011000011";
	public static final String SEQ36 = "000110011000001111111001100001100100";
	public static final String SEQ48 = "001001100110000011111111110000001100110010011111";
	public static final String SEQ50 = "11010101011110100010001000010001000101111010101011";
	public static final String[] SEQS = {
			//"10100110100101100101",                              // SEQ 20
			//"110010010010010010010011",                          // SEQ 24
			//"0010011000011000011000011",                          // SEQ 25
			//"000110011000001111111001100001100100",                // SEQ 36
			//"001001100110000011111111110000001100110010011111",    // SEQ 48
			//"11010101011110100010001000010001000101111010101011"  // SEQ 50
			"001110111111110001111111111010001111111111110000111111011010" // SEQ 60
	};
	public static final String SEQ60 = "001110111111110001111111111010001111111111110000111111011010";

	public static final Integer ChildsPerGeneration = 1000;
	public static final Integer Generations = 50;
	public static final Float CrossOverChance = 0.26f;
	public static final boolean TurnierSelektion = false;
	public static final Float MutationMax = 0.125f;
	public static final Float MutationMin = 0.001f;

	/*
	Linear reduction in mutation rate
	0.5 to 0.001
	https://research.ijcaonline.org/volume72/number17/pxc3889343.pdf
	 */


	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		ArrayList<Thread> Thread_Holder = new ArrayList<>();
		for (String SEQ : SEQS) {
			Thread_Holder.add(
					new Thread(
							new Faltungswrapper(SEQ, ChildsPerGeneration, Generations, MutationMax, MutationMin,
									CrossOverChance, TurnierSelektion)
					)
			);
		}
		for (Thread T : Thread_Holder) {
			T.start();
		}
		int i = 0;
		for (Thread T : Thread_Holder) {
			try {
				T.join();
				System.out.println(++i + " Joined");
			} catch (Exception e) {
				System.out.println("Error joining threads");
			}

		}

		long endTime = System.currentTimeMillis();
		System.out.println("That took " + (endTime - startTime) + " milliseconds");
	}


}