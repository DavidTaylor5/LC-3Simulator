import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComputerTest {

    private static Computer myComp;

    private static BitString instruction1 = new BitString();
    private static BitString instruction2 = new BitString();
    private static BitString instruction3 = new BitString();
    private static BitString halt = new BitString();
    private static BitString value1 = new BitString();
    private static BitString value2 = new BitString();
    private static BitString value3 = new BitString();
    private static BitString testVal1 = new BitString();
    private static BitString testCC = new BitString();


    @BeforeEach
    void setUp() {
        myComp = new Computer();
        value1.setBits("0000000000010010".toCharArray());  //value1 set to 18
        value2.setBits("1111111111111111".toCharArray());  //value2 set to -1
        value3.setBits("1010101010101010".toCharArray());  //value3 set to -21846 address xAAAA

        halt.setBits("1111000000100101".toCharArray());

        myComp.loadWord(4, value1);
        myComp.loadWord(5, value2);
        myComp.loadWord(6, value3);
    }

    @Test
    void executeLoad() {
        instruction1.setBits("0010100000000011".toCharArray());    //r4 = 18     CC P

        myComp.loadWord(0, instruction1);
        myComp.loadWord(2, halt);

        myComp.execute();

        testVal1.setBits("0000000000010010".toCharArray());
        testCC.setBits("001".toCharArray());
        assertArrayEquals(testVal1.getBits(), myComp.getmRegisters()[4].getBits());
        assertArrayEquals(testCC.getBits(), myComp.getmCC().getBits());
    }

    @org.junit.jupiter.api.Test
    void executeNot() {
        instruction1.setBits("0010011000000011".toCharArray());    //r4 = 18     CC P
        instruction2.setBits("1001011011111111".toCharArray());    //r3 = NOT r3 (1101100111111100) CC N

        myComp.loadWord(0, instruction1);
        myComp.loadWord(1, instruction2);
        myComp.loadWord(2, halt);

        myComp.execute();
        testVal1.setBits("1111111111101101".toCharArray());
        testCC.setBits("100".toCharArray());

        assertArrayEquals(testVal1.getBits(), myComp.getmRegisters()[3].getBits());
        assertArrayEquals(testCC.getBits(), myComp.getmCC().getBits());

    }

    @Test
    void executeAddRegister() {
        //TEST REGISTER MODE
        instruction1.setBits("0010011000000011".toCharArray());    //r3 = 18     CC P
        instruction2.setBits("0010101000000011".toCharArray());    //r5 = -1     CC N
        instruction3.setBits("0001001011000101".toCharArray());    //r1(17) = r3(-1) + r5(18)  CC P

        myComp.loadWord(0, instruction1);
        myComp.loadWord(1, instruction2);
        myComp.loadWord(2, instruction3);
        myComp.loadWord(3, halt);
        myComp.execute();

        testVal1.setBits("0000000000010001".toCharArray());
        testCC.setBits("001".toCharArray());
        assertArrayEquals(testVal1.getBits(), myComp.getmRegisters()[1].getBits());
        assertArrayEquals(testCC.getBits(), myComp.getmCC().getBits());
    }

    @Test
    void executeAddImmediate() {
        //TEST Immediate MODE
        instruction1.setBits("0010011000000011".toCharArray());    //r3 = 18     CC P
        instruction2.setBits("0001001011110000".toCharArray());    //r1(3) = r3(18) + (-15)  CC P

        myComp.loadWord(0, instruction1);
        myComp.loadWord(1, instruction2);
        myComp.loadWord(2, halt);
        myComp.execute();

        testVal1.setBits("0000000000000010".toCharArray());
        testCC.setBits("001".toCharArray());
        assertArrayEquals(testVal1.getBits(), myComp.getmRegisters()[1].getBits());
        assertArrayEquals(testCC.getBits(), myComp.getmCC().getBits());
    }

    @Test
    void executeAndRegister() {
        //TEST REGISTER MODE
        instruction1.setBits("0010011000000011".toCharArray());    //LD r3 = 18     CC P
        instruction2.setBits("0010101000000011".toCharArray());    //LD r5 = -1     CC N
        instruction3.setBits("0101001011000101".toCharArray());    //r1(18) = r3(-1) AND r5(18)  CC P

        myComp.loadWord(0, instruction1);
        myComp.loadWord(1, instruction2);
        myComp.loadWord(2, instruction3);
        myComp.loadWord(3, halt);
        myComp.execute();

        testVal1.setBits("0000000000010010".toCharArray());
        testCC.setBits("001".toCharArray());
        assertArrayEquals(testVal1.getBits(), myComp.getmRegisters()[1].getBits());
        assertArrayEquals(testCC.getBits(), myComp.getmCC().getBits());

    }

    @Test
    void executeAndImmediate() {
        //TEST REGISTER MODE
        instruction1.setBits("0010011000000011".toCharArray());    //LD r3 = 18     CC P
        instruction2.setBits("0101001011100010".toCharArray());    //r1(2) = r3(18) AND (2)  CC P

        myComp.loadWord(0, instruction1);
        myComp.loadWord(1, instruction2);
        myComp.loadWord(2, halt);
        myComp.execute();

        testVal1.setBits("0000000000000010".toCharArray());
        testCC.setBits("001".toCharArray());
        assertArrayEquals(testVal1.getBits(), myComp.getmRegisters()[1].getBits());
        assertArrayEquals(testCC.getBits(), myComp.getmCC().getBits());

    }

    @Test
    void executeBR() {
        instruction1.setBits("0010011000000011".toCharArray());    //r3 = 18     CC P
        instruction2.setBits("0001011011111110".toCharArray());    //r3(16) = r3(18) + (-2)  CC P
        instruction3.setBits("0000001111111110".toCharArray());     //subtract 2 until r1 is <= 0;

        myComp.loadWord(0, instruction1);
        myComp.loadWord(1, instruction2);
        myComp.loadWord(2, instruction3);
        myComp.loadWord(3, halt);
        myComp.execute();

        testVal1.setBits("0000000000000000".toCharArray());
        testCC.setBits("010".toCharArray());
        assertArrayEquals(testVal1.getBits(), myComp.getmRegisters()[3].getBits());
        assertArrayEquals(testCC.getBits(), myComp.getmCC().getBits());
    }

    @Test
    void executeLEA() {
        instruction1.setBits("1110011000000101".toCharArray());    //LEA r3 = with address of Val3 (-21846)     CC N
                                                                      //r3 = 6
        myComp.loadWord(0, instruction1);
        myComp.loadWord(1, halt);
        myComp.execute();

        testVal1.setBits("0000000000000110".toCharArray());
        assertArrayEquals(testVal1.getBits(), myComp.getmRegisters()[3].getBits());

        //LEA does not set CC

    }

    @Test
    void executeLDI(){
        instruction1.setBits("1010011000000011".toCharArray()); //LDI r3 = m[ m[pc + offset] ]
        BitString value18 = new BitString();
        value18.setValue(42);

        myComp.loadWord(0, instruction1);
        myComp.loadWord(1, halt);
        myComp.loadWord(18, value18);

        myComp.execute();

        testVal1.setBits("0000000000101010".toCharArray()); //42
        testCC.setBits("001".toCharArray());
        assertArrayEquals(testVal1.getBits(), myComp.getmRegisters()[3].getBits());
        assertArrayEquals(testCC.getBits(), myComp.getmCC().getBits());
    }

    @Test
    void executeSTI(){
        instruction1.setBits("0010011000000101".toCharArray()); //r3 = -21846
        instruction2.setBits("1011011000000010".toCharArray()); //m[18] <- m[6] <- r3 (PC + offset)
                                                                  //m[18] = -21846
        myComp.loadWord(0, instruction1);
        myComp.loadWord(1, instruction2);
        myComp.loadWord(2, halt);

        myComp.execute();

        testVal1.setValue2sComp(-21846);
        assertArrayEquals(testVal1.getBits(), myComp.getmMemory()[18].getBits());

        //STI doesn't set CC
    }

    @Test
    void executeLDR(){
        instruction1.setBits("0010011000000011".toCharArray()); //r3 = 18
        instruction2.setBits("0110001011000011".toCharArray()); //r1 = m [r3(18) + offset6(3)]
                                                                 //r1 = m[21] (87)
        BitString value21 = new BitString();
        value21.setValue(888);

        myComp.loadWord(0, instruction1);
        myComp.loadWord(1, instruction2);
        myComp.loadWord(2, halt);
        myComp.loadWord(21, value21);
        myComp.execute();

        testVal1.setValue(888);
        assertArrayEquals(testVal1.getBits(), myComp.getmMemory()[21].getBits());
        testCC.setBits("001".toCharArray());
        assertArrayEquals(testCC.getBits(), myComp.getmCC().getBits());
    }

    @Test
    void executeSTR(){
        instruction1.setBits("0010011000000011".toCharArray()); //r3 = 18
        instruction2.setBits("0010101000000011".toCharArray()); //r5 = -1
        instruction3.setBits("0111011101001010".toCharArray()); //m [r5(18) + offset6(10)] = r3(18)
                                                                    //m[9] = 18
        myComp.loadWord(0, instruction1);
        myComp.loadWord(1, instruction2);
        myComp.loadWord(2, instruction3);
        myComp.loadWord(3, halt);
        myComp.execute();

        testVal1.setValue(18);
        assertArrayEquals(testVal1.getBits(), myComp.getmMemory()[9].getBits());
    }



}