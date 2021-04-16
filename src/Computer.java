/**
 * Computer class comprises of memory, registers, cc and
 * can execute the instructions based on PC and IR 
 * @author mmuppa
 *
 */
public class Computer {

	private final static int MAX_MEMORY = 50;
	private final static int MAX_REGISTERS = 8;

	private BitString mRegisters[];
	private BitString mMemory[];
	private BitString mPC;
	private BitString mIR;
	private BitString mCC;

	/**
	 * Initializes all the memory to 0, registers to 0 to 7
	 * PC, IR to 16 bit 0s and CC to 000 
	 * Represents the initial state 
	 */
	public Computer() {
		mPC = new BitString();
		mPC.setValue(0);
		mIR = new BitString();
		mIR.setValue(0);
		mCC = new BitString();
		mCC.setBits(new char[] { '0', '0', '0' });
		mRegisters = new BitString[MAX_REGISTERS];
		for (int i = 0; i < MAX_REGISTERS; i++) {
			mRegisters[i] = new BitString();
			mRegisters[i].setValue(i);
		}

		mMemory = new BitString[MAX_MEMORY];
		for (int i = 0; i < MAX_MEMORY; i++) {
			mMemory[i] = new BitString();
			mMemory[i].setValue(0);
		}
	}

	public BitString[] getmRegisters() {
		return mRegisters;
	}

	public BitString[] getmMemory() {
		return mMemory;
	}

	public BitString getmCC() {
		return mCC;
	}

	/**
	 * Loads a 16 bit word into memory at the given address. 
	 * @param address memory address
	 * @param word data or instruction or address to be loaded into memory
	 */
	public void loadWord(int address, BitString word) {
		if (address < 0 || address >= MAX_MEMORY) {
			throw new IllegalArgumentException("Invalid address");
		}
		mMemory[address] = word;
	}

	/**
	 * Performs not operation by using the data from the register based on bits[7:9] 
	 * and inverting and storing in the register based on bits[4:6]
	 */
	public void executeNot() {
		BitString destBS = mIR.substring(4, 3);
		BitString sourceBS = mIR.substring(7, 3);
		mRegisters[destBS.getValue()] = mRegisters[sourceBS.getValue()].copy();
		mRegisters[destBS.getValue()].invert();

		BitString notVal = mRegisters[destBS.getValue()];
		setConditionalCode(notVal);
	}

	/**
	 * Performs load operation, destination register is bits[4:6],
	 * get value from memory location [9 bit offset[7:15] + PC value]
	 */
	public void executeLoad(){
		BitString destBS = mIR.substring(4, 3);
		BitString pcOffset = mIR.substring(7, 9);
		BitString wantedVal = mMemory[mPC.getValue() + pcOffset.getValue2sComp()];
		mRegisters[destBS.getValue()] = wantedVal;

		setConditionalCode(wantedVal);
		//now I just need to set the conditional codes.
	}

	/**
	 * Performs the add operation with destination register [4:6] and first operand [7:9].
	 * Based on bit [10] it will either use immediate mode with [11:15] 2's complement or
	 * register mode with bits [13:15] designating second register for second operand.
	 */
	public void executeAdd(){
		BitString destBS = mIR.substring(4, 3);
		BitString firstOperand = mIR.substring(7, 3);
		int sum;
		if(mIR.substring(10, 1).getValue() == 1){
			int imma5 = mIR.substring(11, 5).getValue2sComp();
			sum = imma5 + mRegisters[firstOperand.getValue()].getValue2sComp();
		} else {
			BitString secondOperand = mIR.substring(13, 3);
			sum = mRegisters[secondOperand.getValue()].getValue2sComp() + mRegisters[firstOperand.getValue()].getValue2sComp();
		}

		mRegisters[destBS.getValue()].setValue(sum);

		setConditionalCode(mRegisters[destBS.getValue()]);


	}

