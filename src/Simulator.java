/**
 * Simulator class contains the instructions to run an entire
 * machine code program that will display 9 to 1 to the console 
 * @author mmuppa
 *
 */
public class Simulator {

	public static void main(String[] args) {

		Computer comp;

		/************************************** */
		/** The next two variables - program and programSize - */
		/** allow someone using the simulator (such as a grader) */
		/** to decide what program will be simulated. */
		/** The simulation must load and execute */
		/** instructions found in the "program" array. */
		/** 
		 *
		 * /
		/** - recompile your program without further changes */
		/** and see the simulator load and execute the new program. */
		/************************************** */

		String program[] = { "0010000000000111", "0010001000000111",
				"0001010000000001", "0000010000000011", "1111000000100001",
				"0001000000111111", "0000111111111011", "1111000000100101",
				"0000000000111001", "1111111111010000" };

		/*
		 * This is the assembly program that was compiled into the binary
		 * program shown above. 
		 * 		.ORIG x3000
		 * 
		 * 		LD R0 START 
		 * 		LD R1 END 
		 * TOP 	ADD R2 R0 R1 
		 * 		BRZ DONE 
		 * 		OUT 
		 * 		ADD R0 R0 -1
		 * 		BRNZP TOP 
		 * DONE HALT
		 * 
		 * START .FILL x39 
		 * END .FILL x-30
		 * 
		 * 		.END
		 */
		comp = new Computer();
		comp.display();

		/* TODO - load the instructions in the "program" array */
		for (int i=0; i<program.length; i++) {
			BitString instr = new BitString();
			instr.setBits(program[i].toCharArray());
			comp.loadWord(i, instr);
		}

		/* execute the program */
		/* During execution, the only output to the screen should be */
		/* the result of executing OUT. */
		comp.execute();

		/* shows final configuration of computer */
		comp.display();
	}

}