	/**
	 * Performs the and operation, sets destination register [4:6] with product of register [7:9] AND register [13:15].
	 * If in immediate mode, it will sign extend the immediate modes 2's complement and and it with register [7:9].
	 * AND function sets the conditional codes.
	 */
	public void executeAnd(){
		BitString destBS = mIR.substring(4, 3);
		BitString operand1 = mRegisters[mIR.substring(7, 3).getValue()];
		char[] andSolution = new char[16];
		BitString operand2 = new BitString();
		if(mIR.substring(10, 1).getValue() == 1){  //immediate mode
			operand2 = mIR.substring(11, 5);   //imma5
			if(operand2.getValue2sComp() < 0){
				BitString negativeExtend = new BitString();
				negativeExtend.setBits("11111111111".toCharArray());
				negativeExtend.append(operand2);
				operand2 = negativeExtend;
			} else {
				BitString positiveExtended = new BitString();
				positiveExtended.setBits("00000000000".toCharArray());
				operand2 = positiveExtended.append(operand2);
			}
		} else {  												//register mode
			operand2 = mRegisters[mIR.substring(13, 3).getValue()];
		}


		for (int i = 0; i < operand1.getLength(); i++) {
			if(operand1.substring(i, 1).getValue() + operand2.substring(i, 1).getValue() ==2){
				andSolution[i] = '1';
			} else {
				andSolution[i] = '0';
			}
		}

		mRegisters[destBS.getValue()].setBits(andSolution);

		setConditionalCode(mRegisters[destBS.getValue()]);
	}

	/**
	 * Performs the branch execution by changes PC based on previous set conditional code.
	 */
	public void executeBR(){
		BitString conditions = mIR.substring(4, 3);
		BitString setInstruction = mIR.substring(7, 9);
		boolean ccMatch = false;
		for (int i = 0; i < conditions.getLength() ; i++) {
			if(conditions.substring(i, 1).getValue() + mCC.substring(i, 1).getValue() == 2){
				ccMatch = true;
			}
		}

		if(ccMatch){
			mPC.setValue(mPC.getValue() + setInstruction.getValue2sComp());
		}
	}

	/**
	 * Performs the OUT subroutine for TRAP instructions. I can take the value from register 0
	 * and convert the int to a char (char c = (char) 48). I print the char without new line to the console.
	 */
	public void executeOUT(){
		int asciiVal = mRegisters[0].getValue();
		char toPrint = (char)asciiVal;
		System.out.print(toPrint);
	}

	/**
	 * Many of the functions have the ability to change the conditional code, this code snippet set conditional code.
	 * @param word The value that has been operated on.
	 */
	private void setConditionalCode(BitString word){
		if(word.getValue2sComp() > 0){
			mCC.setBits(new char[] {'0', '0', '1'});
		} else if(word.getValue2sComp() < 0){
			mCC.setBits(new char[] {'1', '0', '0'});
		} else {
			mCC.setBits(new char[] {'0', '1', '0'});
		}
	}

	/**
	 * This method will execute all the instructions starting at address 0 
	 * till HALT instruction is encountered. 
	 */
	public void execute() {
		BitString opCodeStr;
		int opCode;

		while (true) {
			// Fetch the instruction
			mIR = mMemory[mPC.getValue()];
			mPC.addOne();

			// Decode the instruction's first 4 bits 
			// to figure out the opcode
			opCodeStr = mIR.substring(0, 4);
			opCode = opCodeStr.getValue();

			// What instruction is this?
			if (opCode == 9) { // NOT
				executeNot();
			} else if (opCode == 0) { //BR
				executeBR();
			} else if (opCode == 1) { //ADD
				executeAdd();
			} else if (opCode == 2) { //LD
				executeLoad();
			} else if (opCode == 5) { //AND
				executeAnd();
			} else if (opCode == 6) { //LDR
				executeLDR();
			} else if (opCode == 7) { //STR
				executeSTR();
			} else if (opCode == 10) { //LDI
				executeLDI();
			} else if (opCode == 11) { //STI
				executeSTI();
			} else if (opCode == 14) { //LEA
				executeLEA();
			} else if (opCode == 15) { //TRAP
				BitString trapStr = mIR.substring(8, 8);
				int trapVal = trapStr.getValue();
				if (trapVal == 33){  //x21 = binary 18 means OUT
					executeOUT();
				}else if(trapVal == 37) { //x25 = binary 37  means HALT
					return;  //halt instruction means to stop executing.
				} else{ System.out.println("Not valid Trap Command.");}
			} else {
				System.out.println("INVALID INSTRUCTION ENTERED.");
			}
			// TODO - Others
		}
	}

	/**
	 * Displays the computer's state
	 */
	public void display() {
		System.out.print("\nPC ");
		mPC.display(true);
		System.out.print("   ");

		System.out.print("IR ");
		mPC.display(true);
		System.out.print("   ");

		System.out.print("CC ");
		mCC.display(true);
		System.out.println("   ");

		for (int i = 0; i < MAX_REGISTERS; i++) {
			System.out.printf("R%d ", i);
			mRegisters[i].display(true);
			if (i % 3 == 2) {
				System.out.println();
			} else {
				System.out.print("   ");
			}
		}
		System.out.println();

		for (int i = 0; i < MAX_MEMORY; i++) {
			System.out.printf("%3d ", i);
			mMemory[i].display(true);
			if (i % 3 == 2) {
				System.out.println();
			} else {
				System.out.print("   ");
			}
		}
		System.out.println();

	}

	//EXTRA CREDIT FUNCTIONS

	/**
	 * This performs LEA by loading the address from PC offset and PC into destination register.
	 */
	public void executeLEA(){
		BitString destBS = mIR.substring(4, 3);
		BitString pcOffset = mIR.substring(7, 9);
		int wantedAdr = mPC.getValue() + pcOffset.getValue2sComp();
		BitString bitAdr = new BitString();
		bitAdr.setValue(wantedAdr);
		mRegisters[destBS.getValue()] = bitAdr;  //If I want location of

		setConditionalCode(bitAdr);
		//now I just need to set the conditional codes.
	}

	/**
	 * Performs LDI, PC + PC offset evaluates to an address, that address is visited to put value into destination register.
	 */
	public void executeLDI(){
		BitString destBS = mIR.substring(4, 3);
		BitString pcOffset = mIR.substring(7, 9);

		BitString wantedAdr = mMemory[mPC.getValue() + pcOffset.getValue2sComp()];
		BitString wantedVal = mMemory[wantedAdr.getValue()];
		mRegisters[destBS.getValue()] = wantedVal;

		setConditionalCode(wantedVal);
	}

	/**
	 * Performs STI, stores source register value into memory[ memory[PC + Offset] ].
	 */
	public void executeSTI(){
		BitString sourceBS = mIR.substring(4, 3);
		BitString pcOffset = mIR.substring(7, 9);

		BitString wantedAdr = mMemory[mPC.getValue() + pcOffset.getValue2sComp()];
		mMemory[wantedAdr.getValue()] = mRegisters[sourceBS.getValue()];

		//STI doesn't set CC
	}

	/**
	 * Performs LDR, destination register gets value from memory location m[source register + offset 6].
	 */
	public void executeLDR(){
		BitString destBS = mIR.substring(4, 3);
		BitString sourceBS = mIR.substring(7, 3);
		BitString offset6 = mIR.substring(10, 6);

		BitString wantedVal = mMemory[mRegisters[sourceBS.getValue()].getValue2sComp() + offset6.getValue2sComp()];
		mRegisters[destBS.getValue()] = wantedVal;

		setConditionalCode(wantedVal);
	}

	/**
	 * Performs STR, m [base register + offset6] = source register
	 */
	public void executeSTR(){
		BitString sourceBS = mIR.substring(4, 3);
		BitString baseBS = mIR.substring(7, 3);
		BitString offset6 = mIR.substring(10, 6);


		mMemory[mRegisters[baseBS.getValue()].getValue2sComp() + offset6.getValue2sComp()] = mRegisters[sourceBS.getValue()];

		//STR doesn't set CC
	}


}
